package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.commands.CreateCompanyProfileCommand;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CreateCompanyProfileResource;

public class CreateCompanyProfileCommandFromResourceAssembler {

    public static CreateCompanyProfileCommand toCommandFromResource(CreateCompanyProfileResource resource) {
        return new CreateCompanyProfileCommand(resource.userId(), resource.legalName(), resource.tradeName(),
                resource.industry(), resource.ruc(), resource.companySize(), resource.district(), resource.email());
    }
}
