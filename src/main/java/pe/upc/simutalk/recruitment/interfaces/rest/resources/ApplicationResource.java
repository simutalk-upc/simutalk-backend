package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import pe.upc.simutalk.enums.ApplicationStatus;

import java.time.Instant;

public record ApplicationResource(
        Long id,
        Long jobPostingId,
        Long candidateId,
        ApplicationStatus status,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt) {
}
