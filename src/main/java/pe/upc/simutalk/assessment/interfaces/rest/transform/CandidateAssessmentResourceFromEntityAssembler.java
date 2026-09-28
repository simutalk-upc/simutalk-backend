package pe.upc.simutalk.assessment.interfaces.rest.transform;

import pe.upc.simutalk.assessment.domain.model.aggregates.Assessment;
import pe.upc.simutalk.assessment.interfaces.rest.resources.CandidateAssessmentResource;
import pe.upc.simutalk.assessment.interfaces.rest.resources.CandidateCriterionScoreResource;

/** Builds the candidate view: integrity flags, confidence, engine and ids of other records are left out on purpose. */
public class CandidateAssessmentResourceFromEntityAssembler {

    public static CandidateAssessmentResource toResourceFromEntity(Assessment entity) {
        var scores = entity.getCriterionScores().stream()
                .map(score -> new CandidateCriterionScoreResource(score.getCriterionName(), score.getCriterionKind(),
                        score.getWeightApplied(), score.getScore()))
                .toList();
        return new CandidateAssessmentResource(entity.getInterviewSessionId(), entity.getJobPostingId(),
                entity.getWeightedScore(), entity.getComputedAt(), scores, entity.getFeedbackSummary());
    }
}
