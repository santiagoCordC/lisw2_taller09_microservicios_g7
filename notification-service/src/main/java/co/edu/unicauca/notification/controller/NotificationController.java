package co.edu.unicauca.notification.controller;

import co.edu.unicauca.notification.model.Notification;
import co.edu.unicauca.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Solo lectura: permite evidenciar en Postman las notificaciones procesadas de forma asíncrona. */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> list(@RequestParam(required = false) Long recipientId,
                                   @RequestParam(required = false) Long questionId) {
        return notificationService.find(recipientId, questionId);
    }
}
