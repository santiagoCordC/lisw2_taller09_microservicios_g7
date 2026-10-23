package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.messaging.message.ValidationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** Eventos informativos (best effort: un fallo aquí nunca rompe el flujo principal). */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishValidationFailed(Long questionId, Long authorId, List<String> errors) {
        ValidationFailedEvent event = new ValidationFailedEvent(null, questionId, authorId, errors, LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_VALIDATION_FAILED, event);
            log.info("[QuestionService] Evento ValidationFailed publicado para el autor {}", authorId);
        } catch (AmqpException ex) {
            log.warn("[QuestionService] No se pudo publicar ValidationFailed: {}", ex.getMessage());
        }
    }
}
