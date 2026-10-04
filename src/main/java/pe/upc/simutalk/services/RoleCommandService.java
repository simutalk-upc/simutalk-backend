package pe.upc.simutalk.services;

import pe.upc.simutalk.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {

    void handle(SeedRolesCommand command);
}
