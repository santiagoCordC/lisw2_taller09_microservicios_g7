package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Historial (trazabilidad) de cambios de estado. Ver HistorialModificacion en el UML. */
@Entity
@Table(name = "state_transitions")
public class StateTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private QuestionStatus fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionStatus toState;

    private LocalDateTime changedAt;

    private Long changedBy;

    @Column(name = "transition_comment", length = 1000)
    private String comment;

    protected StateTransition() {
    }

    public StateTransition(Long questionId, QuestionStatus fromState, QuestionStatus toState, Long changedBy, String comment) {
        this.questionId = questionId;
        this.fromState = fromState;
        this.toState = toState;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public QuestionStatus getFromState() { return fromState; }
    public QuestionStatus getToState() { return toState; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getChangedBy() { return changedBy; }
    public String getComment() { return comment; }
}
