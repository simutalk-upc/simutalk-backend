package pe.upc.simutalk.assessment.domain.model.valueobjects;

import java.math.BigDecimal;

public record CriterionAverage(Long criterionId, String criterionName, BigDecimal averageScore, long assessedCount) {
}
