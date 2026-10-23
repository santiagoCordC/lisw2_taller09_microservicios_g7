package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.messaging.message.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Consumidor asíncrono del comando SubmitQuestionForReview. */
@Component
public class CommandListener {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final LifecycleService lifecycleService;
    private final EventPublisher eventPublisher;
    private final long processingDelayMs;

    public CommandListener(LifecycleService lifecycleService,
                           EventPublisher eventPublisher,
                           @Value("${lifecycle.processing-delay-ms:0}") long processingDelayMs) {
        this.lifecycleService = lifecycleService;
        this.eventPublisher = eventPublisher;
        this.processingDelayMs = processingDelayMs;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SUBMIT_REVIEW)
    public void handleSubmitForReview(SubmitQuestionForReviewCommand command) {
        log.info("[LifecycleService] Recibido comando SubmitQuestionForReview: pregunta={}, operación={}",
                command.questionId(), command.operationId());

        if (command.operationId() == null || command.questionId() == null) {
            // Mensaje mal formado: no tiene sentido reintentarlo, va directo a la DLQ
            throw new AmqpRejectAndDontRequeueException("Comando inválido: falta operationId o questionId");
        }

        simulateProcessingTime();

        // Procesa la transición de estado de forma asíncrona: DRAFT -> PENDING_REVIEW
        OperationResult result = lifecycleService.processSubmitCommand(command);

        // Informa el resultado (éxito o fallo) para que el cliente pueda consultarlo después
        eventPublisher.publishOperationResult(result);
        if (result.getStatus() == OperationStatus.COMPLETED) {
            eventPublisher.publishQuestionSubmitted(result, command.requestedBy());
        }
    }

    private void simulateProcessingTime() {
        if (processingDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(processingDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
