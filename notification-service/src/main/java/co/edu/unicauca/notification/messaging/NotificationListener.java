package co.edu.unicauca.notification.messaging;

import co.edu.unicauca.notification.config.RabbitMQConfig;
import co.edu.unicauca.notification.messaging.message.QuestionSubmittedForReviewEvent;
import co.edu.unicauca.notification.messaging.message.ReviewCompletedEvent;
import co.edu.unicauca.notification.messaging.message.ReviewerAssignedEvent;
import co.edu.unicauca.notification.messaging.message.ValidationFailedEvent;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumidores asíncronos: ningún productor espera a que la notificación termine. */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEWER_ASSIGNED)
    public void onReviewerAssigned(ReviewerAssignedEvent event) {
        log.info("[NotificationService] ReviewerAssigned recibido: pregunta={}, revisor={}",
                event.questionId(), event.reviewerId());
        String recipient = event.reviewerName() + " <" + event.reviewerEmail() + ">";
        notificationService.process(event.reviewerId(), recipient, NotificationType.REVIEWER_ASSIGNED,
                event.questionId(),
                "Nueva pregunta asignada para revisión",
                "Hola " + event.reviewerName() + ", se le asignó la pregunta #" + event.questionId()
                        + " para revisión por pares (asignación #" + event.assignmentId() + ").");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEW_COMPLETED)
    public void onReviewCompleted(ReviewCompletedEvent event) {
        log.info("[NotificationService] ReviewCompleted recibido: pregunta={}, resultado={}",
                event.questionId(), event.result());
        boolean approved = "APPROVED".equals(event.result());
        String detail = event.comments() == null || event.comments().isBlank() ? "" : " Observaciones: " + event.comments();
        notificationService.process(event.authorId(), "Autor #" + event.authorId(),
                approved ? NotificationType.QUESTION_APPROVED : NotificationType.QUESTION_REJECTED,
                event.questionId(),
                approved ? "Su pregunta fue aprobada" : "Su pregunta fue rechazada",
                "La pregunta #" + event.questionId() + " fue " + (approved ? "APROBADA" : "RECHAZADA")
                        + " por el revisor #" + event.reviewerId() + "." + detail);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_QUESTION_SUBMITTED)
    public void onQuestionSubmitted(QuestionSubmittedForReviewEvent event) {
        log.info("[NotificationService] QuestionSubmittedForReview recibido: pregunta={}", event.questionId());
        notificationService.process(null, "Administradores", NotificationType.QUESTION_SUBMITTED,
                event.questionId(),
                "Pregunta pendiente de asignación de revisor",
                "La pregunta #" + event.questionId() + " fue enviada a revisión por el usuario #"
                        + event.requestedBy() + " y está en " + event.newState() + ".");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VALIDATION_FAILED)
    public void onValidationFailed(ValidationFailedEvent event) {
        log.info("[NotificationService] ValidationFailed recibido: autor={}", event.authorId());
        notificationService.process(event.authorId(), "Autor #" + event.authorId(), NotificationType.VALIDATION_FAILED,
                event.questionId(),
                "Su pregunta no superó la validación",
                "Errores encontrados: " + String.join("; ", event.errors() == null ? java.util.List.<String>of() : event.errors()));
    }
}
