package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.commands.RecordAnswerCommand;
import pe.upc.simutalk.interviews.interfaces.rest.resources.RecordAnswerResource;

public class RecordAnswerCommandFromResourceAssembler {

    public static RecordAnswerCommand toCommandFromResource(Long interviewSessionId, RecordAnswerResource resource) {
        return new RecordAnswerCommand(interviewSessionId, resource.questionId(), resource.transcript(),
                resource.audioUrl(), resource.durationSeconds(), resource.followUp(), resource.parentAnswerId());
    }
}
