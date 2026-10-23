package co.edu.unicauca.notification.messaging.message;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationFailedEvent(String operationId,
                                    Long questionId,
                                    Long authorId,
                                    List<String> errors,
                                    LocalDateTime occurredAt) {
}
