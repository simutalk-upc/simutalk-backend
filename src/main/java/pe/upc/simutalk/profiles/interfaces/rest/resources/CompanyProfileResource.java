package pe.upc.simutalk.profiles.interfaces.rest.resources;

import pe.upc.simutalk.profiles.domain.model.valueobjects.CompanySize;

import java.time.Instant;

public record CompanyProfileResource(
        Long id,
        Long userId,
        String legalName,
        String tradeName,
        String industry,
        String ruc,
        CompanySize companySize,
        String district,
        String email,
        Instant createdAt,
        Instant updatedAt) {
}
