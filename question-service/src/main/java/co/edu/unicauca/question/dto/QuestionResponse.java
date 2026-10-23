package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.Difficulty;
import co.edu.unicauca.question.model.Question;

import java.time.LocalDateTime;
import java.util.List;

/** Representación de la pregunta. "state" se obtiene de Lifecycle Service. */
public record QuestionResponse(
        Long id,
        String context,
        String statement,
        List<OptionDTO> options,
        String correctOptionId,
        String justification,
        String bibliography,
        String competence,
        String topic,
        String subtopic,
        Difficulty difficulty,
        Long authorId,
        String state,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static QuestionResponse from(Question q, String state) {
        List<OptionDTO> options = q.getOptions().stream()
                .map(o -> new OptionDTO(o.getOptionId(), o.getText()))
                .toList();
        return new QuestionResponse(q.getId(), q.getContext(), q.getStatement(), options, q.getCorrectOptionId(),
                q.getJustification(), q.getBibliography(), q.getCompetence(), q.getTopic(), q.getSubtopic(),
                q.getDifficulty(), q.getAuthorId(), state, q.getCreatedAt(), q.getUpdatedAt());
    }
}
