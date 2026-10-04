package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.dtos.UpdateJobPostingCommand;
import pe.upc.simutalk.dtos.UpdateJobPostingResource;

public class UpdateJobPostingCommandFromResourceAssembler {

    public static UpdateJobPostingCommand toCommandFromResource(Long jobPostingId, UpdateJobPostingResource resource) {
        return new UpdateJobPostingCommand(jobPostingId, resource.title(), resource.description(),
                resource.closingDate(), resource.anonymizedScreening());
    }
}
