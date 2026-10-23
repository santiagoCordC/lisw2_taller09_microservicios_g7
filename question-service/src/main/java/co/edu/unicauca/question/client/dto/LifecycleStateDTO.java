package co.edu.unicauca.question.client.dto;

import java.time.LocalDateTime;

/** Respuesta de Lifecycle Service (estado actual de la pregunta). */
public record LifecycleStateDTO(Long questionId,
                                String state,
                                String previousState,
                                LocalDateTime changedAt,
                                Long changedBy,
                                String comment) {
}
