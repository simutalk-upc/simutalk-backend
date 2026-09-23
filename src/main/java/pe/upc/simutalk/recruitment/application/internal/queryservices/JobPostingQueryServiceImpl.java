package pe.upc.simutalk.recruitment.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.SearchJobPostingsQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CompanyId;
import pe.upc.simutalk.recruitment.domain.services.JobPostingQueryService;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.JobPostingRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobPostingQueryServiceImpl implements JobPostingQueryService {

    private final JobPostingRepository jobPostingRepository;

    @Override
    public Optional<JobPosting> handle(GetJobPostingByIdQuery query) {
        return jobPostingRepository.findWithCriteriaById(query.jobPostingId());
    }

    @Override
    public List<JobPosting> handle(SearchJobPostingsQuery query) {
        var status = query.status();
        if (query.companyId() == null) {
            return status == null
                    ? jobPostingRepository.findAllByOrderByIdAsc()
                    : jobPostingRepository.findAllByStatusOrderByIdAsc(status);
        }
        var companyId = new CompanyId(query.companyId());
        return status == null
                ? jobPostingRepository.findAllByCompanyIdOrderByIdAsc(companyId)
                : jobPostingRepository.findAllByCompanyIdAndStatusOrderByIdAsc(companyId, status);
    }
}
