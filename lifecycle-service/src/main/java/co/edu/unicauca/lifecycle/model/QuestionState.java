package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Estado actual de una pregunta. Solo este servicio lo modifica. */
@Entity
@Table(name = "question_states")
public class QuestionState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionStatus state;

    private LocalDateTime changedAt;

    private Long changedBy;

    @Column(name = "state_comment", length = 1000)
    private String comment;

    /** Bloqueo optimista: evita que dos transiciones concurrentes se pisen. */
    @Version
    private Long version;

    protected QuestionState() {
    }

    public QuestionState(Long questionId, QuestionStatus state, Long changedBy, String comment) {
        this.questionId = questionId;
        this.state = state;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
    }

    public void applyTransition(QuestionStatus newState, Long changedBy, String comment) {
        this.state = newState;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public QuestionStatus getState() { return state; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getChangedBy() { return changedBy; }
    public String getComment() { return comment; }
}
