package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Resultado del procesamiento de un comando asíncrono. Su clave es el operationId generado por
 * Question Service, lo que además sirve para descartar mensajes duplicados (idempotencia).
 */
@Entity
@Table(name = "operation_results")
public class OperationResult {

    @Id
    @Column(length = 64)
    private String operationId;

    private Long questionId;

    @Column(name = "operation_type", length = 40)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private QuestionStatus resultingState;

    @Column(length = 1000)
    private String message;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    protected OperationResult() {
    }

    public OperationResult(String operationId, Long questionId, String type, LocalDateTime createdAt) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.status = OperationStatus.PENDING;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public void complete(QuestionStatus resultingState, String message) {
        this.status = OperationStatus.COMPLETED;
        this.resultingState = resultingState;
        this.message = message;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(QuestionStatus currentState, String message) {
        this.status = OperationStatus.FAILED;
        this.resultingState = currentState;
        this.message = message;
        this.completedAt = LocalDateTime.now();
    }

    public String getOperationId() { return operationId; }
    public Long getQuestionId() { return questionId; }
    public String getType() { return type; }
    public OperationStatus getStatus() { return status; }
    public QuestionStatus getResultingState() { return resultingState; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
