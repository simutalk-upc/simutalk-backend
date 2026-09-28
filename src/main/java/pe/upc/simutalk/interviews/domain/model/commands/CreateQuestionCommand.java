package pe.upc.simutalk.interviews.domain.model.commands;

import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionOrigin;

/** The new question is appended at the end of the script. */
public record CreateQuestionCommand(Long jobPostingId, Long criterionId, String statement, int maxDurationSeconds,
                                    QuestionOrigin origin, boolean allowsFollowUp) {
}
