package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.iam.domain.model.commands.SignUpCommand;
import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = resource.roles() == null
                ? List.<Role>of()
                : resource.roles().stream().map(Role::toRoleFromName).toList();
        return new SignUpCommand(resource.username(), resource.password(), roles);
    }
}
