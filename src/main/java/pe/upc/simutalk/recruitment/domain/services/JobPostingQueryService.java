package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.entities.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingIdsByCompanyIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetPublishedJobPostingCountByCompanyIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.SearchJobPostingsQuery;

import java.util.List;
import java.util.Optional;

public interface JobPostingQueryService {

    Optional<JobPosting> handle(GetJobPostingByIdQuery query);

    List<JobPosting> handle(SearchJobPostingsQuery query);

    long handle(GetPublishedJobPostingCountByCompanyIdQuery query);

    List<Long> handle(GetJobPostingIdsByCompanyIdQuery query);
}
