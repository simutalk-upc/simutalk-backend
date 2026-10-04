package pe.upc.simutalk.assessment.interfaces.rest.transform;

import pe.upc.simutalk.entities.Evidence;
import pe.upc.simutalk.dtos.EvidenceResource;

public class EvidenceResourceFromEntityAssembler {

    public static EvidenceResource toResourceFromEntity(Evidence entity) {
        return new EvidenceResource(entity.getId(), entity.getAnswerId(), entity.getExcerpt(), entity.getStartOffset(),
                entity.getEndOffset());
    }
}
