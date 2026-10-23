package co.edu.unicauca.notification.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null = notificación dirigida a un grupo (p. ej. administradores). */
    private Long recipientId;

    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 30)
    private NotificationType type;

    private Long questionId;

    private String subject;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationStatus status;

    private LocalDateTime receivedAt;

    private LocalDateTime sentAt;

    protected Notification() {
    }

    public Notification(Long recipientId, String recipient, NotificationType type, Long questionId,
                        String subject, String message) {
        this.recipientId = recipientId;
        this.recipient = recipient;
        this.type = type;
        this.questionId = questionId;
        this.subject = subject;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.receivedAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public Long getId() { return id; }
    public Long getRecipientId() { return recipientId; }
    public String getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public Long getQuestionId() { return questionId; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public NotificationStatus getStatus() { return status; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public LocalDateTime getSentAt() { return sentAt; }
}
