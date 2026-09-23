package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.recruitment.domain.model.commands.UpdateEvaluationCriterionCommand;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateEvaluationCriterionResource;

public class UpdateEvaluationCriterionCommandFromResourceAssembler {

    public static UpdateEvaluationCriterionCommand toCommandFromResource(Long jobPostingId, Long criterionId,
                                                                         UpdateEvaluationCriterionResource resource) {
        return new UpdateEvaluationCriterionCommand(jobPostingId, criterionId, resource.name(), resource.description(),
                resource.weight(), resource.criterionType(), resource.certificationName(), resource.mandatory());
    }
}
