package pe.upc.simutalk.interviews.domain.model.commands;

/**
 * @param parentAnswerId only for follow-ups: the answer that originated the follow-up
 */
public record RecordAnswerCommand(Long interviewSessionId, Long questionId, String transcript, String audioUrl,
                                  int durationSeconds, boolean followUp, Long parentAnswerId) {
}
