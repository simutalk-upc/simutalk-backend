package pe.upc.simutalk.profiles.domain.model.commands;

import pe.upc.simutalk.profiles.domain.model.valueobjects.CompanySize;

/** The RUC is not part of the update: it cannot change. */
public record UpdateCompanyProfileCommand(
        Long companyProfileId,
        String legalName,
        String tradeName,
        String industry,
        CompanySize companySize,
        String district,
        String email) {

    /** Updates without e-mail, which leaves the profile without one. */
    public UpdateCompanyProfileCommand(Long companyProfileId, String legalName, String tradeName, String industry,
                                       CompanySize companySize, String district) {
        this(companyProfileId, legalName, tradeName, industry, companySize, district, null);
    }
}
