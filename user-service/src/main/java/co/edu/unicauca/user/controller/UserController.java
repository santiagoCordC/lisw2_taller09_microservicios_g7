package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.UserDTO;
import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consulta de usuarios y roles. La usan otros microservicios (Question, Review) de forma síncrona
 * para verificar que quien hace la petición tiene el rol requerido.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> list(@RequestParam(required = false) Role role) {
        return userService.findAll(role);
    }

    @GetMapping("/{id}")
    public UserDTO get(@PathVariable Long id) {
        return userService.findById(id);
    }
}
