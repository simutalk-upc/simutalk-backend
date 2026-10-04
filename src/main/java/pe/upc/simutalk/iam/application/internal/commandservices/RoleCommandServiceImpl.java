package pe.upc.simutalk.iam.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.iam.domain.model.commands.SeedRolesCommand;
import pe.upc.simutalk.iam.domain.model.entities.Role;
import pe.upc.simutalk.enums.Roles;
import pe.upc.simutalk.iam.domain.services.RoleCommandService;
import pe.upc.simutalk.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    /** Idempotent: only inserts the roles that are still missing. */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values())
                .filter(name -> !roleRepository.existsByName(name))
                .forEach(name -> roleRepository.save(new Role(name)));
    }
}
