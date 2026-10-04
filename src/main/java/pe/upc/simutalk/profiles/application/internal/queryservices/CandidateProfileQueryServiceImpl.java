package pe.upc.simutalk.profiles.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.entities.CandidateProfile;
import pe.upc.simutalk.entities.Certification;
import pe.upc.simutalk.profiles.domain.model.queries.GetAllCandidateProfilesQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCertificationsByCandidateIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetVerifiedCertificationCountQuery;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileQueryService;
import pe.upc.simutalk.repositories.CandidateProfileRepository;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;

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

    /**
     * Pages without fetch-joining the collection (which would paginate in memory) and loads the
     * certifications inside the transaction, batched by {@code @BatchSize}.
     */
    @Override
    public Page<CandidateProfile> handle(GetAllCandidateProfilesQuery query) {
        var page = candidateProfileRepository.findAll(PageRequest.of(query.page(), query.size(), Sort.by("id")));
        page.forEach(candidate -> candidate.getCertifications().size());
        return page;
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
