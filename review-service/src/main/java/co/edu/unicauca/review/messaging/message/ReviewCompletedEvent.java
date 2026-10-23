package co.edu.unicauca.review.messaging.message;

import java.time.LocalDateTime;

/** «Event» publicado cuando el revisor aprueba o rechaza la pregunta (se notifica al autor). */
public record ReviewCompletedEvent(Long questionId,
                                   Long reviewerId,
                                   Long authorId,
                                   String result,
                                   String comments,
                                   LocalDateTime occurredAt) {
}
