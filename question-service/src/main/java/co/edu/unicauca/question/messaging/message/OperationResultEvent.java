package co.edu.unicauca.question.messaging.message;

import java.time.LocalDateTime;

/** «Event» publicado por Lifecycle Service con el resultado de la operación. */
public record OperationResultEvent(String operationId,
                                   Long questionId,
                                   String type,
                                   String status,
                                   String resultingState,
                                   String message,
                                   LocalDateTime completedAt) {
}
