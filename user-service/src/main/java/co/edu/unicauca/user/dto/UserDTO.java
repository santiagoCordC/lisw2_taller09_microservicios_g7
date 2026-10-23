package co.edu.unicauca.user.dto;

import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.model.User;

import java.util.Set;
import java.util.TreeSet;

/** Datos básicos del usuario (nunca expone el hash de la contraseña). */
public record UserDTO(Long id, String username, String fullName, String email, boolean active, Set<Role> roles) {

    public static UserDTO from(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.isActive(),
                new TreeSet<>(u.getRoles()));
    }
}
