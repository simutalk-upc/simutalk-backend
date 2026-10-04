package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.dtos.AddEvaluationCriterionCommand;
import pe.upc.simutalk.enums.CriterionOrigin;
import pe.upc.simutalk.dtos.CreateEvaluationCriterionResource;

public class AddEvaluationCriterionCommandFromResourceAssembler {

    public static AddEvaluationCriterionCommand toCommandFromResource(Long jobPostingId,
                                                                      CreateEvaluationCriterionResource resource) {
        return new AddEvaluationCriterionCommand(jobPostingId, resource.name(), resource.description(),
                resource.weight(), resource.criterionType(), resource.certificationName(), resource.mandatory(),
                resource.origin() == null ? CriterionOrigin.MANUAL : resource.origin());
    }
}
