package pe.upc.simutalk.recruitment.domain.model.commands;

import java.time.LocalDate;

public record UpdateJobPostingCommand(
        Long jobPostingId,
        String title,
        String description,
        LocalDate closingDate,
        boolean anonymizedScreening) {
}
