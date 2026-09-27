package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.iam.domain.model.commands.SignInCommand;
import pe.upc.simutalk.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {

    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}
