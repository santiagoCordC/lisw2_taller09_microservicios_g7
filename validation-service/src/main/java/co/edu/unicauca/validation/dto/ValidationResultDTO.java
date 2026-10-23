package co.edu.unicauca.validation.dto;

import java.util.List;

/**
 * Resultado de la validación. "errors" impide registrar la pregunta;
 * "warnings" son recomendaciones de calidad que no bloquean.
 */
public record ValidationResultDTO(boolean valid, List<String> errors, List<String> warnings) {
}
