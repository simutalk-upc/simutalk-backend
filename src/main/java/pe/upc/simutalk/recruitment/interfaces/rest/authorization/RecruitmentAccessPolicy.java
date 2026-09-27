package pe.upc.simutalk.recruitment.interfaces.rest.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingViewer;
import pe.upc.simutalk.recruitment.domain.services.JobPostingQueryService;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;

import java.util.Optional;

/**
 * Ownership checks used from {@code @PreAuthorize} as {@code @recruitmentAccess}, modeled
 * after profiles' ProfileAccessPolicy. The company of the authenticated user is resolved
 * through the IamContextFacade and ProfilesContextFacade contracts in shared, so
 * recruitment never imports iam or profiles classes.
 */
@Component("recruitmentAccess")
@RequiredArgsConstructor
public class RecruitmentAccessPolicy {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final JobPostingQueryService jobPostingQueryService;

    /** True when the authenticated user's company owns the job posting. */
    public boolean ownsJobPosting(Long jobPostingId, Authentication authentication) {
        return currentCompanyId(authentication)
                .map(companyId -> jobPostingQueryService
                        .handle(new GetJobPostingByIdQuery(jobPostingId, JobPostingViewer.unrestrictedViewer()))
                        .map(jobPosting -> jobPosting.isOwnedBy(companyId))
                        .orElse(false))
                .orElse(false);
    }

    /** Company profile id of the authenticated user, if it has one. */
    public Optional<Long> currentCompanyId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        var userId = iamContextFacade.fetchUserIdByUsername(authentication.getName());
        if (userId == null || userId == 0L) {
            return Optional.empty();
        }
        var companyId = profilesContextFacade.fetchCompanyIdByUserId(userId);
        return companyId == null || companyId == 0L ? Optional.empty() : Optional.of(companyId);
    }

    /** Visibility context for reads: admins see everything, others see by company. */
    public JobPostingViewer currentViewer(Authentication authentication) {
        var isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
        return isAdmin
                ? JobPostingViewer.unrestrictedViewer()
                : JobPostingViewer.ofCompany(currentCompanyId(authentication).orElse(null));
    }
}
