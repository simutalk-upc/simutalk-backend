package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.entities.Application;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.ApplicationResource;

public class ApplicationResourceFromEntityAssembler {

    public static ApplicationResource toResourceFromEntity(Application entity) {
        return new ApplicationResource(entity.getId(), entity.getJobPostingId(), entity.getCandidateId(),
                entity.getStatus(), entity.getAppliedAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
