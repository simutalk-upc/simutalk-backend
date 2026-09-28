package pe.upc.simutalk.shared.interfaces.acl;

import java.util.List;

/**
 * Contract other bounded contexts (recruitment) use to ask interviews about interview
 * scripts. Implemented by interviews.
 */
public interface InterviewsContextFacade {

    /** Number of interview questions that evaluate the given criterion. */
    long countQuestionsByCriterionId(Long criterionId);

    /** @return the InterviewSessionStatus name, or an empty string if the session does not exist */
    String fetchSessionStatus(Long interviewSessionId);

    /** @return the application id of the session, or {@code 0L} */
    Long fetchApplicationIdBySessionId(Long interviewSessionId);

    /** @return the job posting id of the session, or {@code 0L} */
    Long fetchJobPostingIdBySessionId(Long interviewSessionId);

    /** @return the candidate profile id of the session, or {@code 0L} */
    Long fetchCandidateIdBySessionId(Long interviewSessionId);

    /** Answers of the session with the criterion each question evaluates; empty if the session does not exist. */
    List<InterviewAnswerView> fetchAnswers(Long interviewSessionId);
}
