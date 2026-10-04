package pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.entities.CandidateProfile;
import pe.upc.simutalk.entities.DocumentNumber;

import java.util.Optional;

/**
 * Finders load the candidate with its certifications (open-in-view is disabled).
 */
@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    @EntityGraph(attributePaths = "certifications")
    Optional<CandidateProfile> findWithCertificationsById(Long id);

    @EntityGraph(attributePaths = "certifications")
    Optional<CandidateProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByDocumentNumber(DocumentNumber documentNumber);
}
