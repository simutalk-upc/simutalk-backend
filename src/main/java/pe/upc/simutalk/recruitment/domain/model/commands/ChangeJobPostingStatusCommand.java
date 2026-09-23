package pe.upc.simutalk.recruitment.domain.model.commands;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;

public record ChangeJobPostingStatusCommand(Long jobPostingId, JobPostingStatus status) {
}
