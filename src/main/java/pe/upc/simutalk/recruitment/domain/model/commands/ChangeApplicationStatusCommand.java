package pe.upc.simutalk.recruitment.domain.model.commands;

import pe.upc.simutalk.enums.ApplicationStatus;

public record ChangeApplicationStatusCommand(Long applicationId, ApplicationStatus status) {
}
