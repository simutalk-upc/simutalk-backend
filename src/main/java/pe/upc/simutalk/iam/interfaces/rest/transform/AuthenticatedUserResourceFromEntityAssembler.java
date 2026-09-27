package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.upc.simutalk.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedUser authenticatedUser) {
        var user = authenticatedUser.user();
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), authenticatedUser.token());
    }
}
