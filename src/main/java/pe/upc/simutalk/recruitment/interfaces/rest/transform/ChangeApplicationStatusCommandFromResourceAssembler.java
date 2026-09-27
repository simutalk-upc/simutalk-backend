package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateApplicationStatusResource;

public class ChangeApplicationStatusCommandFromResourceAssembler {

    public static ChangeApplicationStatusCommand toCommandFromResource(Long applicationId,
                                                                       UpdateApplicationStatusResource resource) {
        return new ChangeApplicationStatusCommand(applicationId, resource.status());
    }
}
