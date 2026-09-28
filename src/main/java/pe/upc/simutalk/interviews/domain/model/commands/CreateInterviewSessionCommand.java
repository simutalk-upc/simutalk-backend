package pe.upc.simutalk.interviews.domain.model.commands;

import java.time.LocalDate;

public record CreateInterviewSessionCommand(Long applicationId, LocalDate expiresAt) {
}
