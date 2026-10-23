package co.edu.unicauca.lifecycle.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de la pregunta y su máquina de transiciones permitidas.
 * <pre>
 * DRAFT ──submit──> PENDING_REVIEW ──asignar revisor──> IN_REVIEW ──┬─> APPROVED ──> PUBLISHED ──> ARCHIVED
 *   ^                                                               └─> REJECTED
 *   └────────────────────── corrección (REJECTED → DRAFT) ──────────────────┘
 * </pre>
 */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    ARCHIVED;

    public Set<QuestionStatus> allowedTargets() {
        return switch (this) {
            case DRAFT -> EnumSet.of(PENDING_REVIEW, ARCHIVED);
            case PENDING_REVIEW -> EnumSet.of(IN_REVIEW);
            case IN_REVIEW -> EnumSet.of(APPROVED, REJECTED);
            case REJECTED -> EnumSet.of(DRAFT, ARCHIVED);
            case APPROVED -> EnumSet.of(PUBLISHED, ARCHIVED);
            case PUBLISHED -> EnumSet.of(ARCHIVED);
            case ARCHIVED -> EnumSet.noneOf(QuestionStatus.class);
        };
    }

    public boolean canTransitionTo(QuestionStatus target) {
        return target != null && allowedTargets().contains(target);
    }
}
