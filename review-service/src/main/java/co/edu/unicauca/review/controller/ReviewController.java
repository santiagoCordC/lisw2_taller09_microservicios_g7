package co.edu.unicauca.review.controller;

import co.edu.unicauca.review.dto.AssignReviewerRequest;
import co.edu.unicauca.review.dto.EvaluationRequest;
import co.edu.unicauca.review.exception.ApiException;
import co.edu.unicauca.review.model.Review;
import co.edu.unicauca.review.model.ReviewAssignment;
import co.edu.unicauca.review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Identidad: encabezado X-User-Id con el id devuelto por /auth/login (sin tokens en esta versión).
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** Un ADMINISTRADOR asigna un revisor a una pregunta en PENDING_REVIEW. */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assign(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody AssignReviewerRequest request) {
        ReviewAssignment assignment = reviewService.assignReviewer(parseUserId(userId), request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Revisor asignado; la pregunta pasó a IN_REVIEW");
        body.put("assignment", assignment);
        body.put("notification", assignment.isNotificationPublished()
                ? "Evento ReviewerAssigned publicado; la notificación se procesa de forma asíncrona"
                : "No se pudo publicar el evento de notificación (la asignación sigue siendo válida)");
        return ResponseEntity.created(URI.create("/reviews/assignments/" + assignment.getId())).body(body);
    }

    /** El revisor asignado aprueba o rechaza (con motivo) la pregunta. */
    @PostMapping("/assignments/{assignmentId}/evaluate")
    public Map<String, Object> evaluate(
            @PathVariable Long assignmentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody EvaluationRequest request) {
        ReviewService.EvaluationResult result = reviewService.evaluate(parseUserId(userId), assignmentId, request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Evaluación registrada; la pregunta quedó en " + result.newState());
        body.put("questionState", result.newState());
        body.put("review", result.review());
        return body;
    }

    @GetMapping("/assignments")
    public List<ReviewAssignment> assignments(@RequestParam(required = false) Long questionId,
                                              @RequestParam(required = false) Long reviewerId) {
        return reviewService.findAssignments(questionId, reviewerId);
    }

    @GetMapping("/assignments/{assignmentId}")
    public ReviewAssignment assignment(@PathVariable Long assignmentId) {
        return reviewService.findAssignment(assignmentId);
    }

    @GetMapping
    public List<Review> reviews(@RequestParam(required = false) Long questionId) {
        return reviewService.findReviews(questionId);
    }

    private static Long parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identificador de usuario inválido: " + raw);
        }
    }
}
