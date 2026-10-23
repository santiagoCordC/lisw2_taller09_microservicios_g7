package co.edu.unicauca.user.service;

import co.edu.unicauca.user.dto.LoginRequestDTO;
import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.exception.ApiException;
import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.model.User;
import co.edu.unicauca.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    /**
     * Verifica las credenciales. Lanza 401 si son incorrectas y 403 si el usuario está inactivo.
     */
    public UserDTO authenticate(LoginRequestDTO request) {
        User user = repository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El usuario está inactivo");
        }
        return UserDTO.from(user);
    }

    public UserDTO findById(Long id) {
        return repository.findById(id)
                .map(UserDTO::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario " + id + " no encontrado"));
    }

    public List<UserDTO> findAll(Role role) {
        return repository.findAll().stream()
                .filter(u -> role == null || u.hasRole(role))
                .map(UserDTO::from)
                .toList();
    }
}
