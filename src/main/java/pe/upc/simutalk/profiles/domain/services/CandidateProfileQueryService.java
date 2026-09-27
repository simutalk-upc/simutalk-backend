package pe.upc.simutalk.profiles.domain.services;

import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.entities.Certification;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCertificationsByCandidateIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetVerifiedCertificationCountQuery;

import java.util.List;
import java.util.Optional;

public interface CandidateProfileQueryService {

    Optional<CandidateProfile> handle(GetCandidateProfileByIdQuery query);

    Optional<CandidateProfile> handle(GetCandidateProfileByUserIdQuery query);

    /**
     * @throws pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException if the candidate does not exist
     */
    List<Certification> handle(GetCertificationsByCandidateIdQuery query);

    /**
     * @return certifications that are VERIFIED and not expired today; 0 if the candidate does not exist
     */
    long handle(GetVerifiedCertificationCountQuery query);
}
