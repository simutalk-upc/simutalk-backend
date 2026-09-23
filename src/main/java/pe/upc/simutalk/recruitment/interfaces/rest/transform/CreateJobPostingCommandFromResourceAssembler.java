package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.CreateJobPostingResource;

public class CreateJobPostingCommandFromResourceAssembler {

    public static CreateJobPostingCommand toCommandFromResource(CreateJobPostingResource resource) {
        return new CreateJobPostingCommand(resource.title(), resource.description(), resource.companyId(),
                resource.closingDate(), resource.anonymizedScreening());
    }
}
