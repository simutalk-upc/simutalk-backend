package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.commands.AddCertificationCommand;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CreateCertificationResource;

public class AddCertificationCommandFromResourceAssembler {

    public static AddCertificationCommand toCommandFromResource(Long candidateId, CreateCertificationResource resource) {
        return new AddCertificationCommand(candidateId, resource.title(), resource.issuer(),
                resource.credentialCode(), resource.issuedAt(), resource.expiresAt());
    }
}
