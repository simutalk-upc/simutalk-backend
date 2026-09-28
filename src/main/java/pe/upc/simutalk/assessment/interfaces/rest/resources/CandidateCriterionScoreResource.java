package pe.upc.simutalk.assessment.interfaces.rest.resources;

import pe.upc.simutalk.assessment.domain.model.valueobjects.CriterionKind;

import java.math.BigDecimal;

public record CandidateCriterionScoreResource(
        String criterionName,
        CriterionKind criterionKind,
        int weightApplied,
        BigDecimal score) {
}
