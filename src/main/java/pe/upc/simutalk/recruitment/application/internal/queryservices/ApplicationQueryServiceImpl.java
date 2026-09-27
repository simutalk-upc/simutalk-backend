package pe.upc.simutalk.recruitment.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByCandidateIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByJobPostingIdQuery;
import pe.upc.simutalk.recruitment.domain.services.ApplicationQueryService;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.ApplicationRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationQueryServiceImpl implements ApplicationQueryService {

    private final ApplicationRepository applicationRepository;

    @Override
    public Optional<Application> handle(GetApplicationByIdQuery query) {
        return applicationRepository.findById(query.applicationId());
    }

    @Override
    public List<Application> handle(GetApplicationsByJobPostingIdQuery query) {
        return query.status() == null
                ? applicationRepository.findAllByJobPostingIdOrderByAppliedAtAsc(query.jobPostingId())
                : applicationRepository.findAllByJobPostingIdAndStatusOrderByAppliedAtAsc(query.jobPostingId(),
                query.status());
    }

    @Override
    public List<Application> handle(GetApplicationsByCandidateIdQuery query) {
        return applicationRepository.findAllByCandidateIdOrderByAppliedAtDesc(query.candidateId());
    }
}
