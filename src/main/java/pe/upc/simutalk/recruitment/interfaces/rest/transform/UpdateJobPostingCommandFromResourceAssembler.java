package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.UpdateJobPostingCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateJobPostingResource;

public class UpdateJobPostingCommandFromResourceAssembler {

    public static UpdateJobPostingCommand toCommandFromResource(Long jobPostingId, UpdateJobPostingResource resource) {
        return new UpdateJobPostingCommand(jobPostingId, resource.title(), resource.description(),
                resource.closingDate(), resource.anonymizedScreening());
    }
}
