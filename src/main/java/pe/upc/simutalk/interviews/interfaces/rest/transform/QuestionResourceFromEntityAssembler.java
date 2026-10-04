package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.entities.Question;
import pe.upc.simutalk.interviews.interfaces.rest.resources.QuestionResource;

public class QuestionResourceFromEntityAssembler {

    public static QuestionResource toResourceFromEntity(Question entity) {
        return new QuestionResource(entity.getId(), entity.getJobPostingId(), entity.getCriterionId(),
                entity.getStatement(), entity.getMaxDurationSeconds(), entity.getPosition(), entity.getOrigin(),
                entity.isAllowsFollowUp(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
