package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.OperationResponse;
import co.edu.unicauca.question.service.OperationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consulta posterior del resultado de las operaciones asíncronas
 * (Figura 2: GET /operations/{operationId}, "consultar hasta completar").
 */
@RestController
@RequestMapping("/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/{operationId}")
    public OperationResponse get(@PathVariable String operationId) {
        return OperationResponse.from(operationService.get(operationId));
    }

    @GetMapping
    public List<OperationResponse> byQuestion(@RequestParam Long questionId) {
        return operationService.findByQuestion(questionId).stream().map(OperationResponse::from).toList();
    }
}
