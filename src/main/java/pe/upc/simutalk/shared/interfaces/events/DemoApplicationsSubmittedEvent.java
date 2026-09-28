package pe.upc.simutalk.shared.interfaces.events;

import java.util.List;

/**
 * Integration event published by recruitment's demo seeder after the demo candidates applied
 * (all in RECEIVED). {@code targetStage} says where each application should end up;
 * interviews reaches it by creating, answering and completing interview sessions.
 */
public record DemoApplicationsSubmittedEvent(Long jobPostingId, List<DemoApplication> applications) {

    /**
     * @param targetStage RECEIVED, INTERVIEWING or ASSESSED
     */
    public record DemoApplication(Long applicationId, String targetStage) {
    }
}
