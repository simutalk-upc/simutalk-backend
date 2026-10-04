package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import pe.upc.simutalk.enums.CriterionOrigin;
import pe.upc.simutalk.enums.CriterionType;

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
        CriterionOrigin origin,
        Instant createdAt,
        Instant updatedAt) {
}
