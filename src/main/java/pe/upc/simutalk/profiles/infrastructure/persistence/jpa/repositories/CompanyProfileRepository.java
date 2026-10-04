package pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.entities.CompanyProfile;
import pe.upc.simutalk.entities.Ruc;

import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    Optional<CompanyProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByRuc(Ruc ruc);
}
