package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.entities.InterviewSession;
import pe.upc.simutalk.dtos.InterviewSessionResource;

public class InterviewSessionResourceFromEntityAssembler {

    public static InterviewSessionResource toResourceFromEntity(InterviewSession entity) {
        return new InterviewSessionResource(entity.getId(), entity.getApplicationId(), entity.getJobPostingId(),
                entity.getCandidateId(), entity.getStatus(), entity.getInvitedAt(), entity.getStartedAt(),
                entity.getFinishedAt(), entity.getExpiresAt(), entity.getTotalDurationSeconds(),
                entity.answeredQuestionCount(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
