package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.OperationStatus;

import java.time.LocalDateTime;

public record OperationResponse(String operationId,
                                Long questionId,
                                String type,
                                Long requestedBy,
                                OperationStatus status,
                                String resultingState,
                                String message,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {

    public static OperationResponse from(Operation o) {
        return new OperationResponse(o.getOperationId(), o.getQuestionId(), o.getType(), o.getRequestedBy(),
                o.getStatus(), o.getResultingState(), o.getMessage(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
