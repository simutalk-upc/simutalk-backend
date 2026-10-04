package pe.upc.simutalk.mappers;

import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.dtos.RoleResource;

public class RoleResourceFromEntityAssembler {

    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
