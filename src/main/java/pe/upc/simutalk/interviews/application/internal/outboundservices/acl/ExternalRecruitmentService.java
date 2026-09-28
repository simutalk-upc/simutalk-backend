package pe.upc.simutalk.interviews.application.internal.outboundservices.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.shared.domain.exceptions.BusinessRuleViolationException;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;
import pe.upc.simutalk.shared.interfaces.acl.RecruitmentContextFacade;

import java.util.List;

/**
 * Anti-corruption layer from interviews to recruitment, through the RecruitmentContextFacade
 * contract in shared. Translates recruitment's neutral answers into interviews' errors.
 */
@Service
@RequiredArgsConstructor
public class ExternalRecruitmentService {

    static final String APPLICATION_RECEIVED = "RECEIVED";

    private final RecruitmentContextFacade recruitmentContextFacade;

    /**
     * The script of a job posting is editable only while the posting is DRAFT; once
     * published it is frozen, like the weights.
     */
    public void ensureScriptIsEditable(Long jobPostingId) {
        if (!recruitmentContextFacade.existsJobPostingById(jobPostingId)) {
            throw new ResourceNotFoundException("Job posting", jobPostingId);
        }
        if (!recruitmentContextFacade.isJobPostingDraft(jobPostingId)) {
            throw new BusinessRuleViolationException(
                    "The interview script can only change while the job posting is in DRAFT");
        }
    }

    /**
     * The criterion must belong to the job posting and be a COMPETENCY: CERTIFICATION criteria
     * are not evaluated by interview.
     */
    public void ensureCompetencyCriterion(Long jobPostingId, Long criterionId) {
        List<Long> competencyIds = recruitmentContextFacade.fetchCompetencyCriterionIds(jobPostingId);
        if (competencyIds.contains(criterionId)) {
            return;
        }
        if (recruitmentContextFacade.existsCriterionInJobPosting(jobPostingId, criterionId)) {
            throw new BusinessRuleViolationException(
                    "Criterion %s is a CERTIFICATION criterion: certifications are not evaluated by interview, only COMPETENCY criteria can have questions"
                            .formatted(criterionId));
        }
        throw new BusinessRuleViolationException(
                "Criterion %s does not belong to job posting %s".formatted(criterionId, jobPostingId));
    }

    /** @return the job posting and candidate of an application that is still RECEIVED */
    public ApplicationSnapshot fetchApplicationReadyForInterview(Long applicationId) {
        var status = recruitmentContextFacade.fetchApplicationStatus(applicationId);
        if (status.isEmpty()) {
            throw new ResourceNotFoundException("Application", applicationId);
        }
        if (!APPLICATION_RECEIVED.equals(status)) {
            throw new BusinessRuleViolationException(
                    "An interview can only be created for a RECEIVED application (current: %s)".formatted(status));
        }
        return new ApplicationSnapshot(applicationId,
                recruitmentContextFacade.fetchJobPostingIdByApplicationId(applicationId),
                recruitmentContextFacade.fetchCandidateIdByApplicationId(applicationId));
    }

    public void markApplicationAsInterviewing(Long applicationId) {
        recruitmentContextFacade.markApplicationAsInterviewing(applicationId);
    }

    public void markApplicationAsAssessed(Long applicationId) {
        recruitmentContextFacade.markApplicationAsAssessed(applicationId);
    }

    public record ApplicationSnapshot(Long applicationId, Long jobPostingId, Long candidateId) {
    }
}
