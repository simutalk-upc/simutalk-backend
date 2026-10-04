package pe.upc.simutalk.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.enums.Roles;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(Roles name);

    boolean existsByName(Roles name);
}
