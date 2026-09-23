package pe.upc.simutalk.recruitment.domain.model.queries;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;

/**
 * Lists job postings. Both filters are optional; a {@code null} filter is ignored.
 */
public record SearchJobPostingsQuery(Long companyId, JobPostingStatus status) {
}
