package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.messaging.message.OperationResultEvent;
import co.edu.unicauca.question.service.OperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Recibe de forma asíncrona el resultado de las operaciones procesadas por Lifecycle Service. */
@Component
public class OperationResultListener {

    private static final Logger log = LoggerFactory.getLogger(OperationResultListener.class);

    private final OperationService operationService;

    public OperationResultListener(OperationService operationService) {
        this.operationService = operationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_OPERATION_RESULT)
    public void onOperationResult(OperationResultEvent event) {
        log.info("[QuestionService] Resultado de operación recibido: {} -> {}", event.operationId(), event.status());
        operationService.applyResult(event.operationId(), event.questionId(), event.type(),
                event.status(), event.resultingState(), event.message());
    }
}
