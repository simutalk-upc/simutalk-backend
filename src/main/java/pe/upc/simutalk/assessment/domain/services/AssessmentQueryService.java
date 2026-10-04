package pe.upc.simutalk.assessment.domain.services;

import pe.upc.simutalk.entities.Assessment;
import pe.upc.simutalk.entities.Evidence;
import pe.upc.simutalk.assessment.domain.model.queries.*;
import pe.upc.simutalk.assessment.domain.model.valueobjects.CriterionAverage;
import pe.upc.simutalk.assessment.domain.model.queries.GetAssessmentByInterviewSessionIdQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetEvidencesByCriterionScoreQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetRankingByJobPostingIdQuery;
import pe.upc.simutalk.assessment.domain.model.valueobjects.Ranking;

import java.util.List;
import java.util.Optional;

public interface AssessmentQueryService {

    Optional<Assessment> handle(GetAssessmentByIdQuery query);

    Optional<Assessment> handle(GetAssessmentByInterviewSessionIdQuery query);

    /**
     * Evidences of a criterion score. When the job posting uses anonymized screening, the
     * candidate's personal data inside the excerpts is redacted.
     */
    List<Evidence> handle(GetEvidencesByCriterionScoreQuery query);

    Ranking handle(GetRankingByJobPostingIdQuery query);

    /** Average score per criterion, aggregated in the database; one decimal. */
    List<CriterionAverage> handle(GetCriterionAveragesQuery query);

    long handle(CountAssessmentsByJobPostingIdsQuery query);

    long handle(CountEvidencesByJobPostingIdsQuery query);
}
