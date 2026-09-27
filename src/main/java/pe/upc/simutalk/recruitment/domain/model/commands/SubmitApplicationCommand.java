package pe.upc.simutalk.recruitment.domain.model.commands;

public record SubmitApplicationCommand(Long jobPostingId, Long candidateId) {
}
