package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationCountByStatusQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetAverageTimeToShortlistQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByCandidateIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByJobPostingIdQuery;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApplicationQueryService {

    Optional<Application> handle(GetApplicationByIdQuery query);

    List<Application> handle(GetApplicationsByJobPostingIdQuery query);

    List<Application> handle(GetApplicationsByCandidateIdQuery query);

    /** Every pipeline stage with its number of applications (0 included). */
    Map<ApplicationStatus, Long> handle(GetApplicationCountByStatusQuery query);

    /** Average time from applying to SHORTLISTED; empty when nobody was shortlisted. */
    Optional<Duration> handle(GetAverageTimeToShortlistQuery query);
}
