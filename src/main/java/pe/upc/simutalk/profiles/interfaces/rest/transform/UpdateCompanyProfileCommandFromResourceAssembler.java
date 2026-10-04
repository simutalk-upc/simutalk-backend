package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.dtos.UpdateCompanyProfileCommand;
import pe.upc.simutalk.dtos.UpdateCompanyProfileResource;

public class UpdateCompanyProfileCommandFromResourceAssembler {

    public static UpdateCompanyProfileCommand toCommandFromResource(Long companyProfileId,
                                                                    UpdateCompanyProfileResource resource) {
        return new UpdateCompanyProfileCommand(companyProfileId, resource.legalName(), resource.tradeName(),
                resource.industry(), resource.companySize(), resource.district(), resource.email());
    }
}
