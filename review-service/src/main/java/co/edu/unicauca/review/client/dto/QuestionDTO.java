package co.edu.unicauca.review.client.dto;

/** Subconjunto de la respuesta de Question Service que necesita Review Service. */
public record QuestionDTO(Long id, String statement, Long authorId, String state) {
}
