package pe.upc.simutalk.recruitment.domain.model.queries;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingViewer;

/**
 * @param viewer who is asking; a DRAFT of another company is reported as not found
 */
public record GetJobPostingByIdQuery(Long jobPostingId, JobPostingViewer viewer) {

    public GetJobPostingByIdQuery {
        if (viewer == null) {
            throw new IllegalArgumentException("Viewer is required");
        }
    }
}
