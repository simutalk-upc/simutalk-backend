package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.entities.JobPosting;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.JobPostingResource;

public class JobPostingResourceFromEntityAssembler {

    public static JobPostingResource toResourceFromEntity(JobPosting entity) {
        var criteria = entity.getCriteria().stream()
                .map(EvaluationCriterionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return new JobPostingResource(entity.getId(), entity.getTitle(), entity.getDescription(),
                entity.getCompanyId().value(), entity.getStatus(), entity.getClosingDate(),
                entity.isAnonymizedScreening(), entity.getTotalWeight(), criteria,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
