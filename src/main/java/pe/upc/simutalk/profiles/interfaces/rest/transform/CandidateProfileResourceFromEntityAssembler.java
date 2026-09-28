package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CandidateProfileResource;

import java.time.LocalDate;

public class CandidateProfileResourceFromEntityAssembler {

    public static CandidateProfileResource toResourceFromEntity(CandidateProfile entity) {
        var certifications = entity.getCertifications().stream()
                .map(CertificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return new CandidateProfileResource(entity.getId(), entity.getUserId(),
                entity.getPersonName().firstName(), entity.getPersonName().lastName(),
                entity.getDocumentNumber().documentType(), entity.getDocumentNumber().value(),
                entity.getEmail() == null ? null : entity.getEmail().value(), entity.getBirthDate(), entity.getPhone(), entity.getDistrict(), entity.getYearsOfExperience(),
                entity.countCertificationsForScoring(LocalDate.now()), certifications,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
