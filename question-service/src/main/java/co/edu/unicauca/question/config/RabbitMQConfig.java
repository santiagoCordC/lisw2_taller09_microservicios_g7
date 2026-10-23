package co.edu.unicauca.question.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ del Question Service.
 * Publica: comando SubmitQuestionForReview (question.submit) y evento ValidationFailed.
 * Consume: OperationResult (question.operation-result) emitido por Lifecycle Service.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String DLX_NAME = "saberpro.dlx.exchange";

    public static final String QUEUE_SUBMIT_REVIEW = "question.submit.review.queue";
    public static final String ROUTING_KEY_SUBMIT = "question.submit";

    public static final String QUEUE_OPERATION_RESULT = "question.operation.result.queue";
    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation-result";

    public static final String ROUTING_KEY_VALIDATION_FAILED = "question.validation-failed";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    /**
     * La cola de comandos también se declara aquí (con los mismos argumentos que en Lifecycle)
     * para que el comando no se pierda si Lifecycle aún no ha arrancado: queda encolado.
     */
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

    @Bean
    public Queue operationResultQueue() {
        return QueueBuilder.durable(QUEUE_OPERATION_RESULT)
                .deadLetterExchange(DLX_NAME)
                .deadLetterRoutingKey(QUEUE_OPERATION_RESULT + ".dlq")
                .build();
    }

    @Bean
    public Queue operationResultDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_OPERATION_RESULT + ".dlq").build();
    }

    @Bean
    public Binding bindingOperationResult() {
        return BindingBuilder.bind(operationResultQueue()).to(exchange()).with(ROUTING_KEY_OPERATION_RESULT);
    }

    @Bean
    public Binding bindingOperationResultDlq() {
        return BindingBuilder.bind(operationResultDeadLetterQueue()).to(deadLetterExchange())
                .with(QUEUE_OPERATION_RESULT + ".dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
