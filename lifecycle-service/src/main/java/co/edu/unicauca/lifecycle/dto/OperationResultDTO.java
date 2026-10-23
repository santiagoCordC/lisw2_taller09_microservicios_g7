package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionStatus;

import java.time.LocalDateTime;

public record OperationResultDTO(String operationId,
                                 Long questionId,
                                 String type,
                                 OperationStatus status,
                                 QuestionStatus resultingState,
                                 String message,
                                 LocalDateTime createdAt,
                                 LocalDateTime completedAt) {

    public static OperationResultDTO from(OperationResult r) {
        return new OperationResultDTO(r.getOperationId(), r.getQuestionId(), r.getType(), r.getStatus(),
                r.getResultingState(), r.getMessage(), r.getCreatedAt(), r.getCompletedAt());
    }
}
