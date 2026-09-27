package pe.upc.simutalk.iam.domain.services;

import pe.upc.simutalk.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {

    void handle(SeedRolesCommand command);
}
