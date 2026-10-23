package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionState;
import co.edu.unicauca.lifecycle.model.QuestionStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record StateResponse(Long questionId,
                            QuestionStatus state,
                            QuestionStatus previousState,
                            LocalDateTime changedAt,
                            Long changedBy,
                            String comment,
                            Set<QuestionStatus> allowedTransitions) {

    public static StateResponse from(QuestionState s, QuestionStatus previous) {
        return new StateResponse(s.getQuestionId(), s.getState(), previous, s.getChangedAt(),
                s.getChangedBy(), s.getComment(), s.getState().allowedTargets());
    }
}
