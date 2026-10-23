package co.edu.unicauca.question.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/** Excepción de negocio que se traduce directamente a un código HTTP. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> errors;

    public ApiException(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    public ApiException(HttpStatus status, String message, List<String> errors) {
        super(message);
        this.status = status;
        this.errors = errors == null ? List.of() : errors;
    }

    public HttpStatus getStatus() { return status; }
    public List<String> getErrors() { return errors; }
}
