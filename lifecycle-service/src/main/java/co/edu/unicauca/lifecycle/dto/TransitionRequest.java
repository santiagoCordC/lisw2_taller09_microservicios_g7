package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

/**
 * Solicitud de transición. questionId es opcional cuando ya viene en la URL
 * (POST /lifecycle/{questionId}/transition).
 */
public record TransitionRequest(Long questionId, QuestionStatus targetState, Long requestedBy, String comment) {
}
