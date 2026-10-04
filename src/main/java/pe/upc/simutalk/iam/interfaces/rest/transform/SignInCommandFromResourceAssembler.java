package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.dtos.SignInCommand;
import pe.upc.simutalk.dtos.SignInResource;

public class SignInCommandFromResourceAssembler {

    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}
