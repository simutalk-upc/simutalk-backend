package pe.upc.simutalk.profiles.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileQueryService;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CompanyProfileRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyProfileQueryServiceImpl implements CompanyProfileQueryService {

    private final CompanyProfileRepository companyProfileRepository;

    @Override
    public Optional<CompanyProfile> handle(GetCompanyProfileByIdQuery query) {
        return companyProfileRepository.findById(query.companyProfileId());
    }

    @Override
    public Optional<CompanyProfile> handle(GetCompanyProfileByUserIdQuery query) {
        return companyProfileRepository.findByUserId(query.userId());
    }
}
