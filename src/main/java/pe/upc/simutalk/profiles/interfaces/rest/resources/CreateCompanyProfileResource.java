package pe.upc.simutalk.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.upc.simutalk.profiles.domain.model.valueobjects.CompanySize;

public record CreateCompanyProfileResource(
        @Schema(example = "1") @NotNull @Positive Long userId,
        @Schema(example = "Consultora Andina S.A.C.") @NotBlank @Size(max = 150) String legalName,
        @Schema(example = "Consultora Andina") @NotBlank @Size(max = 150) String tradeName,
        @Schema(example = "Consultoría de TI") @NotBlank @Size(max = 150) String industry,
        @Schema(example = "20554873621", description = "11 dígitos, empieza en 10 o 20") @NotBlank String ruc,
        @Schema(example = "MEDIANA") @NotNull CompanySize companySize,
        @Schema(example = "San Isidro") @NotBlank @Size(max = 80) String district) {
}
