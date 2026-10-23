package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.messaging.message.SubmitQuestionForReviewCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publicación de comandos con RabbitTemplate. */
@Component
public class CommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);
    private static final long CONFIRM_TIMEOUT_MS = 5000;

    private final RabbitTemplate rabbitTemplate;

    public CommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el comando y espera la confirmación (publisher confirm) del broker.
     * Si RabbitMQ no está disponible o no confirma, lanza AmqpException y el controlador
     * NO responde 202 (no se acepta una solicitud que no quedó encolada).
     */
    public void publishSubmitForReview(SubmitQuestionForReviewCommand command) {
        rabbitTemplate.invoke(operations -> {
            operations.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_SUBMIT, command);
            operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
            return null;
        });
        log.info("[QuestionService] Comando SubmitQuestionForReview publicado: {}", command);
    }
}
