package co.edu.unicauca.lifecycle.repository;

import co.edu.unicauca.lifecycle.model.StateTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateTransitionRepository extends JpaRepository<StateTransition, Long> {
    List<StateTransition> findByQuestionIdOrderByIdAsc(Long questionId);
}
