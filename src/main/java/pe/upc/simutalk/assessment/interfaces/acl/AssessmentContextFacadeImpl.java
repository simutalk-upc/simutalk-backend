package pe.upc.simutalk.assessment.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.assessment.domain.model.queries.CountAssessmentsByJobPostingIdsQuery;
import pe.upc.simutalk.assessment.domain.model.queries.CountEvidencesByJobPostingIdsQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetCriterionAveragesQuery;
import pe.upc.simutalk.services.AssessmentQueryService;
import pe.upc.simutalk.services.AssessmentContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.CriterionAverageView;

import java.util.List;

/**
 * assessment's implementation of the {@link AssessmentContextFacade} contract published in shared.
 */
@Service
@RequiredArgsConstructor
public class AssessmentContextFacadeImpl implements AssessmentContextFacade {

    private final AssessmentQueryService assessmentQueryService;

    @Override
    public List<CriterionAverageView> fetchCriterionAverages(Long jobPostingId) {
        return assessmentQueryService.handle(new GetCriterionAveragesQuery(jobPostingId)).stream()
                .map(average -> new CriterionAverageView(average.criterionId(), average.criterionName(),
                        average.averageScore(), average.assessedCount()))
                .toList();
    }

    @Override
    public long countAssessmentsByJobPostingIds(List<Long> jobPostingIds) {
        return assessmentQueryService.handle(new CountAssessmentsByJobPostingIdsQuery(jobPostingIds));
    }

    @Override
    public long countEvidencesByJobPostingIds(List<Long> jobPostingIds) {
        return assessmentQueryService.handle(new CountEvidencesByJobPostingIdsQuery(jobPostingIds));
    }
}
