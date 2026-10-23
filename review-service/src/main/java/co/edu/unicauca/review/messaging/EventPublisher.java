package co.edu.unicauca.review.messaging;

import co.edu.unicauca.review.config.RabbitMQConfig;
import co.edu.unicauca.review.messaging.message.ReviewCompletedEvent;
import co.edu.unicauca.review.messaging.message.ReviewerAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos hacia Notification Service. La notificación NO debe bloquear la asignación:
 * si RabbitMQ falla, se registra el error y se devuelve false, pero la operación principal sigue.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean publishReviewerAssigned(ReviewerAssignedEvent event) {
        return send(RabbitMQConfig.ROUTING_KEY_REVIEWER_ASSIGNED, event);
    }

    public boolean publishReviewCompleted(ReviewCompletedEvent event) {
        return send(RabbitMQConfig.ROUTING_KEY_REVIEW_COMPLETED, event);
    }

    private boolean send(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("[ReviewService] Evento publicado ({}): {}", routingKey, event);
            return true;
        } catch (AmqpException ex) {
            log.error("[ReviewService] No se pudo publicar {}: {}", routingKey, ex.getMessage());
            return false;
        }
    }
}
