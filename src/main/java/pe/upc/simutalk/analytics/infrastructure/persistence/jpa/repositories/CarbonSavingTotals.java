package pe.upc.simutalk.analytics.infrastructure.persistence.jpa.repositories;

import java.math.BigDecimal;

/** JPQL projection: SUM and COUNT of the carbon savings of a job posting. */
public interface CarbonSavingTotals {

    BigDecimal getTotalKg();

    BigDecimal getTotalKm();

    long getTrips();
}
