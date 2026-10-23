package co.edu.unicauca.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Colas que consume Notification Service (cada una con su DLQ):
 * - notification.reviewer-assigned      ← reviewer.assigned          (Review Service)
 * - notification.review-completed       ← review.completed           (Review Service)
 * - notification.question-submitted     ← question.submitted         (Lifecycle Service)
 * - notification.validation-failed      ← question.validation-failed (Question Service)
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String DLX_NAME = "saberpro.dlx.exchange";

    public static final String QUEUE_REVIEWER_ASSIGNED = "notification.reviewer-assigned";
    public static final String QUEUE_REVIEW_COMPLETED = "notification.review-completed";
    public static final String QUEUE_QUESTION_SUBMITTED = "notification.question-submitted";
    public static final String QUEUE_VALIDATION_FAILED = "notification.validation-failed";

    public static final String RK_REVIEWER_ASSIGNED = "reviewer.assigned";
    public static final String RK_REVIEW_COMPLETED = "review.completed";
    public static final String RK_QUESTION_SUBMITTED = "question.submitted";
    public static final String RK_VALIDATION_FAILED = "question.validation-failed";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    /** Cola + DLQ + bindings para cada evento consumido. */
    @Bean
    public Declarables notificationTopology() {
        List<Declarable> all = new ArrayList<>();
        addQueueWithDlq(all, QUEUE_REVIEWER_ASSIGNED, RK_REVIEWER_ASSIGNED);
        addQueueWithDlq(all, QUEUE_REVIEW_COMPLETED, RK_REVIEW_COMPLETED);
        addQueueWithDlq(all, QUEUE_QUESTION_SUBMITTED, RK_QUESTION_SUBMITTED);
        addQueueWithDlq(all, QUEUE_VALIDATION_FAILED, RK_VALIDATION_FAILED);
        return new Declarables(all);
    }

    private static void addQueueWithDlq(List<Declarable> target, String queueName, String routingKey) {
        Queue queue = QueueBuilder.durable(queueName)
                .deadLetterExchange(DLX_NAME)
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
        Queue dlq = QueueBuilder.durable(queueName + ".dlq").build();
        target.add(queue);
        target.add(dlq);
        target.add(new Binding(queueName, Binding.DestinationType.QUEUE, EXCHANGE_NAME, routingKey, null));
        target.add(new Binding(queueName + ".dlq", Binding.DestinationType.QUEUE, DLX_NAME, queueName + ".dlq", null));
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
