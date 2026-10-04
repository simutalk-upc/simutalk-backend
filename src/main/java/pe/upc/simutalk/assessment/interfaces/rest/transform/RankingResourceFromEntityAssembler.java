package pe.upc.simutalk.assessment.interfaces.rest.transform;

import pe.upc.simutalk.dtos.Ranking;
import pe.upc.simutalk.dtos.RankingResource;
import pe.upc.simutalk.dtos.RankingResource.CriterionBreakdownResource;
import pe.upc.simutalk.dtos.RankingResource.RankingEntryResource;

public class RankingResourceFromEntityAssembler {

    public static RankingResource toResourceFromEntity(Ranking ranking) {
        var entries = ranking.entries().stream()
                .map(entry -> new RankingEntryResource(entry.rank(), entry.candidateCode(), entry.candidateId(),
                        entry.fullName(), entry.documentNumber(), entry.applicationId(), entry.assessmentId(),
                        entry.weightedScore(),
                        entry.criteria().stream()
                                .map(c -> new CriterionBreakdownResource(c.criterionScoreId(), c.criterionId(),
                                        c.criterionName(), c.kind(), c.weightApplied(), c.score(), c.confidence(),
                                        c.evidenceCount()))
                                .toList(),
                        entry.badges()))
                .toList();
        return new RankingResource(ranking.jobPostingId(), ranking.anonymized(), ranking.anonymizationForced(), entries);
    }
}
