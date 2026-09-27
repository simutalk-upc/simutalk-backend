package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import pe.upc.simutalk.profiles.interfaces.rest.resources.UpdateCompanyProfileResource;

public class UpdateCompanyProfileCommandFromResourceAssembler {

    public static UpdateCompanyProfileCommand toCommandFromResource(Long companyProfileId,
                                                                    UpdateCompanyProfileResource resource) {
        return new UpdateCompanyProfileCommand(companyProfileId, resource.legalName(), resource.tradeName(),
                resource.industry(), resource.companySize(), resource.district());
    }
}
