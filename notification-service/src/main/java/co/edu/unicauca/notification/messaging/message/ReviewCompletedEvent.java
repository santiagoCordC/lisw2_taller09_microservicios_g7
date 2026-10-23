package co.edu.unicauca.notification.messaging.message;

import java.time.LocalDateTime;

public record ReviewCompletedEvent(Long questionId,
                                   Long reviewerId,
                                   Long authorId,
                                   String result,
                                   String comments,
                                   LocalDateTime occurredAt) {
}
