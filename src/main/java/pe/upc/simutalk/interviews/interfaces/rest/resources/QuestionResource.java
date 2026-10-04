package pe.upc.simutalk.interviews.interfaces.rest.resources;

import pe.upc.simutalk.enums.QuestionOrigin;

import java.time.Instant;

public record QuestionResource(
        Long id,
        Long jobPostingId,
        Long criterionId,
        String statement,
        int maxDurationSeconds,
        int position,
        QuestionOrigin origin,
        boolean allowsFollowUp,
        Instant createdAt,
        Instant updatedAt) {
}
