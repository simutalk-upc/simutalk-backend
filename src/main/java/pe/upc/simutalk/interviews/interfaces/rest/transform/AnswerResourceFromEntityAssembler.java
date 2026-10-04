package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.entities.Answer;
import pe.upc.simutalk.dtos.AnswerResource;

public class AnswerResourceFromEntityAssembler {

    public static AnswerResource toResourceFromEntity(Answer entity) {
        return new AnswerResource(entity.getId(), entity.getQuestionId(), entity.getParentAnswerId(),
                entity.getTranscript(), entity.getAudioUrl(), entity.getDurationSeconds(), entity.getAnsweredAt(),
                entity.isFollowUp());
    }
}
