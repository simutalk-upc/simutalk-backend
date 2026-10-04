package pe.upc.simutalk.interviews.interfaces.rest.resources;

import pe.upc.simutalk.enums.InterviewSessionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record InterviewSessionResource(
        Long id,
        Long applicationId,
        Long jobPostingId,
        Long candidateId,
        InterviewSessionStatus status,
        Instant invitedAt,
        Instant startedAt,
        Instant finishedAt,
        LocalDate expiresAt,
        int totalDurationSeconds,
        long answeredQuestions,
        Instant createdAt,
        Instant updatedAt) {
}
