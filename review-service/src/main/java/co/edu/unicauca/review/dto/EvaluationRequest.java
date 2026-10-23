package co.edu.unicauca.review.dto;

/**
 * Evaluación del revisor (FormatoEvaluacion del UML).
 * Para aprobar deben cumplirse los tres criterios; para rechazar, el motivo (comments) es obligatorio.
 */
public record EvaluationRequest(Boolean approved,
                                String comments,
                                Boolean pedagogicalQuality,
                                Boolean technicalQuality,
                                Boolean structuralQuality) {

    public boolean allCriteriaMet() {
        return Boolean.TRUE.equals(pedagogicalQuality)
                && Boolean.TRUE.equals(technicalQuality)
                && Boolean.TRUE.equals(structuralQuality);
    }
}
