package pe.upc.simutalk.interviews.domain.services;

import pe.upc.simutalk.interviews.domain.model.aggregates.InterviewSession;
import pe.upc.simutalk.interviews.domain.model.entities.Answer;
import pe.upc.simutalk.interviews.domain.model.queries.GetAnswersByInterviewSessionIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByApplicationIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.HasInterviewSessionInProgressQuery;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionQueryService {

    Optional<InterviewSession> handle(GetInterviewSessionByIdQuery query);

    Optional<InterviewSession> handle(GetInterviewSessionByApplicationIdQuery query);

    /**
     * @throws pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException if the session does not exist
     */
    List<Answer> handle(GetAnswersByInterviewSessionIdQuery query);

    /** Whether the candidate has an IN_PROGRESS session for the job posting. */
    boolean handle(HasInterviewSessionInProgressQuery query);
}
