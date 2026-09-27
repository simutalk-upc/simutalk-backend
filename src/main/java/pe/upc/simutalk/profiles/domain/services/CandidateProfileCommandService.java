package pe.upc.simutalk.profiles.domain.services;

import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.commands.*;
import pe.upc.simutalk.profiles.domain.model.entities.Certification;
import pe.upc.simutalk.profiles.domain.model.valueobjects.CertificationVerification;

public interface CandidateProfileCommandService {

    CandidateProfile handle(CreateCandidateProfileCommand command);

    CandidateProfile handle(UpdateCandidateProfileCommand command);

    Certification handle(AddCertificationCommand command);

    CertificationVerification handle(VerifyCertificationCommand command);

    void handle(DeleteCertificationCommand command);
}
