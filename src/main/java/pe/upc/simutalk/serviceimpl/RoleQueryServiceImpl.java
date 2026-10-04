package pe.upc.simutalk.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.iam.domain.model.queries.GetAllRolesQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetRoleByNameQuery;
import pe.upc.simutalk.services.RoleQueryService;
import pe.upc.simutalk.repositories.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name());
    }
}
