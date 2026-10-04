package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.iam.domain.model.queries.GetAllRolesQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {

    List<Role> handle(GetAllRolesQuery query);

    Optional<Role> handle(GetRoleByNameQuery query);
}
