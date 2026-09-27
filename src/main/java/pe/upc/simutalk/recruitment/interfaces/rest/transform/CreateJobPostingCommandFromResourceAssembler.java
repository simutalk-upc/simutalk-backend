package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.CreateJobPostingResource;

public class CreateJobPostingCommandFromResourceAssembler {

    /**
     * @param companyId company of the authenticated recruiter; never taken from the request body
     */
    public static CreateJobPostingCommand toCommandFromResource(CreateJobPostingResource resource, Long companyId) {
        return new CreateJobPostingCommand(resource.title(), resource.description(), companyId,
                resource.closingDate(), resource.anonymizedScreening());
    }
}
