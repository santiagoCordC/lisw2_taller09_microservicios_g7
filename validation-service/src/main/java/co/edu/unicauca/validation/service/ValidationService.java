package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.config.ValidationRulesProperties;
import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reglas de calidad estructural (ver ValidadorEstructural en el UML):
 * contexto/enunciado, cantidad de opciones, una única clave, expresiones prohibidas,
 * longitud de las opciones.
 */
@Service
public class ValidationService {

    private final ValidationRulesProperties rules;

    public ValidationService(ValidationRulesProperties rules) {
        this.rules = rules;
    }

    public ValidationResultDTO validate(ValidationRequestDTO request) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (request == null) {
            return new ValidationResultDTO(false, List.of("La solicitud de validación está vacía"), List.of());
        }

        verifyStatement(request, errors);
        verifyOptions(request, errors, warnings);

        return new ValidationResultDTO(errors.isEmpty(), errors, warnings);
    }

    /** verificarContextoYEnunciado */
    private void verifyStatement(ValidationRequestDTO request, List<String> errors) {
        if (isBlank(request.statement())) {
            errors.add("El enunciado (statement) es obligatorio");
        } else if (request.statement().trim().length() < rules.getMinStatementLength()) {
            errors.add("El enunciado debe tener al menos " + rules.getMinStatementLength() + " caracteres");
        }
        if (isBlank(request.competence())) {
            errors.add("La competencia evaluada (competence) es obligatoria");
        }
        if (!isBlank(request.statement())) {
            String normalized = normalize(request.statement());
            for (String expression : rules.getProhibitedExpressions()) {
                if (normalized.contains(normalize(expression))) {
                    errors.add("El enunciado contiene una expresión no permitida: '" + expression + "'");
                }
            }
        }
    }

    /** verificarCantidadOpciones, clave única, expresiones prohibidas y longitud */
    private void verifyOptions(ValidationRequestDTO request, List<String> errors, List<String> warnings) {
        List<OptionDTO> options = request.options();
        if (options == null || options.isEmpty()) {
            errors.add("La pregunta debe tener " + rules.getOptionsCount() + " opciones de respuesta");
            return;
        }
        if (options.size() != rules.getOptionsCount()) {
            errors.add("La pregunta debe tener exactamente " + rules.getOptionsCount()
                    + " opciones de respuesta (tiene " + options.size() + ")");
        }

        Set<String> ids = new HashSet<>();
        Set<String> texts = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            OptionDTO option = options.get(i);
            String label = "Opción " + (i + 1);
            if (option == null) {
                errors.add(label + ": no puede ser nula");
                continue;
            }
            if (isBlank(option.id())) {
                errors.add(label + ": el identificador (id) es obligatorio");
            } else if (!ids.add(option.id().trim().toUpperCase(Locale.ROOT))) {
                errors.add(label + ": el identificador '" + option.id() + "' está repetido");
            }
            if (isBlank(option.text())) {
                errors.add(label + ": el texto es obligatorio");
                continue;
            }
            if (option.text().length() > rules.getMaxOptionLength()) {
                errors.add(label + ": supera la longitud máxima de " + rules.getMaxOptionLength() + " caracteres");
            }
            String normalized = normalize(option.text());
            if (!texts.add(normalized)) {
                errors.add(label + ": el texto está duplicado con otra opción");
            }
            for (String expression : rules.getProhibitedExpressions()) {
                if (normalized.contains(normalize(expression))) {
                    errors.add(label + ": contiene la expresión prohibida '" + expression + "'");
                }
            }
        }

        // Exactamente una respuesta correcta (la clave) que exista entre las opciones
        if (isBlank(request.correctOptionId())) {
            errors.add("Debe indicar la respuesta correcta (correctOptionId)");
        } else if (!ids.contains(request.correctOptionId().trim().toUpperCase(Locale.ROOT))) {
            errors.add("La respuesta correcta '" + request.correctOptionId() + "' no corresponde a ninguna opción");
        } else {
            verifyLengthBalance(request, options, warnings);
        }
    }

    /** verificarLongitudYGramatica: la clave no debe "delatarse" por ser mucho más larga. */
    private void verifyLengthBalance(ValidationRequestDTO request, List<OptionDTO> options, List<String> warnings) {
        String key = request.correctOptionId().trim();
        int keyLength = 0;
        int distractorTotal = 0;
        int distractorCount = 0;
        for (OptionDTO option : options) {
            if (option == null || isBlank(option.text()) || isBlank(option.id())) {
                continue;
            }
            if (option.id().trim().equalsIgnoreCase(key)) {
                keyLength = option.text().trim().length();
            } else {
                distractorTotal += option.text().trim().length();
                distractorCount++;
            }
        }
        if (distractorCount > 0) {
            double average = (double) distractorTotal / distractorCount;
            if (average > 0 && keyLength > average * rules.getLengthWarningRatio()) {
                warnings.add("La respuesta correcta es considerablemente más larga que los distractores; "
                        + "esto puede delatar la clave");
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalize(String value) {
        String noAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
