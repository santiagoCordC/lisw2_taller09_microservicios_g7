package co.edu.unicauca.question.service;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.client.dto.LifecycleStateDTO;
import co.edu.unicauca.question.client.dto.UserDTO;
import co.edu.unicauca.question.client.dto.ValidationRequestDTO;
import co.edu.unicauca.question.client.dto.ValidationResultDTO;
import co.edu.unicauca.question.dto.CreateQuestionRequest;
import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.QuestionResponse;
import co.edu.unicauca.question.exception.ApiException;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.messaging.EventPublisher;
import co.edu.unicauca.question.messaging.message.SubmitQuestionForReviewCommand;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.Question;
import co.edu.unicauca.question.model.QuestionOption;
import co.edu.unicauca.question.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Coordina el flujo de la pregunta:
 * - Creación/edición: User Service (REST) → Validation Service (REST) → persistencia → Lifecycle (REST).
 * - Envío a revisión: comando asíncrono por RabbitMQ + 202 Accepted.
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final Set<String> EDITABLE_STATES = Set.of("DRAFT", "REJECTED");
    public static final String OP_SUBMIT_FOR_REVIEW = "SUBMIT_FOR_REVIEW";

    private final QuestionRepository questionRepository;
    private final UserClient userClient;
    private final ValidationClient validationClient;
    private final LifecycleClient lifecycleClient;
    private final CommandPublisher commandPublisher;
    private final EventPublisher eventPublisher;
    private final OperationService operationService;

    public QuestionService(QuestionRepository questionRepository, UserClient userClient,
                           ValidationClient validationClient, LifecycleClient lifecycleClient,
                           CommandPublisher commandPublisher, EventPublisher eventPublisher,
                           OperationService operationService) {
        this.questionRepository = questionRepository;
        this.userClient = userClient;
        this.validationClient = validationClient;
        this.lifecycleClient = lifecycleClient;
        this.commandPublisher = commandPublisher;
        this.eventPublisher = eventPublisher;
        this.operationService = operationService;
    }

    public record CreateResult(QuestionResponse question, List<String> warnings) {
    }

    // ------------------------------------------------------------------ creación (síncrona)

    public CreateResult create(Long userId, CreateQuestionRequest request) {
        UserDTO author = requireAuthor(userId);
        ValidationResultDTO validation = validateOrFail(request, author.id(), null);

        Question question = new Question();
        applyContent(question, request);
        question.setAuthorId(author.id());
        question = questionRepository.save(question);

        try {
            LifecycleStateDTO state = lifecycleClient.register(question.getId(), author.id());
            log.info("[QuestionService] Pregunta {} creada por {} en estado {}", question.getId(),
                    author.username(), state.state());
            return new CreateResult(QuestionResponse.from(question, state.state()), warnings(validation));
        } catch (ApiException ex) {
            // Compensación: sin estado inicial la pregunta quedaría huérfana, así que no se conserva
            questionRepository.delete(question);
            throw new ApiException(ex.getStatus(),
                    "No se pudo registrar el estado inicial de la pregunta; no fue creada. " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ corrección (síncrona)

    public CreateResult update(Long userId, Long questionId, CreateQuestionRequest request) {
        Question question = findQuestion(questionId);
        UserDTO author = requireAuthor(userId);
        if (!question.getAuthorId().equals(author.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el autor puede modificar la pregunta");
        }

        String currentState = lifecycleClient.getState(questionId).state();
        if (!EDITABLE_STATES.contains(currentState)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La pregunta está en estado " + currentState + "; solo se puede modificar en DRAFT o REJECTED");
        }

        ValidationResultDTO validation = validateOrFail(request, author.id(), questionId);

        String newState = currentState;
        if ("REJECTED".equals(currentState)) {
            // Corrección de una pregunta rechazada: Lifecycle la devuelve a DRAFT para poder reenviarla
            newState = lifecycleClient.transition(questionId, "DRAFT", author.id(),
                    "Pregunta corregida por el autor").state();
        }

        applyContent(question, request);
        question = questionRepository.save(question);
        return new CreateResult(QuestionResponse.from(question, newState), warnings(validation));
    }

    // ------------------------------------------------------------------ envío a revisión (asíncrono)

    public Operation submitForReview(Long userId, Long questionId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debe autenticarse (encabezado X-User-Id)");
        }
        Question question = findQuestion(questionId);
        if (!question.getAuthorId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el autor puede enviar la pregunta a revisión");
        }

        Operation operation = operationService.createPending(questionId, OP_SUBMIT_FOR_REVIEW, userId);
        SubmitQuestionForReviewCommand command = new SubmitQuestionForReviewCommand(
                operation.getOperationId(), questionId, userId, LocalDateTime.now());
        try {
            commandPublisher.publishSubmitForReview(command);
        } catch (AmqpException ex) {
            log.error("[QuestionService] No se pudo publicar el comando: {}", ex.getMessage());
            operationService.markFailed(operation.getOperationId(),
                    "No se pudo encolar el comando en RabbitMQ: " + ex.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El sistema de mensajería no está disponible; la solicitud no fue aceptada. Intente más tarde.");
        }
        return operation;
    }

    // ------------------------------------------------------------------ consultas

    public QuestionResponse get(Long questionId) {
        Question question = findQuestion(questionId);
        return QuestionResponse.from(question, currentStateOrUnknown(questionId));
    }

    public List<QuestionResponse> list(Long authorId) {
        List<Question> questions = authorId == null ? questionRepository.findAll()
                : questionRepository.findByAuthorId(authorId);
        return questions.stream()
                .map(q -> QuestionResponse.from(q, currentStateOrUnknown(q.getId())))
                .toList();
    }

    // ------------------------------------------------------------------ auxiliares

    private UserDTO requireAuthor(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debe autenticarse (encabezado X-User-Id)");
        }
        UserDTO user = userClient.getUser(userId);
        if (!user.active()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El usuario está inactivo");
        }
        if (!user.hasAnyRole("DOCENTE", "AUTOR")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un DOCENTE o AUTOR puede crear o modificar preguntas");
        }
        return user;
    }

    private ValidationResultDTO validateOrFail(CreateQuestionRequest request, Long authorId, Long questionId) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        ValidationResultDTO result = validationClient.validate(new ValidationRequestDTO(
                request.context(), request.statement(), request.competence(), request.topic(),
                request.subtopic(), request.options(), request.correctOptionId()));
        if (result == null || !result.valid()) {
            List<String> errors = result == null ? List.of("Respuesta vacía de Validation Service") : result.errors();
            eventPublisher.publishValidationFailed(questionId, authorId, errors);
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La pregunta no superó la validación estructural; no se registró como válida", errors);
        }
        return result;
    }

    private static List<String> warnings(ValidationResultDTO validation) {
        return validation.warnings() == null ? List.of() : validation.warnings();
    }

    private static void applyContent(Question question, CreateQuestionRequest request) {
        question.setContext(request.context());
        question.setStatement(request.statement().trim());
        question.setOptions(request.options().stream()
                .map((OptionDTO o) -> new QuestionOption(o.id().trim().toUpperCase(), o.text().trim()))
                .toList());
        question.setCorrectOptionId(request.correctOptionId().trim().toUpperCase());
        question.setJustification(request.justification());
        question.setBibliography(request.bibliography());
        question.setCompetence(request.competence());
        question.setTopic(request.topic());
        question.setSubtopic(request.subtopic());
        question.setDifficulty(request.difficulty());
    }

    private Question findQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no existe"));
    }

    /** El estado vive en Lifecycle; si no responde, la consulta de contenido no se cae. */
    private String currentStateOrUnknown(Long questionId) {
        try {
            return lifecycleClient.getState(questionId).state();
        } catch (ApiException ex) {
            log.warn("[QuestionService] No se pudo consultar el estado de {}: {}", questionId, ex.getMessage());
            return "UNKNOWN";
        }
    }
}
