package co.edu.unicauca.validation.dto;

import java.util.List;

/** Contenido de la pregunta a validar (lo envía Question Service por REST). */
public record ValidationRequestDTO(
        String context,
        String statement,
        String competence,
        String topic,
        String subtopic,
        List<OptionDTO> options,
        String correctOptionId) {
}
