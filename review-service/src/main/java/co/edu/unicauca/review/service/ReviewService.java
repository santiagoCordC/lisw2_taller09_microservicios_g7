package co.edu.unicauca.review.service;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.QuestionClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.client.dto.LifecycleStateDTO;
import co.edu.unicauca.review.client.dto.QuestionDTO;
import co.edu.unicauca.review.client.dto.UserDTO;
import co.edu.unicauca.review.dto.AssignReviewerRequest;
import co.edu.unicauca.review.dto.EvaluationRequest;
import co.edu.unicauca.review.exception.ApiException;
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.messaging.message.ReviewCompletedEvent;
import co.edu.unicauca.review.messaging.message.ReviewerAssignedEvent;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.Review;
import co.edu.unicauca.review.model.ReviewAssignment;
import co.edu.unicauca.review.model.ReviewResult;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Asignación de revisores y registro de resultados.
 * Flujo de asignación (Figura 2, fase 2):
 *   verificar elegibilidad (REST) → registrar asignación PENDING → solicitar transición a Lifecycle (REST)
 *   → confirmar (ASSIGNED) o compensar (FAILED) → publicar ReviewerAssigned (RabbitMQ, no bloqueante).
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewAssignmentRepository assignmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final QuestionClient questionClient;
    private final LifecycleClient lifecycleClient;
    private final EventPublisher eventPublisher;

    public ReviewService(ReviewAssignmentRepository assignmentRepository, ReviewRepository reviewRepository,
                         UserClient userClient, QuestionClient questionClient, LifecycleClient lifecycleClient,
                         EventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.reviewRepository = reviewRepository;
        this.userClient = userClient;
        this.questionClient = questionClient;
        this.lifecycleClient = lifecycleClient;
        this.eventPublisher = eventPublisher;
    }

    // ------------------------------------------------------------------ asignación

    public ReviewAssignment assignReviewer(Long adminId, AssignReviewerRequest request) {
        UserDTO admin = requireUser(adminId);
        if (!admin.hasRole("ADMINISTRADOR")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo un ADMINISTRADOR puede asignar revisores");
        }
        if (request == null || request.questionId() == null || request.reviewerId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "questionId y reviewerId son obligatorios");
        }
        Long questionId = request.questionId();

        // 1. Elegibilidad de la pregunta
        QuestionDTO question = questionClient.findQuestion(questionId);
        if (question == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "La pregunta " + questionId + " no existe");
        }
        String state = lifecycleClient.getState(questionId).state();
        if (!"PENDING_REVIEW".equals(state)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La pregunta no es elegible para asignación: está en " + state + " (se requiere PENDING_REVIEW)");
        }
        if (assignmentRepository.existsByQuestionIdAndStatusIn(questionId,
                EnumSet.of(AssignmentStatus.PENDING, AssignmentStatus.ASSIGNED))) {
            throw new ApiException(HttpStatus.CONFLICT, "La pregunta ya tiene un revisor asignado");
        }

        // 2. Elegibilidad del revisor
        UserDTO reviewer = userClient.findUser(request.reviewerId());
        if (reviewer == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El revisor " + request.reviewerId() + " no existe");
        }
        if (!reviewer.active()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El revisor " + reviewer.username() + " está inactivo");
        }
        if (!reviewer.hasRole("REVISOR")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El usuario " + reviewer.username() + " no tiene el rol REVISOR");
        }
        if (reviewer.id().equals(question.authorId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El revisor no puede ser el autor de la pregunta");
        }

        // 3. Registrar la asignación (PENDING) y solicitar la transición a Lifecycle
        ReviewAssignment assignment = assignmentRepository.save(
                new ReviewAssignment(questionId, reviewer.id(), question.authorId(), admin.id()));
        try {
            requestTransition(questionId, "IN_REVIEW", admin.id(), "Revisor asignado: " + reviewer.username());
        } catch (ApiException ex) {
            // Compensación: la asignación no queda activa si el estado no cambió
            assignment.fail(ex.getMessage());
            assignmentRepository.save(assignment);
            log.warn("[ReviewService] Asignación {} anulada: {}", assignment.getId(), ex.getMessage());
            throw new ApiException(ex.getStatus(),
                    "No se pudo asignar el revisor (la asignación fue anulada): " + ex.getMessage());
        }
        assignment.confirm();
        assignmentRepository.save(assignment);

        // 4. Notificación asíncrona: no bloquea ni revierte la asignación si falla
        boolean published = eventPublisher.publishReviewerAssigned(new ReviewerAssignedEvent(
                assignment.getId(), questionId, reviewer.id(), reviewer.fullName(), reviewer.email(),
                admin.id(), LocalDateTime.now()));
        assignment.setNotificationPublished(published);
        return assignmentRepository.save(assignment);
    }

    // ------------------------------------------------------------------ evaluación

    public record EvaluationResult(Review review, String newState) {
    }

    public EvaluationResult evaluate(Long reviewerId, Long assignmentId, EvaluationRequest request) {
        if (reviewerId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debe autenticarse (encabezado X-User-Id)");
        }
        ReviewAssignment assignment = findAssignment(assignmentId);
        if (!assignment.getReviewerId().equals(reviewerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el revisor asignado puede evaluar esta pregunta");
        }
        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La asignación está en estado " + assignment.getStatus() + " y no admite evaluación");
        }
        if (request == null || request.approved() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe indicar si aprueba la pregunta (approved: true/false)");
        }
        boolean approve = request.approved();
        if (approve && !request.allCriteriaMet()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Para aprobar, la pregunta debe cumplir calidad pedagógica, técnica y estructural");
        }
        if (!approve && (request.comments() == null || request.comments().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Para rechazar debe registrar el motivo (comments)");
        }

        String target = approve ? "APPROVED" : "REJECTED";
        LifecycleStateDTO state = requestTransition(assignment.getQuestionId(), target, reviewerId, request.comments());

        Review review = reviewRepository.save(new Review(assignment,
                approve ? ReviewResult.APPROVED : ReviewResult.REJECTED, request.comments(),
                Boolean.TRUE.equals(request.pedagogicalQuality()),
                Boolean.TRUE.equals(request.technicalQuality()),
                Boolean.TRUE.equals(request.structuralQuality())));
        assignment.complete();
        assignmentRepository.save(assignment);

        eventPublisher.publishReviewCompleted(new ReviewCompletedEvent(assignment.getQuestionId(), reviewerId,
                assignment.getAuthorId(), target, request.comments(), LocalDateTime.now()));
        return new EvaluationResult(review, state.state());
    }

    // ------------------------------------------------------------------ consultas

    public ReviewAssignment findAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La asignación " + assignmentId + " no existe"));
    }

    public List<ReviewAssignment> findAssignments(Long questionId, Long reviewerId) {
        if (questionId != null) {
            return assignmentRepository.findByQuestionId(questionId);
        }
        if (reviewerId != null) {
            return assignmentRepository.findByReviewerId(reviewerId);
        }
        return assignmentRepository.findAll();
    }

    public List<Review> findReviews(Long questionId) {
        return questionId == null ? reviewRepository.findAll() : reviewRepository.findByQuestionIdOrderByIdAsc(questionId);
    }

    // ------------------------------------------------------------------ auxiliares

    private UserDTO requireUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debe autenticarse (encabezado X-User-Id)");
        }
        UserDTO user = userClient.findUser(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "El usuario " + userId + " no existe");
        }
        if (!user.active()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El usuario está inactivo");
        }
        return user;
    }

    /**
     * Solicita la transición a Lifecycle. Si la llamada falla por un problema técnico (timeout, 5xx),
     * la transición pudo haberse aplicado igualmente: se verifica el estado real antes de dar por fallida
     * la operación, para no dejar la asignación y el estado inconsistentes.
     */
    private LifecycleStateDTO requestTransition(Long questionId, String target, Long requestedBy, String comment) {
        try {
            return lifecycleClient.transition(questionId, target, requestedBy, comment);
        } catch (ApiException ex) {
            if (ex.getStatus().is5xxServerError()) {
                try {
                    LifecycleStateDTO actual = lifecycleClient.getState(questionId);
                    if (target.equals(actual.state())) {
                        log.warn("[ReviewService] La transición a {} sí se aplicó pese al error: {}", target, ex.getMessage());
                        return actual;
                    }
                } catch (ApiException ignored) {
                    // Lifecycle sigue sin responder: se mantiene el error original
                }
            }
            throw ex;
        }
    }
}
