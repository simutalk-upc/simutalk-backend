package pe.upc.simutalk.shared.interfaces.acl;

import pe.upc.simutalk.services.InterviewsContextFacade;

/**
 * Read-only view of an interview answer, shared through {@link InterviewsContextFacade}.
 *
 * @param criterionId criterion evaluated by the question that was answered
 */
public record InterviewAnswerView(Long answerId, Long questionId, Long criterionId, String transcript, boolean followUp) {
}
