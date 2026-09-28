package pe.upc.simutalk.assessment.interfaces.rest.resources;

import pe.upc.simutalk.assessment.domain.model.valueobjects.CriterionKind;

import java.math.BigDecimal;

public record CriterionScoreResource(
        Long id,
        Long criterionId,
        String criterionName,
        CriterionKind criterionKind,
        BigDecimal score,
        int weightApplied,
        BigDecimal confidence,
        int evidenceCount) {
}
