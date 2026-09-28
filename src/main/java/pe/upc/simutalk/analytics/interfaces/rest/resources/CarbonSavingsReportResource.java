package pe.upc.simutalk.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

public record CarbonSavingsReportResource(Long jobPostingId, BigDecimal totalKgCo2eAvoided, long tripsAvoided,
                                          BigDecimal totalKmAvoided) {
}
