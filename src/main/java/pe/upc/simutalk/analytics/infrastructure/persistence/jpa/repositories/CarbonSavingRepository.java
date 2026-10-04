package pe.upc.simutalk.analytics.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.entities.CarbonSaving;

@Repository
public interface CarbonSavingRepository extends JpaRepository<CarbonSaving, Long> {

    boolean existsByApplicationId(Long applicationId);

    @Query("select coalesce(sum(c.kgCo2eAvoided), 0) as totalKg, coalesce(sum(c.distanceKm), 0) as totalKm, "
            + "count(c) as trips from CarbonSaving c where c.jobPostingId = :jobPostingId")
    CarbonSavingTotals totalsByJobPostingId(@Param("jobPostingId") Long jobPostingId);
}
