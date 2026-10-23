package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.messaging.message.OperationResultEvent;
import co.edu.unicauca.lifecycle.messaging.message.QuestionSubmittedForReviewEvent;
import co.edu.unicauca.lifecycle.model.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publica eventos de Lifecycle. Si RabbitMQ falla al publicar, el resultado ya quedó persistido
 * y Question Service puede consultarlo por REST (GET /lifecycle/operations/{id}).
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOperationResult(OperationResult result) {
        OperationResultEvent event = new OperationResultEvent(
                result.getOperationId(),
                result.getQuestionId(),
                result.getType(),
                result.getStatus().name(),
                result.getResultingState() == null ? null : result.getResultingState().name(),
                result.getMessage(),
                result.getCompletedAt());
        send(RabbitMQConfig.ROUTING_KEY_OPERATION_RESULT, event);
    }

    public void publishQuestionSubmitted(OperationResult result, Long requestedBy) {
        QuestionSubmittedForReviewEvent event = new QuestionSubmittedForReviewEvent(
                result.getOperationId(),
                result.getQuestionId(),
                result.getResultingState().name(),
                requestedBy,
                LocalDateTime.now());
        send(RabbitMQConfig.ROUTING_KEY_QUESTION_SUBMITTED, event);
    }

    private void send(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("[Lifecycle] Evento publicado ({}): {}", routingKey, event);
        } catch (AmqpException ex) {
            log.error("[Lifecycle] No se pudo publicar el evento {}: {}", routingKey, ex.getMessage());
        }
    }
}
