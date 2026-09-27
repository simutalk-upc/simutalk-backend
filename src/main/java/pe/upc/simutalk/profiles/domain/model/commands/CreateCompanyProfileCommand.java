package pe.upc.simutalk.profiles.domain.model.commands;

import pe.upc.simutalk.profiles.domain.model.valueobjects.CompanySize;

public record CreateCompanyProfileCommand(
        Long userId,
        String legalName,
        String tradeName,
        String industry,
        String ruc,
        CompanySize companySize,
        String district) {
}
