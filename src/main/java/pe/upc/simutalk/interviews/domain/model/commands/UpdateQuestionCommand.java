package pe.upc.simutalk.interviews.domain.model.commands;

import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionOrigin;

/** The position changes only through ReorderQuestionsCommand. */
public record UpdateQuestionCommand(Long jobPostingId, Long questionId, Long criterionId, String statement,
                                    int maxDurationSeconds, QuestionOrigin origin, boolean allowsFollowUp) {
}
