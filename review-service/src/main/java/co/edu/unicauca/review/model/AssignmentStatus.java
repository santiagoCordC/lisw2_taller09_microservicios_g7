package co.edu.unicauca.review.model;

/**
 * PENDING: registrada localmente, esperando la transición en Lifecycle.
 * ASSIGNED: Lifecycle confirmó IN_REVIEW; el revisor puede evaluar.
 * FAILED: la transición falló; la asignación se anula (compensación) para evitar inconsistencias.
 * COMPLETED: el revisor registró su evaluación.
 */
public enum AssignmentStatus {
    PENDING,
    ASSIGNED,
    FAILED,
    COMPLETED
}
