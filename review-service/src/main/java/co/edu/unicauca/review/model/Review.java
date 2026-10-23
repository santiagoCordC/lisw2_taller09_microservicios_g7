package co.edu.unicauca.review.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Resultado de la revisión por pares (UML: Revision + FormatoEvaluacion). */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long assignmentId;
    private Long questionId;
    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", length = 20)
    private ReviewResult result;

    @Column(length = 2000)
    private String comments;

    // FormatoEvaluacion
    private boolean pedagogicalQuality;
    private boolean technicalQuality;
    private boolean structuralQuality;

    private LocalDateTime createdAt;

    protected Review() {
    }

    public Review(ReviewAssignment assignment, ReviewResult result, String comments,
                  boolean pedagogicalQuality, boolean technicalQuality, boolean structuralQuality) {
        this.assignmentId = assignment.getId();
        this.questionId = assignment.getQuestionId();
        this.reviewerId = assignment.getReviewerId();
        this.result = result;
        this.comments = comments;
        this.pedagogicalQuality = pedagogicalQuality;
        this.technicalQuality = technicalQuality;
        this.structuralQuality = structuralQuality;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getAssignmentId() { return assignmentId; }
    public Long getQuestionId() { return questionId; }
    public Long getReviewerId() { return reviewerId; }
    public ReviewResult getResult() { return result; }
    public String getComments() { return comments; }
    public boolean isPedagogicalQuality() { return pedagogicalQuality; }
    public boolean isTechnicalQuality() { return technicalQuality; }
    public boolean isStructuralQuality() { return structuralQuality; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
