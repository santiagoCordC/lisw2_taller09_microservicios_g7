package co.edu.unicauca.lifecycle.messaging.message;

import java.time.LocalDateTime;

/** «Event» con el resultado de una operación asíncrona (COMPLETED / FAILED). */
public record OperationResultEvent(String operationId,
                                   Long questionId,
                                   String type,
                                   String status,
                                   String resultingState,
                                   String message,
                                   LocalDateTime completedAt) {
}
