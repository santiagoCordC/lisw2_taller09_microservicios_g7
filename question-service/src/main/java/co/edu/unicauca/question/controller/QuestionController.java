package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.CreateQuestionRequest;
import co.edu.unicauca.question.dto.QuestionResponse;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Identidad del usuario: en esta versión del taller no hay tokens (JWT llega en el corte 3).
 * El cliente envía el id obtenido en /auth/login en el encabezado X-User-Id
 * (también se acepta "Authorization: Bearer {userId}").
 */
@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** Creación síncrona: valida con Validation Service y persiste en DRAFT (201) o responde 400. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateQuestionRequest request) {
        QuestionService.CreateResult result = questionService.create(
                CurrentUser.resolve(userIdHeader, authorization), request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Pregunta creada en estado " + result.question().state());
        body.put("question", result.question());
        body.put("warnings", result.warnings());
        return ResponseEntity.created(URI.create("/questions/" + result.question().id())).body(body);
    }

    /** Corrección de una pregunta en DRAFT o REJECTED (si estaba REJECTED vuelve a DRAFT). */
    @PutMapping("/{questionId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long questionId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateQuestionRequest request) {
        QuestionService.CreateResult result = questionService.update(
                CurrentUser.resolve(userIdHeader, authorization), questionId, request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Pregunta actualizada; estado actual " + result.question().state());
        body.put("question", result.question());
        body.put("warnings", result.warnings());
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public List<QuestionResponse> list(@RequestParam(required = false) Long authorId) {
        return questionService.list(authorId);
    }

    @GetMapping("/{questionId}")
    public QuestionResponse get(@PathVariable Long questionId) {
        return questionService.get(questionId);
    }

    /**
     * Envío asíncrono a revisión: publica SubmitQuestionForReview en RabbitMQ y responde
     * 202 Accepted de inmediato, sin esperar a que Lifecycle procese la transición.
     */
    @PostMapping("/{questionId}/submit")
    public ResponseEntity<Map<String, Object>> submitForReview(
            @PathVariable Long questionId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Operation operation = questionService.submitForReview(
                CurrentUser.resolve(userIdHeader, authorization), questionId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Solicitud de envío a revisión aceptada para procesamiento.");
        body.put("operationId", operation.getOperationId());
        body.put("questionId", questionId);
        body.put("status", "PROCESSING");
        body.put("statusUrl", "/operations/" + operation.getOperationId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create("/operations/" + operation.getOperationId()))
                .body(body);
    }
}
