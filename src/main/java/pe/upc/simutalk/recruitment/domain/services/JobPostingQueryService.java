package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.SearchJobPostingsQuery;

import java.util.List;
import java.util.Optional;

public interface JobPostingQueryService {

    Optional<JobPosting> handle(GetJobPostingByIdQuery query);

    List<JobPosting> handle(SearchJobPostingsQuery query);
}
