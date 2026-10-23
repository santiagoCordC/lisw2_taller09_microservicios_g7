package co.edu.unicauca.review.client.dto;

import java.time.LocalDateTime;

public record LifecycleStateDTO(Long questionId,
                                String state,
                                String previousState,
                                LocalDateTime changedAt,
                                Long changedBy,
                                String comment) {
}
