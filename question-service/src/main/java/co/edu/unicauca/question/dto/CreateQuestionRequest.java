package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.Difficulty;

import java.util.List;

/**
 * Cuerpo de POST /questions y PUT /questions/{id}.
 * Las reglas de estructura (enunciado, 4 opciones, una clave...) las aplica Validation Service.
 */
public record CreateQuestionRequest(
        String context,
        String statement,
        List<OptionDTO> options,
        String correctOptionId,
        String justification,
        String bibliography,
        String competence,
        String topic,
        String subtopic,
        Difficulty difficulty) {
}
