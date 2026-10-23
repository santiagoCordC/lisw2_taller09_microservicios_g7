package co.edu.unicauca.lifecycle.controller;

import co.edu.unicauca.lifecycle.dto.OperationResultDTO;
import co.edu.unicauca.lifecycle.dto.RegisterQuestionRequest;
import co.edu.unicauca.lifecycle.dto.StateResponse;
import co.edu.unicauca.lifecycle.dto.TransitionRequest;
import co.edu.unicauca.lifecycle.exception.ApiException;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.model.StateTransition;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    /** Registro inicial (lo invoca Question Service al crear la pregunta). */
    @PostMapping("/questions")
    public ResponseEntity<StateResponse> register(@RequestBody RegisterQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lifecycleService.register(request.questionId(), request.requestedBy()));
    }

    @GetMapping("/{questionId}")
    public StateResponse getState(@PathVariable Long questionId) {
        return lifecycleService.getState(questionId);
    }

    @GetMapping("/{questionId}/history")
    public List<StateTransition> history(@PathVariable Long questionId) {
        return lifecycleService.getHistory(questionId);
    }

    /** Transición síncrona (Figura 2: POST /lifecycle/{questionId}/transition). */
    @PostMapping("/{questionId}/transition")
    public StateResponse transition(@PathVariable Long questionId, @RequestBody TransitionRequest request) {
        return lifecycleService.transition(questionId, request.targetState(), request.requestedBy(), request.comment());
    }

    /** Variante de la guía: POST /lifecycle/transition con el questionId en el cuerpo. */
    @PostMapping("/transition")
    public StateResponse transitionWithBody(@RequestBody TransitionRequest request) {
        if (request.questionId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "questionId es obligatorio");
        }
        return lifecycleService.transition(request.questionId(), request.targetState(), request.requestedBy(),
                request.comment());
    }

    /** Resultado registrado de una operación asíncrona. */
    @GetMapping("/operations/{operationId}")
    public OperationResultDTO operation(@PathVariable String operationId) {
        return lifecycleService.getOperation(operationId);
    }

    /** Documenta la máquina de estados. */
    @GetMapping("/transitions")
    public Map<QuestionStatus, Set<QuestionStatus>> allowedTransitions() {
        Map<QuestionStatus, Set<QuestionStatus>> map = new LinkedHashMap<>();
        for (QuestionStatus status : QuestionStatus.values()) {
            map.put(status, status.allowedTargets());
        }
        return map;
    }
}
