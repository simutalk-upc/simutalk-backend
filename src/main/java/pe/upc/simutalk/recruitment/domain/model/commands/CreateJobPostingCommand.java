package pe.upc.simutalk.recruitment.domain.model.commands;

import java.time.LocalDate;

public record CreateJobPostingCommand(
        String title,
        String description,
        Long companyId,
        LocalDate closingDate,
        boolean anonymizedScreening) {
}
