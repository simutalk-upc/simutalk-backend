package pe.upc.simutalk.assessment.domain.model.queries;

import java.util.List;

public record CountAssessmentsByJobPostingIdsQuery(List<Long> jobPostingIds) {

    public CountAssessmentsByJobPostingIdsQuery {
        jobPostingIds = jobPostingIds == null ? List.of() : List.copyOf(jobPostingIds);
    }
}
