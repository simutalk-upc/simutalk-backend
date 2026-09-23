package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.ChangeJobPostingStatusCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateJobPostingStatusResource;

public class ChangeJobPostingStatusCommandFromResourceAssembler {

    public static ChangeJobPostingStatusCommand toCommandFromResource(Long jobPostingId,
                                                                      UpdateJobPostingStatusResource resource) {
        return new ChangeJobPostingStatusCommand(jobPostingId, resource.status());
    }
}
