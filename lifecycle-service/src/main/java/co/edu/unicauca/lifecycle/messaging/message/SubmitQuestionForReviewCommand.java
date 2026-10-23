package co.edu.unicauca.lifecycle.messaging.message;

import java.time.LocalDateTime;

/** «Command» publicado por Question Service en la cola question.submit.review.queue. */
public record SubmitQuestionForReviewCommand(String operationId,
                                             Long questionId,
                                             Long requestedBy,
                                             LocalDateTime requestedAt) {
}
