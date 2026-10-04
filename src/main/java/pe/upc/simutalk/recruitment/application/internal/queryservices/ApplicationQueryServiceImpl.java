package pe.upc.simutalk.recruitment.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.entities.Application;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationCountByStatusQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetAverageTimeToShortlistQuery;
import pe.upc.simutalk.enums.ApplicationStatus;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByCandidateIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByJobPostingIdQuery;
import pe.upc.simutalk.recruitment.domain.services.ApplicationQueryService;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.ApplicationRepository;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    public Map<ApplicationStatus, Long> handle(GetApplicationCountByStatusQuery query) {
        var counts = new EnumMap<ApplicationStatus, Long>(ApplicationStatus.class);
        for (var status : ApplicationStatus.values()) {
            counts.put(status, 0L);
        }
        applicationRepository.countByStatusForJobPosting(query.jobPostingId())
                .forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return counts;
    }

    @Override
    public Optional<Duration> handle(GetAverageTimeToShortlistQuery query) {
        if (query.jobPostingIds().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(applicationRepository.averageSecondsToShortlist(query.jobPostingIds()))
                .map(seconds -> Duration.ofSeconds(Math.round(seconds)));
    }

    @Override
    public List<Application> handle(GetApplicationsByCandidateIdQuery query) {
        return applicationRepository.findAllByCandidateIdOrderByAppliedAtDesc(query.candidateId());
    }
}
