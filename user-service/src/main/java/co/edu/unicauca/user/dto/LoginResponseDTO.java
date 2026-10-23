package co.edu.unicauca.user.dto;

/**
 * Respuesta de POST /auth/login.
 * En esta versión del taller NO se emiten tokens (JWT llega en el corte 3).
 */
public record LoginResponseDTO(boolean authenticated, UserDTO user, String message) {

    public static LoginResponseDTO ok(UserDTO user) {
        return new LoginResponseDTO(true, user, "Autenticación exitosa");
    }

    public static LoginResponseDTO rejected(String message) {
        return new LoginResponseDTO(false, null, message);
    }
}
