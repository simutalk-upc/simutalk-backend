package pe.upc.simutalk.assessment.domain.model.queries;

import java.util.List;

public record CountEvidencesByJobPostingIdsQuery(List<Long> jobPostingIds) {

    public CountEvidencesByJobPostingIdsQuery {
        jobPostingIds = jobPostingIds == null ? List.of() : List.copyOf(jobPostingIds);
    }
}
