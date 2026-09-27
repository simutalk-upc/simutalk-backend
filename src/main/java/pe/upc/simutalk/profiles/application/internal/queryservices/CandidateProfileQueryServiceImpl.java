package pe.upc.simutalk.profiles.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.entities.Certification;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCertificationsByCandidateIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetVerifiedCertificationCountQuery;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileQueryService;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CandidateProfileRepository;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CandidateProfileQueryServiceImpl implements CandidateProfileQueryService {

    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    public Optional<CandidateProfile> handle(GetCandidateProfileByIdQuery query) {
        return candidateProfileRepository.findWithCertificationsById(query.candidateId());
    }

    @Override
    public Optional<CandidateProfile> handle(GetCandidateProfileByUserIdQuery query) {
        return candidateProfileRepository.findByUserId(query.userId());
    }

    @Override
    public List<Certification> handle(GetCertificationsByCandidateIdQuery query) {
        return candidateProfileRepository.findWithCertificationsById(query.candidateId())
                .map(CandidateProfile::getCertifications)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile", query.candidateId()));
    }

    @Override
    public long handle(GetVerifiedCertificationCountQuery query) {
        return candidateProfileRepository.findWithCertificationsById(query.candidateId())
                .map(candidate -> candidate.countCertificationsForScoring(LocalDate.now()))
                .orElse(0L);
    }
}
