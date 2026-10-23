package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.dto.OperationResultDTO;
import co.edu.unicauca.lifecycle.dto.StateResponse;
import co.edu.unicauca.lifecycle.exception.ApiException;
import co.edu.unicauca.lifecycle.messaging.message.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.*;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import co.edu.unicauca.lifecycle.repository.StateTransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Autoridad única sobre el estado de las preguntas. Ningún otro servicio modifica el estado:
 * lo solicitan por REST (transición síncrona) o por RabbitMQ (comando asíncrono).
 */
@Service
public class LifecycleService {

    private static final Logger log = LoggerFactory.getLogger(LifecycleService.class);
    public static final String OP_SUBMIT_FOR_REVIEW = "SUBMIT_FOR_REVIEW";

    private final QuestionStateRepository stateRepository;
    private final StateTransitionRepository transitionRepository;
    private final OperationResultRepository operationRepository;

    public LifecycleService(QuestionStateRepository stateRepository,
                            StateTransitionRepository transitionRepository,
                            OperationResultRepository operationRepository) {
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.operationRepository = operationRepository;
    }

    /** Registra una pregunta nueva en estado DRAFT. */
    @Transactional
    public StateResponse register(Long questionId, Long requestedBy) {
        if (questionId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "questionId es obligatorio");
        }
        if (stateRepository.existsByQuestionId(questionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "La pregunta " + questionId + " ya está registrada");
        }
        QuestionState state = stateRepository.save(
                new QuestionState(questionId, QuestionStatus.DRAFT, requestedBy, "Pregunta creada"));
        transitionRepository.save(new StateTransition(questionId, null, QuestionStatus.DRAFT, requestedBy, "Pregunta creada"));
        log.info("[Lifecycle] Pregunta {} registrada en DRAFT", questionId);
        return StateResponse.from(state, null);
    }

    @Transactional(readOnly = true)
    public StateResponse getState(Long questionId) {
        return StateResponse.from(findState(questionId), null);
    }

    @Transactional(readOnly = true)
    public List<StateTransition> getHistory(Long questionId) {
        findState(questionId);
        return transitionRepository.findByQuestionIdOrderByIdAsc(questionId);
    }

    /**
     * Aplica una transición validando la máquina de estados.
     * 404 si la pregunta no existe, 409 si la transición no está permitida.
     */
    @Transactional
    public StateResponse transition(Long questionId, QuestionStatus target, Long requestedBy, String comment) {
        if (target == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "targetState es obligatorio");
        }
        QuestionState state = findState(questionId);
        QuestionStatus current = state.getState();

        if (!current.canTransitionTo(target)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Transición no permitida: " + current + " → " + target
                            + ". Desde " + current + " solo se permite: " + current.allowedTargets());
        }
        if (target == QuestionStatus.REJECTED && (comment == null || comment.isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe registrar el motivo del rechazo (comment)");
        }

        state.applyTransition(target, requestedBy, comment);
        stateRepository.save(state);
        transitionRepository.save(new StateTransition(questionId, current, target, requestedBy, comment));
        log.info("[Lifecycle] Pregunta {}: {} → {}", questionId, current, target);
        return StateResponse.from(state, current);
    }

    /**
     * Procesa el comando SubmitQuestionForReview (DRAFT → PENDING_REVIEW) y registra el resultado
     * de la operación. Es idempotente: un operationId ya procesado no se vuelve a aplicar.
     */
    @Transactional
    public OperationResult processSubmitCommand(SubmitQuestionForReviewCommand command) {
        Optional<OperationResult> existing = operationRepository.findById(command.operationId());
        if (existing.isPresent() && existing.get().getStatus() != OperationStatus.PENDING) {
            log.warn("[Lifecycle] Operación {} ya procesada ({}); se ignora el duplicado",
                    command.operationId(), existing.get().getStatus());
            return existing.get();
        }

        OperationResult result = existing.orElseGet(() -> new OperationResult(
                command.operationId(), command.questionId(), OP_SUBMIT_FOR_REVIEW, command.requestedAt()));

        try {
            StateResponse response = transition(command.questionId(), QuestionStatus.PENDING_REVIEW,
                    command.requestedBy(), "Enviada a revisión (operación " + command.operationId() + ")");
            result.complete(response.state(), "Transición " + response.previousState() + " → "
                    + response.state() + " completada");
        } catch (ApiException ex) {
            QuestionStatus current = stateRepository.findByQuestionId(command.questionId())
                    .map(QuestionState::getState).orElse(null);
            result.fail(current, ex.getMessage());
            log.warn("[Lifecycle] Operación {} FALLIDA: {}", command.operationId(), ex.getMessage());
        }
        return operationRepository.save(result);
    }

    @Transactional(readOnly = true)
    public OperationResultDTO getOperation(String operationId) {
        return operationRepository.findById(operationId)
                .map(OperationResultDTO::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "La operación " + operationId + " no existe o aún no ha sido recibida"));
    }

    private QuestionState findState(Long questionId) {
        return stateRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "La pregunta " + questionId + " no está registrada en el ciclo de vida"));
    }
}
