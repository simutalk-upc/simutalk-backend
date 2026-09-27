package pe.upc.simutalk.recruitment.domain.model.queries;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;

/**
 * @param status optional pipeline stage filter; {@code null} returns every stage
 */
public record GetApplicationsByJobPostingIdQuery(Long jobPostingId, ApplicationStatus status) {
}
