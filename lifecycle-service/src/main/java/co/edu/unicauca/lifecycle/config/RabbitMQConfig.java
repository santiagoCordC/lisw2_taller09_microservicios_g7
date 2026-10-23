package co.edu.unicauca.lifecycle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ que consume/produce el Lifecycle Service.
 * Consume: comando SubmitQuestionForReview (question.submit).
 * Publica: OperationResult (question.operation-result) y QuestionSubmittedForReview (question.submitted).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String DLX_NAME = "saberpro.dlx.exchange";

    public static final String QUEUE_SUBMIT_REVIEW = "question.submit.review.queue";
    public static final String ROUTING_KEY_SUBMIT = "question.submit";

    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation-result";
    public static final String ROUTING_KEY_QUESTION_SUBMITTED = "question.submitted";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    /** Debe declararse con los MISMOS argumentos en Question Service (productor). */
    @Bean
    public Queue submitQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT_REVIEW)
                .deadLetterExchange(DLX_NAME)
                .deadLetterRoutingKey(QUEUE_SUBMIT_REVIEW + ".dlq")
                .build();
    }

    @Bean
    public Queue submitDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT_REVIEW + ".dlq").build();
    }

    @Bean
    public Binding bindingSubmit() {
        return BindingBuilder.bind(submitQueue()).to(exchange()).with(ROUTING_KEY_SUBMIT);
    }

    @Bean
    public Binding bindingSubmitDlq() {
        return BindingBuilder.bind(submitDeadLetterQueue()).to(deadLetterExchange())
                .with(QUEUE_SUBMIT_REVIEW + ".dlq");
    }

    /**
     * Serialización JSON. alwaysConvertToInferredType=true hace que el consumidor deserialice
     * al tipo del parámetro del @RabbitListener, sin depender del nombre de clase del productor
     * (cada microservicio tiene sus propias clases de mensaje).
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
