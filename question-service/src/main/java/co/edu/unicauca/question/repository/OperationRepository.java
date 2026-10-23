package co.edu.unicauca.question.repository;

import co.edu.unicauca.question.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationRepository extends JpaRepository<Operation, String> {
    List<Operation> findByQuestionIdOrderByCreatedAtDesc(Long questionId);
}
