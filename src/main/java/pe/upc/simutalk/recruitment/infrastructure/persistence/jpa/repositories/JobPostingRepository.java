package pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CompanyId;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;

import java.util.List;
import java.util.Optional;

/**
 * Every finder loads the aggregate together with its criteria, so the whole
 * aggregate is available after the transaction ends (open-in-view is disabled).
 */
@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    @EntityGraph(attributePaths = "criteria")
    Optional<JobPosting> findWithCriteriaById(Long id);

    @EntityGraph(attributePaths = "criteria")
    List<JobPosting> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "criteria")
    List<JobPosting> findAllByCompanyIdOrderByIdAsc(CompanyId companyId);

    @EntityGraph(attributePaths = "criteria")
    List<JobPosting> findAllByStatusOrderByIdAsc(JobPostingStatus status);

    @EntityGraph(attributePaths = "criteria")
    List<JobPosting> findAllByCompanyIdAndStatusOrderByIdAsc(CompanyId companyId, JobPostingStatus status);

    long countByCompanyIdAndStatus(CompanyId companyId, JobPostingStatus status);

    @Query("select j.id from JobPosting j where j.companyId = :companyId order by j.id")
    List<Long> findIdsByCompanyId(@Param("companyId") CompanyId companyId);
}
