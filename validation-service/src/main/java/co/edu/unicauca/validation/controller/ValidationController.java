package co.edu.unicauca.validation.controller;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.service.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Comunicación síncrona: POST /validate.
 * Siempre responde 200 con el resultado (valid=true/false). Question Service decide
 * qué hacer con él (201 o 400 hacia el cliente).
 */
@RestController
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ValidationResultDTO validate(@RequestBody ValidationRequestDTO request) {
        return validationService.validate(request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationResultDTO> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationResultDTO(false, List.of("El cuerpo de la solicitud no es un JSON válido"), List.of()));
    }
}
