package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByCandidateIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByJobPostingIdQuery;

import java.util.List;
import java.util.Optional;

public interface ApplicationQueryService {

    Optional<Application> handle(GetApplicationByIdQuery query);

    List<Application> handle(GetApplicationsByJobPostingIdQuery query);

    List<Application> handle(GetApplicationsByCandidateIdQuery query);
}
