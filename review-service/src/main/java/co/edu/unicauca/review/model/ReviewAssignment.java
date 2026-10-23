package co.edu.unicauca.review.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_assignments")
public class ReviewAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Long reviewerId;

    private Long authorId;

    private Long assignedBy;

    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssignmentStatus status;

    @Column(length = 1000)
    private String failureReason;

    private boolean notificationPublished;

    protected ReviewAssignment() {
    }

    public ReviewAssignment(Long questionId, Long reviewerId, Long authorId, Long assignedBy) {
        this.questionId = questionId;
        this.reviewerId = reviewerId;
        this.authorId = authorId;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.status = AssignmentStatus.PENDING;
    }

    public void confirm() { this.status = AssignmentStatus.ASSIGNED; }

    public void fail(String reason) {
        this.status = AssignmentStatus.FAILED;
        this.failureReason = reason;
    }

    public void complete() { this.status = AssignmentStatus.COMPLETED; }

    public void setNotificationPublished(boolean notificationPublished) {
        this.notificationPublished = notificationPublished;
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public Long getReviewerId() { return reviewerId; }
    public Long getAuthorId() { return authorId; }
    public Long getAssignedBy() { return assignedBy; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public AssignmentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public boolean isNotificationPublished() { return notificationPublished; }
}
