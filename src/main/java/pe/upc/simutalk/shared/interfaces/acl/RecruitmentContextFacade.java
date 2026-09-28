package pe.upc.simutalk.shared.interfaces.acl;

import java.util.List;

/**
 * Contract other bounded contexts (interviews, assessment) use to ask recruitment about job
 * postings and applications. Implemented by recruitment. Missing ids answer neutral values
 * ({@code false}, {@code 0L}, empty list or empty string) instead of throwing.
 */
public interface RecruitmentContextFacade {

    boolean existsJobPostingById(Long jobPostingId);

    boolean isJobPostingPublished(Long jobPostingId);

    /** Whether the job posting exists and is still in DRAFT (its script and weights can change). */
    boolean isJobPostingDraft(Long jobPostingId);

    /** @return the owning company profile id, or {@code 0L} */
    Long fetchCompanyIdByJobPostingId(Long jobPostingId);

    boolean existsCriterionInJobPosting(Long jobPostingId, Long criterionId);

    /** Every criterion of the job posting, in creation order; empty if the posting does not exist. */
    List<CriterionView> fetchCriteria(Long jobPostingId);

    /** Whether the job posting hides candidates' personal data from human evaluators. */
    boolean isAnonymizedScreening(Long jobPostingId);

    /** Ids of the job posting's COMPETENCY criteria (CERTIFICATION ones are not interviewed). */
    List<Long> fetchCompetencyCriterionIds(Long jobPostingId);

    /** @return the job posting id of the application, or {@code 0L} */
    Long fetchJobPostingIdByApplicationId(Long applicationId);

    /** @return the candidate profile id of the application, or {@code 0L} */
    Long fetchCandidateIdByApplicationId(Long applicationId);

    /** @return the ApplicationStatus name, or an empty string if the application does not exist */
    String fetchApplicationStatus(Long applicationId);

    /** Moves the application to INTERVIEWING through its aggregate (transition rules apply). */
    void markApplicationAsInterviewing(Long applicationId);

    /** Moves the application to ASSESSED through its aggregate (transition rules apply). */
    void markApplicationAsAssessed(Long applicationId);
}
