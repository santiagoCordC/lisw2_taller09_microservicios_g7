package co.edu.unicauca.review.messaging.message;

import java.time.LocalDateTime;

/**
 * «Event» ReviewerAssigned. Lleva los datos de contacto del revisor (event-carried state transfer)
 * para que Notification Service no tenga que consultar a User Service.
 */
public record ReviewerAssignedEvent(Long assignmentId,
                                    Long questionId,
                                    Long reviewerId,
                                    String reviewerName,
                                    String reviewerEmail,
                                    Long assignedBy,
                                    LocalDateTime occurredAt) {
}
