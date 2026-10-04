package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.entities.EvaluationCriterion;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.EvaluationCriterionResource;

public class EvaluationCriterionResourceFromEntityAssembler {

    public static EvaluationCriterionResource toResourceFromEntity(EvaluationCriterion entity) {
        return new EvaluationCriterionResource(entity.getId(), entity.getJobPostingId(), entity.getName(),
                entity.getDescription(), entity.getWeight().value(), entity.getCriterionType(),
                entity.getCertificationName(), entity.isMandatory(), entity.getOrigin(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
