package co.edu.unicauca.lifecycle.messaging.message;

import java.time.LocalDateTime;

/** «Event» publicado cuando una pregunta queda en PENDING_REVIEW. */
public record QuestionSubmittedForReviewEvent(String operationId,
                                              Long questionId,
                                              String newState,
                                              Long requestedBy,
                                              LocalDateTime occurredAt) {
}
