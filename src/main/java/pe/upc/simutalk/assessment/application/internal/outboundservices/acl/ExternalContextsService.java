package pe.upc.simutalk.assessment.application.internal.outboundservices.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.assessment.domain.model.valueobjects.InterviewSessionSnapshot;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;
import pe.upc.simutalk.shared.interfaces.acl.*;

import java.util.List;
import java.util.Optional;

/**
 * Anti-corruption layer from assessment to interviews, recruitment and profiles, through the
 * contracts in shared. assessment never imports classes of those contexts.
 */
@Service("assessmentExternalContextsService")
@RequiredArgsConstructor
public class ExternalContextsService {

    private final InterviewsContextFacade interviewsContextFacade;
    private final RecruitmentContextFacade recruitmentContextFacade;
    private final ProfilesContextFacade profilesContextFacade;

    public InterviewSessionSnapshot fetchSession(Long interviewSessionId) {
        var status = interviewsContextFacade.fetchSessionStatus(interviewSessionId);
        if (status.isEmpty()) {
            throw new ResourceNotFoundException("Interview session", interviewSessionId);
        }
        return new InterviewSessionSnapshot(interviewSessionId,
                interviewsContextFacade.fetchApplicationIdBySessionId(interviewSessionId),
                interviewsContextFacade.fetchJobPostingIdBySessionId(interviewSessionId),
                interviewsContextFacade.fetchCandidateIdBySessionId(interviewSessionId),
                status);
    }

    public List<InterviewAnswerView> fetchAnswers(Long interviewSessionId) {
        return interviewsContextFacade.fetchAnswers(interviewSessionId);
    }

    public List<CriterionView> fetchCriteria(Long jobPostingId) {
        return recruitmentContextFacade.fetchCriteria(jobPostingId);
    }

    public boolean isAnonymizedScreening(Long jobPostingId) {
        return recruitmentContextFacade.isAnonymizedScreening(jobPostingId);
    }

    public boolean existsJobPosting(Long jobPostingId) {
        return recruitmentContextFacade.existsJobPostingById(jobPostingId);
    }

    public long fetchVerifiedCertificationCount(Long candidateId) {
        return profilesContextFacade.fetchVerifiedCertificationCount(candidateId);
    }

    public long fetchRejectedCertificationCount(Long candidateId) {
        return profilesContextFacade.fetchRejectedCertificationCount(candidateId);
    }

    /** PII: only for redaction and for authorized, non-anonymized display. */
    public Optional<CandidatePersonalData> fetchCandidatePersonalData(Long candidateId) {
        return Optional.ofNullable(profilesContextFacade.fetchCandidatePersonalData(candidateId));
    }
}
