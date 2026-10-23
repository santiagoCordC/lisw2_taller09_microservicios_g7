package co.edu.unicauca.lifecycle.dto;

/** Registro inicial de una pregunta recién creada (queda en DRAFT). */
public record RegisterQuestionRequest(Long questionId, Long requestedBy) {
}
