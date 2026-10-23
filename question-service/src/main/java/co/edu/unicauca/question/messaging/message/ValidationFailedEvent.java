package co.edu.unicauca.question.messaging.message;

import java.time.LocalDateTime;
import java.util.List;

/** «Event» informativo: una pregunta no superó la validación (lo consume Notification Service). */
public record ValidationFailedEvent(String operationId,
                                   Long questionId,
                                   Long authorId,
                                   List<String> errors,
                                   LocalDateTime occurredAt) {
}
