package co.edu.unicauca.review.client.dto;

import java.util.Set;

public record UserDTO(Long id, String username, String fullName, String email, boolean active, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
