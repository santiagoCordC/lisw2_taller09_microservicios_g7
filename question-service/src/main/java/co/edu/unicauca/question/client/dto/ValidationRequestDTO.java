package co.edu.unicauca.question.client.dto;

import co.edu.unicauca.question.dto.OptionDTO;

import java.util.List;

/** Cuerpo enviado a Validation Service POST /validate. */
public record ValidationRequestDTO(String context,
                                   String statement,
                                   String competence,
                                   String topic,
                                   String subtopic,
                                   List<OptionDTO> options,
                                   String correctOptionId) {
}
