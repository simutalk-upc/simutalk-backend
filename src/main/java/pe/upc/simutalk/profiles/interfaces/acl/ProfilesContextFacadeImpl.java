package pe.upc.simutalk.profiles.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetVerifiedCertificationCountQuery;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileQueryService;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileQueryService;
import pe.upc.simutalk.shared.interfaces.acl.CandidatePersonalData;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;

/**
 * profiles' implementation of the {@link ProfilesContextFacade} contract published in shared.
 */
@Service
@RequiredArgsConstructor
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {

    private final CandidateProfileQueryService candidateProfileQueryService;
    private final CompanyProfileQueryService companyProfileQueryService;

    @Override
    public Long fetchCandidateIdByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return candidateProfileQueryService.handle(new GetCandidateProfileByUserIdQuery(userId))
                .map(CandidateProfile::getId)
                .orElse(0L);
    }

    @Override
    public Long fetchCompanyIdByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return companyProfileQueryService.handle(new GetCompanyProfileByUserIdQuery(userId))
                .map(CompanyProfile::getId)
                .orElse(0L);
    }

    @Override
    public long fetchVerifiedCertificationCount(Long candidateId) {
        if (candidateId == null) {
            return 0L;
        }
        return candidateProfileQueryService.handle(new GetVerifiedCertificationCountQuery(candidateId));
    }

    @Override
    public long fetchRejectedCertificationCount(Long candidateId) {
        if (candidateId == null) {
            return 0L;
        }
        return candidateProfileQueryService.handle(new GetCandidateProfileByIdQuery(candidateId))
                .map(CandidateProfile::countRejectedCertifications)
                .orElse(0L);
    }

    @Override
    public CandidatePersonalData fetchCandidatePersonalData(Long candidateId) {
        if (candidateId == null) {
            return null;
        }
        return candidateProfileQueryService.handle(new GetCandidateProfileByIdQuery(candidateId))
                .map(candidate -> new CandidatePersonalData(candidate.getId(), candidate.getPersonName().firstName(),
                        candidate.getPersonName().lastName(), candidate.getDocumentNumber().value(),
                        candidate.getPhone(), candidate.getDistrict(), candidate.getBirthDate()))
                .orElse(null);
    }

    @Override
    public String fetchCandidateDistrict(Long candidateId) {
        if (candidateId == null) {
            return "";
        }
        return candidateProfileQueryService.handle(new GetCandidateProfileByIdQuery(candidateId))
                .map(CandidateProfile::getDistrict)
                .orElse("");
    }
}
