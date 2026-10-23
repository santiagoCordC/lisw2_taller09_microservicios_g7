package co.edu.unicauca.question.client.dto;

/** Respuesta de Lifecycle Service GET /lifecycle/operations/{operationId}. */
public record LifecycleOperationDTO(String operationId,
                                    Long questionId,
                                    String type,
                                    String status,
                                    String resultingState,
                                    String message) {
}
