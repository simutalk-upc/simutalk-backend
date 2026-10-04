package pe.upc.simutalk.iam.interfaces.rest.transform;

import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {

    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
