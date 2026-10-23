package co.edu.unicauca.question.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Seguimiento local de una operación asíncrona (patrón "202 Accepted + consulta posterior").
 * Nace PENDING y pasa a COMPLETED o FAILED cuando llega el evento OperationResult.
 */
@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @Column(length = 64)
    private String operationId;

    private Long questionId;

    @Column(name = "operation_type", length = 40)
    private String type;

    private Long requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationStatus status;

    @Column(length = 30)
    private String resultingState;

    @Column(length = 1000)
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Operation() {
    }

    public Operation(String operationId, Long questionId, String type, Long requestedBy) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.requestedBy = requestedBy;
        this.status = OperationStatus.PENDING;
        this.message = "Solicitud aceptada; en espera de procesamiento por Lifecycle Service";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public boolean isFinished() {
        return status != OperationStatus.PENDING;
    }

    public void finish(OperationStatus status, String resultingState, String message) {
        this.status = status;
        this.resultingState = resultingState;
        this.message = message;
        this.updatedAt = LocalDateTime.now();
    }

    public String getOperationId() { return operationId; }
    public Long getQuestionId() { return questionId; }
    public String getType() { return type; }
    public Long getRequestedBy() { return requestedBy; }
    public OperationStatus getStatus() { return status; }
    public String getResultingState() { return resultingState; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
