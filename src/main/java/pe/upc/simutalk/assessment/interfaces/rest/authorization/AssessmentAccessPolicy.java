package pe.upc.simutalk.assessment.interfaces.rest.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pe.upc.simutalk.assessment.domain.model.queries.GetAssessmentByIdQuery;
import pe.upc.simutalk.assessment.domain.services.AssessmentQueryService;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.InterviewsContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.RecruitmentContextFacade;

import java.util.Optional;

/**
 * Ownership checks used from {@code @PreAuthorize} as {@code @assessmentAccess}: the
 * authenticated recruiter's company must own the job posting behind the session, the
 * assessment or the ranking. Resolved only through the shared facades.
 */
@Component("assessmentAccess")
@RequiredArgsConstructor
public class AssessmentAccessPolicy {

    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final RecruitmentContextFacade recruitmentContextFacade;
    private final InterviewsContextFacade interviewsContextFacade;
    private final AssessmentQueryService assessmentQueryService;

    public boolean ownsJobPosting(Long jobPostingId, Authentication authentication) {
        return currentCompanyId(authentication)
                .map(companyId -> companyId.equals(recruitmentContextFacade.fetchCompanyIdByJobPostingId(jobPostingId)))
                .orElse(false);
    }

    public boolean ownsSessionJobPosting(Long interviewSessionId, Authentication authentication) {
        return ownsJobPosting(interviewsContextFacade.fetchJobPostingIdBySessionId(interviewSessionId), authentication);
    }

    public boolean ownsAssessment(Long assessmentId, Authentication authentication) {
        return assessmentQueryService.handle(new GetAssessmentByIdQuery(assessmentId))
                .map(assessment -> ownsJobPosting(assessment.getJobPostingId(), authentication))
                .orElse(false);
    }

    private Optional<Long> currentCompanyId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(iamContextFacade.fetchUserIdByUsername(authentication.getName()))
                .filter(id -> id != 0L)
                .map(profilesContextFacade::fetchCompanyIdByUserId)
                .filter(id -> id != null && id != 0L);
    }
}
