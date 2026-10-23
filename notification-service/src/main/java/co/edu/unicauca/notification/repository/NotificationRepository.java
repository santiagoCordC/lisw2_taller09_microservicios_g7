package co.edu.unicauca.notification.repository;

import co.edu.unicauca.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByIdDesc(Long recipientId);
    List<Notification> findByQuestionIdOrderByIdDesc(Long questionId);
    List<Notification> findAllByOrderByIdDesc();
}
