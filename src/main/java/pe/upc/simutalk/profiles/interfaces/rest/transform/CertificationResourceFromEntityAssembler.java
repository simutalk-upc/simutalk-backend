package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.entities.Certification;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CertificationResource;

import java.time.LocalDate;

public class CertificationResourceFromEntityAssembler {

    public static CertificationResource toResourceFromEntity(Certification entity) {
        return new CertificationResource(entity.getId(), entity.getTitle(), entity.getIssuer(),
                entity.getCredentialCode(), entity.getIssuedAt(), entity.getExpiresAt(),
                entity.isExpired(LocalDate.now()), entity.getVerificationStatus(), entity.getVerifiedAt(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
