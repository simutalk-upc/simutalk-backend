package pe.upc.simutalk.recruitment.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.entities.EvaluationCriterion;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingViewer;
import pe.upc.simutalk.recruitment.domain.services.ApplicationCommandService;
import pe.upc.simutalk.recruitment.domain.services.ApplicationQueryService;
import pe.upc.simutalk.recruitment.domain.services.JobPostingQueryService;
import pe.upc.simutalk.shared.interfaces.acl.RecruitmentContextFacade;

import java.util.List;
import java.util.Optional;

/**
 * recruitment's implementation of the {@link RecruitmentContextFacade} contract published in
 * shared. Internal lookups are unrestricted: authorization is the caller's responsibility.
 */
@Service
@RequiredArgsConstructor
public class RecruitmentContextFacadeImpl implements RecruitmentContextFacade {

    private final JobPostingQueryService jobPostingQueryService;
    private final ApplicationQueryService applicationQueryService;
    private final ApplicationCommandService applicationCommandService;

    @Override
    public boolean existsJobPostingById(Long jobPostingId) {
        return findJobPosting(jobPostingId).isPresent();
    }

    @Override
    public boolean isJobPostingPublished(Long jobPostingId) {
        return findJobPosting(jobPostingId).map(JobPosting::isPublished).orElse(false);
    }

    @Override
    public boolean isJobPostingDraft(Long jobPostingId) {
        return findJobPosting(jobPostingId).map(JobPosting::isDraft).orElse(false);
    }

    @Override
    public Long fetchCompanyIdByJobPostingId(Long jobPostingId) {
        return findJobPosting(jobPostingId).map(jobPosting -> jobPosting.getCompanyId().value()).orElse(0L);
    }

    @Override
    public boolean existsCriterionInJobPosting(Long jobPostingId, Long criterionId) {
        return criterionId != null && findJobPosting(jobPostingId)
                .map(jobPosting -> jobPosting.getCriteria().stream()
                        .anyMatch(criterion -> criterionId.equals(criterion.getId())))
                .orElse(false);
    }

    @Override
    public List<Long> fetchCompetencyCriterionIds(Long jobPostingId) {
        return findJobPosting(jobPostingId)
                .map(jobPosting -> jobPosting.getCriteria().stream()
                        .filter(criterion -> criterion.getCriterionType() == CriterionType.COMPETENCY)
                        .map(EvaluationCriterion::getId)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public Long fetchJobPostingIdByApplicationId(Long applicationId) {
        return findApplication(applicationId).map(Application::getJobPostingId).orElse(0L);
    }

    @Override
    public Long fetchCandidateIdByApplicationId(Long applicationId) {
        return findApplication(applicationId).map(Application::getCandidateId).orElse(0L);
    }

    @Override
    public String fetchApplicationStatus(Long applicationId) {
        return findApplication(applicationId).map(application -> application.getStatus().name()).orElse("");
    }

    @Override
    public void markApplicationAsInterviewing(Long applicationId) {
        applicationCommandService.handle(new ChangeApplicationStatusCommand(applicationId, ApplicationStatus.INTERVIEWING));
    }

    @Override
    public void markApplicationAsAssessed(Long applicationId) {
        applicationCommandService.handle(new ChangeApplicationStatusCommand(applicationId, ApplicationStatus.ASSESSED));
    }

    private Optional<JobPosting> findJobPosting(Long jobPostingId) {
        if (jobPostingId == null) {
            return Optional.empty();
        }
        return jobPostingQueryService.handle(new GetJobPostingByIdQuery(jobPostingId, JobPostingViewer.unrestrictedViewer()));
    }

    private Optional<Application> findApplication(Long applicationId) {
        if (applicationId == null) {
            return Optional.empty();
        }
        return applicationQueryService.handle(new GetApplicationByIdQuery(applicationId));
    }
}
