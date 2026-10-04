package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.dtos.ChangeApplicationStatusCommand;
import pe.upc.simutalk.dtos.UpdateApplicationStatusResource;

public class ChangeApplicationStatusCommandFromResourceAssembler {

    public static ChangeApplicationStatusCommand toCommandFromResource(Long applicationId,
                                                                       UpdateApplicationStatusResource resource) {
        return new ChangeApplicationStatusCommand(applicationId, resource.status());
    }
}
