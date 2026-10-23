package co.edu.unicauca.question.service;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.dto.LifecycleOperationDTO;
import co.edu.unicauca.question.exception.ApiException;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.OperationStatus;
import co.edu.unicauca.question.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Registro y consulta del estado de las operaciones asíncronas. */
@Service
public class OperationService {

    private static final Logger log = LoggerFactory.getLogger(OperationService.class);

    private final OperationRepository repository;
    private final LifecycleClient lifecycleClient;
    private final long reconcileAfterMs;

    public OperationService(OperationRepository repository,
                            LifecycleClient lifecycleClient,
                            @Value("${operations.reconcile-after-ms:4000}") long reconcileAfterMs) {
        this.repository = repository;
        this.lifecycleClient = lifecycleClient;
        this.reconcileAfterMs = reconcileAfterMs;
    }

    @Transactional
    public Operation createPending(Long questionId, String type, Long requestedBy) {
        return repository.save(new Operation(UUID.randomUUID().toString(), questionId, type, requestedBy));
    }

    @Transactional
    public void markFailed(String operationId, String message) {
        repository.findById(operationId).ifPresent(op -> {
            op.finish(OperationStatus.FAILED, null, message);
            repository.save(op);
        });
    }

    /** Aplica el resultado recibido por RabbitMQ (idempotente). */
    @Transactional
    public void applyResult(String operationId, Long questionId, String type, String status,
                            String resultingState, String message) {
        Operation op = repository.findById(operationId).orElse(null);
        if (op == null) {
            log.warn("[QuestionService] Resultado para operación desconocida {}; se registra igualmente", operationId);
            op = new Operation(operationId, questionId, type, null);
        }
        if (op.isFinished()) {
            log.info("[QuestionService] Operación {} ya estaba finalizada ({}); resultado duplicado ignorado",
                    operationId, op.getStatus());
            return;
        }
        op.finish(parseStatus(status), resultingState, message);
        repository.save(op);
    }

    /**
     * Consulta una operación. Si sigue PENDING más tiempo del esperado (p. ej. se perdió el evento
     * de resultado), se reconcilia consultando a Lifecycle Service por REST.
     */
    @Transactional
    public Operation get(String operationId) {
        Operation op = repository.findById(operationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La operación " + operationId + " no existe"));
        if (!op.isFinished() && isOverdue(op)) {
            try {
                LifecycleOperationDTO remote = lifecycleClient.getOperation(operationId);
                if (remote != null && !"PENDING".equals(remote.status())) {
                    log.info("[QuestionService] Operación {} reconciliada vía REST: {}", operationId, remote.status());
                    op.finish(parseStatus(remote.status()), remote.resultingState(), remote.message());
                    repository.save(op);
                }
            } catch (ApiException ex) {
                log.warn("[QuestionService] No se pudo reconciliar la operación {}: {}", operationId, ex.getMessage());
            }
        }
        return op;
    }

    @Transactional(readOnly = true)
    public List<Operation> findByQuestion(Long questionId) {
        return repository.findByQuestionIdOrderByCreatedAtDesc(questionId);
    }

    private boolean isOverdue(Operation op) {
        return Duration.between(op.getCreatedAt(), LocalDateTime.now()).toMillis() > reconcileAfterMs;
    }

    private static OperationStatus parseStatus(String status) {
        try {
            return OperationStatus.valueOf(status);
        } catch (Exception ex) {
            return OperationStatus.FAILED;
        }
    }
}
