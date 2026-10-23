package co.edu.unicauca.question.client.dto;

import java.util.Set;

/** Respuesta de User Service GET /users/{id}. */
public record UserDTO(Long id, String username, String fullName, String email, boolean active, Set<String> roles) {

    public boolean hasAnyRole(String... wanted) {
        if (roles == null) {
            return false;
        }
        for (String role : wanted) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
