package co.edu.unicauca.question.messaging.message;

import java.time.LocalDateTime;

/** «Command» dirigido a Lifecycle Service: solicitar DRAFT → PENDING_REVIEW. */
public record SubmitQuestionForReviewCommand(String operationId,
                                             Long questionId,
                                             Long requestedBy,
                                             LocalDateTime requestedAt) {
}
