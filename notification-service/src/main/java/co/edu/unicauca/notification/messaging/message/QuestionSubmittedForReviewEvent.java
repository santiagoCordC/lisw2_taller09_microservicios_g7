package co.edu.unicauca.notification.messaging.message;

import java.time.LocalDateTime;

public record QuestionSubmittedForReviewEvent(String operationId,
                                              Long questionId,
                                              String newState,
                                              Long requestedBy,
                                              LocalDateTime occurredAt) {
}
