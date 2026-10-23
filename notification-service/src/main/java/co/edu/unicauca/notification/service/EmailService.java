package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Simulación de envío diferido de correo (no hay servidor SMTP real). */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final long simulatedDelayMs;

    public EmailService(@Value("${notification.simulated-send-delay-ms:2000}") long simulatedDelayMs) {
        this.simulatedDelayMs = simulatedDelayMs;
    }

    public void send(Notification notification) {
        try {
            Thread.sleep(simulatedDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("""

                ==================== CORREO SIMULADO ====================
                Para:    {}
                Asunto:  {}
                Mensaje: {}
                =========================================================""",
                notification.getRecipient(), notification.getSubject(), notification.getMessage());
    }
}
