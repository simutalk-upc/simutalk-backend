package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;

import java.time.Instant;

public record EvaluationCriterionResource(
        Long id,
        Long jobPostingId,
        String name,
        String description,
        int weight,
        CriterionType criterionType,
        String certificationName,
        boolean mandatory,
        Instant createdAt,
        Instant updatedAt) {
}
