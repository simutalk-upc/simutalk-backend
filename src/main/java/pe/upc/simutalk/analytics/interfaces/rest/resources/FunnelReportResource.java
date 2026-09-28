package pe.upc.simutalk.analytics.interfaces.rest.resources;

import java.util.List;

public record FunnelReportResource(Long jobPostingId, long totalApplications, List<Stage> stages) {

    public record Stage(String stage, long count) {
    }
}
