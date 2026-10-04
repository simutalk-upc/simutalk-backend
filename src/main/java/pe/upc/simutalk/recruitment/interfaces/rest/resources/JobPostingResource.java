package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import pe.upc.simutalk.enums.JobPostingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record JobPostingResource(
        Long id,
        String title,
        String description,
        Long companyId,
        JobPostingStatus status,
        LocalDate closingDate,
        boolean anonymizedScreening,
        int totalWeight,
        List<EvaluationCriterionResource> criteria,
        Instant createdAt,
        Instant updatedAt) {
}
