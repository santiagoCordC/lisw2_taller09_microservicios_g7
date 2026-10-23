package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.LoginRequestDTO;
import co.edu.unicauca.user.dto.LoginResponseDTO;
import co.edu.unicauca.user.exception.ApiException;
import co.edu.unicauca.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Comunicación síncrona: POST /auth/login. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(LoginResponseDTO.ok(userService.authenticate(request)));
        } catch (ApiException ex) {
            // 401 Unauthorized (credenciales) o 403 Forbidden (usuario inactivo), con cuerpo JSON uniforme
            return ResponseEntity.status(ex.getStatus()).body(LoginResponseDTO.rejected(ex.getMessage()));
        }
    }
}
