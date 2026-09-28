package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.commands.CreateInterviewSessionCommand;
import pe.upc.simutalk.interviews.interfaces.rest.resources.CreateInterviewSessionResource;

public class CreateInterviewSessionCommandFromResourceAssembler {

    public static CreateInterviewSessionCommand toCommandFromResource(Long applicationId,
                                                                      CreateInterviewSessionResource resource) {
        return new CreateInterviewSessionCommand(applicationId, resource.expiresAt());
    }
}
