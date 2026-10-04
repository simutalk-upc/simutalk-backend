package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.commands.CreateQuestionCommand;
import pe.upc.simutalk.enums.QuestionOrigin;
import pe.upc.simutalk.interviews.interfaces.rest.resources.CreateQuestionResource;

public class CreateQuestionCommandFromResourceAssembler {

    public static CreateQuestionCommand toCommandFromResource(Long jobPostingId, CreateQuestionResource resource) {
        return new CreateQuestionCommand(jobPostingId, resource.criterionId(), resource.statement(),
                resource.maxDurationSeconds(), resource.origin() == null ? QuestionOrigin.MANUAL : resource.origin(),
                resource.allowsFollowUp());
    }
}
