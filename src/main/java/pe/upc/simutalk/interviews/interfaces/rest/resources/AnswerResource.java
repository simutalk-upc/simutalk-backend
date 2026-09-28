package pe.upc.simutalk.interviews.interfaces.rest.resources;

import java.time.Instant;

public record AnswerResource(
        Long id,
        Long questionId,
        Long parentAnswerId,
        String transcript,
        String audioUrl,
        int durationSeconds,
        Instant answeredAt,
        boolean followUp) {
}
