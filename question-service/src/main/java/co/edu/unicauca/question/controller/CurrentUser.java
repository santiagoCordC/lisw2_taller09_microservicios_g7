package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Obtiene el id del usuario autenticado a partir de los encabezados de la petición. */
final class CurrentUser {

    private CurrentUser() {
    }

    /** @return el id del usuario, o null si no se envió ninguno de los encabezados. */
    static Long resolve(String xUserId, String authorization) {
        String raw = null;
        if (xUserId != null && !xUserId.isBlank()) {
            raw = xUserId.trim();
        } else if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            raw = authorization.substring(7).trim();
        }
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identificador de usuario inválido: " + raw);
        }
    }
}
