package co.edu.unicauca.notification.messaging.message;

import java.time.LocalDateTime;

public record ReviewerAssignedEvent(Long assignmentId,
                                    Long questionId,
                                    Long reviewerId,
                                    String reviewerName,
                                    String reviewerEmail,
                                    Long assignedBy,
                                    LocalDateTime occurredAt) {
}
