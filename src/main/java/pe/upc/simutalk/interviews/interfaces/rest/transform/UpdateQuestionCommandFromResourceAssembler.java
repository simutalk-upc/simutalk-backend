package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.dtos.UpdateQuestionCommand;
import pe.upc.simutalk.enums.QuestionOrigin;
import pe.upc.simutalk.dtos.UpdateQuestionResource;

public class UpdateQuestionCommandFromResourceAssembler {

    public static UpdateQuestionCommand toCommandFromResource(Long jobPostingId, Long questionId,
                                                              UpdateQuestionResource resource) {
        return new UpdateQuestionCommand(jobPostingId, questionId, resource.criterionId(), resource.statement(),
                resource.maxDurationSeconds(), resource.origin() == null ? QuestionOrigin.MANUAL : resource.origin(),
                resource.allowsFollowUp());
    }
}
