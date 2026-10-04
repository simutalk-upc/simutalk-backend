package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.entities.User;
import pe.upc.simutalk.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(user.getId(), user.getUsername(), user.getRoleNames());
    }
}
