package pe.upc.simutalk.recruitment.domain.model.queries;

import java.util.List;

public record GetAverageTimeToShortlistQuery(List<Long> jobPostingIds) {

    public GetAverageTimeToShortlistQuery {
        jobPostingIds = jobPostingIds == null ? List.of() : List.copyOf(jobPostingIds);
    }
}
