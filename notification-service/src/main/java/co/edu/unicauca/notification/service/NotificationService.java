package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.model.Notification;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    /** Registra la notificación, la "envía" y deja constancia del resultado. */
    public Notification process(Long recipientId, String recipient, NotificationType type, Long questionId,
                                String subject, String message) {
        Notification notification = repository.save(
                new Notification(recipientId, recipient, type, questionId, subject, message));
        try {
            emailService.send(notification);
            notification.markAsSent();
        } catch (RuntimeException ex) {
            notification.markAsFailed();
            repository.save(notification);
            throw ex; // se reintenta según la configuración del listener; luego va a la DLQ
        }
        return repository.save(notification);
    }

    public List<Notification> find(Long recipientId, Long questionId) {
        if (recipientId != null) {
            return repository.findByRecipientIdOrderByIdDesc(recipientId);
        }
        if (questionId != null) {
            return repository.findByQuestionIdOrderByIdDesc(questionId);
        }
        return repository.findAllByOrderByIdDesc();
    }
}
