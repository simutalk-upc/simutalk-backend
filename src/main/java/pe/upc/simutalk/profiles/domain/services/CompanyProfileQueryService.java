package pe.upc.simutalk.profiles.domain.services;

import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;

import java.util.Optional;

public interface CompanyProfileQueryService {

    Optional<CompanyProfile> handle(GetCompanyProfileByIdQuery query);

    Optional<CompanyProfile> handle(GetCompanyProfileByUserIdQuery query);
}
