package pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobPostingIdAndCandidateId(Long jobPostingId, Long candidateId);

    List<Application> findAllByJobPostingIdOrderByAppliedAtAsc(Long jobPostingId);

    List<Application> findAllByJobPostingIdAndStatusOrderByAppliedAtAsc(Long jobPostingId, ApplicationStatus status);

    List<Application> findAllByCandidateIdOrderByAppliedAtDesc(Long candidateId);
}
