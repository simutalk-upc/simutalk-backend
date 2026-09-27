package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;

public record UpdateApplicationStatusResource(
        @Schema(example = "INTERVIEWING",
                description = "RECEIVED→INTERVIEWING→ASSESSED→SHORTLISTED→HIRED; REJECTED desde cualquier etapa no final")
        @NotNull ApplicationStatus status) {
}
