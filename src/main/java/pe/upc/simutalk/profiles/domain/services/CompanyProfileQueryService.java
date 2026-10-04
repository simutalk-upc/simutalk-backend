package pe.upc.simutalk.profiles.domain.services;

import org.springframework.data.domain.Page;
import pe.upc.simutalk.entities.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.queries.GetAllCompanyProfilesQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;

import java.util.Optional;

public interface CompanyProfileQueryService {

    Optional<CompanyProfile> handle(GetCompanyProfileByIdQuery query);

    /** Page of companies ordered by id. */
    Page<CompanyProfile> handle(GetAllCompanyProfilesQuery query);

    Optional<CompanyProfile> handle(GetCompanyProfileByUserIdQuery query);
}
