package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.entities.Answer;
import pe.upc.simutalk.interviews.interfaces.rest.resources.AnswerResource;

public class AnswerResourceFromEntityAssembler {

    public static AnswerResource toResourceFromEntity(Answer entity) {
        return new AnswerResource(entity.getId(), entity.getQuestionId(), entity.getParentAnswerId(),
                entity.getTranscript(), entity.getAudioUrl(), entity.getDurationSeconds(), entity.getAnsweredAt(),
                entity.isFollowUp());
    }
}
