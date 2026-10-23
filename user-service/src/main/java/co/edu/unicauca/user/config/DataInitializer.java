package co.edu.unicauca.user.config;

import co.edu.unicauca.user.model.Role;
import co.edu.unicauca.user.model.User;
import co.edu.unicauca.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Usuarios de prueba (se recrean en cada arranque porque la BD H2 es en memoria). */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new User("admin", encoder.encode("admin123"), "Ana Administradora",
                "admin@unicauca.edu.co", true, Role.ADMINISTRADOR));
        repository.save(new User("docente", encoder.encode("docente123"), "Diego Docente",
                "docente@unicauca.edu.co", true, Role.DOCENTE, Role.AUTOR));
        repository.save(new User("revisor", encoder.encode("revisor123"), "Rosa Revisora",
                "revisor@unicauca.edu.co", true, Role.REVISOR));
        repository.save(new User("revisor2", encoder.encode("revisor123"), "Raúl Revisor",
                "revisor2@unicauca.edu.co", true, Role.REVISOR, Role.DOCENTE));
        repository.save(new User("estudiante", encoder.encode("estudiante123"), "Esteban Estudiante",
                "estudiante@unicauca.edu.co", true, Role.ESTUDIANTE));
        repository.save(new User("inactivo", encoder.encode("inactivo123"), "Usuario Inactivo",
                "inactivo@unicauca.edu.co", false, Role.DOCENTE));
        log.info("Usuarios de prueba creados: admin, docente, revisor, revisor2, estudiante, inactivo");
    }
}
