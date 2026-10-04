package pe.upc.simutalk.interviews.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.interviews.domain.model.aggregates.InterviewSession;
import pe.upc.simutalk.interviews.domain.model.entities.Answer;
import pe.upc.simutalk.interviews.domain.model.queries.GetAnswersByInterviewSessionIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByApplicationIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.HasInterviewSessionInProgressQuery;
import pe.upc.simutalk.interviews.domain.model.valueobjects.InterviewSessionStatus;
import pe.upc.simutalk.interviews.domain.services.InterviewSessionQueryService;
import pe.upc.simutalk.interviews.infrastructure.persistence.jpa.repositories.InterviewSessionRepository;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterviewSessionQueryServiceImpl implements InterviewSessionQueryService {

    private final InterviewSessionRepository interviewSessionRepository;

    @Override
    public Optional<InterviewSession> handle(GetInterviewSessionByIdQuery query) {
        return interviewSessionRepository.findWithAnswersById(query.interviewSessionId());
    }

    @Override
    public Optional<InterviewSession> handle(GetInterviewSessionByApplicationIdQuery query) {
        return interviewSessionRepository.findByApplicationId(query.applicationId());
    }

    @Override
    public List<Answer> handle(GetAnswersByInterviewSessionIdQuery query) {
        return interviewSessionRepository.findWithAnswersById(query.interviewSessionId())
                .map(InterviewSession::getAnswers)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session", query.interviewSessionId()));
    }

    @Override
    public boolean handle(HasInterviewSessionInProgressQuery query) {
        return query.jobPostingId() != null && query.candidateId() != null
                && interviewSessionRepository.existsByJobPostingIdAndCandidateIdAndStatus(
                query.jobPostingId(), query.candidateId(), InterviewSessionStatus.IN_PROGRESS);
    }
}
