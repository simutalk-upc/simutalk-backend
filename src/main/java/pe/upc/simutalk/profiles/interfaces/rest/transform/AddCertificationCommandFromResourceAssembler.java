package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.dtos.AddCertificationCommand;
import pe.upc.simutalk.dtos.CreateCertificationResource;

public class AddCertificationCommandFromResourceAssembler {

    public static AddCertificationCommand toCommandFromResource(Long candidateId, CreateCertificationResource resource) {
        return new AddCertificationCommand(candidateId, resource.title(), resource.issuer(),
                resource.credentialCode(), resource.issuedAt(), resource.expiresAt());
    }
}
