package co.edu.unicauca.review.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Review Service solo PUBLICA eventos (ReviewerAssigned, ReviewCompleted).
 * Las colas las declara su consumidor (Notification Service).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";
    public static final String ROUTING_KEY_REVIEWER_ASSIGNED = "reviewer.assigned";
    public static final String ROUTING_KEY_REVIEW_COMPLETED = "review.completed";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
