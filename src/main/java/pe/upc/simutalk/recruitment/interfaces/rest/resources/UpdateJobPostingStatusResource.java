package pe.upc.simutalk.recruitment.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;

public record UpdateJobPostingStatusResource(
        @Schema(example = "PUBLISHED", description = "Estado destino: PUBLISHED o CLOSED")
        @NotNull JobPostingStatus status) {
}
