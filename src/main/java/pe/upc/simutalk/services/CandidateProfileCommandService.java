package pe.upc.simutalk.services;

import pe.upc.simutalk.profiles.domain.model.commands.AddCertificationCommand;
import pe.upc.simutalk.profiles.domain.model.commands.CreateCandidateProfileCommand;
import pe.upc.simutalk.profiles.domain.model.commands.DeleteCertificationCommand;
import pe.upc.simutalk.profiles.domain.model.commands.UpdateCandidateProfileCommand;
import pe.upc.simutalk.profiles.domain.model.commands.VerifyCertificationCommand;

import pe.upc.simutalk.entities.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.commands.*;
import pe.upc.simutalk.entities.Certification;
import pe.upc.simutalk.profiles.domain.model.valueobjects.CertificationVerification;

public interface CandidateProfileCommandService {

    CandidateProfile handle(CreateCandidateProfileCommand command);

    CandidateProfile handle(UpdateCandidateProfileCommand command);

    Certification handle(AddCertificationCommand command);

    CertificationVerification handle(VerifyCertificationCommand command);

    void handle(DeleteCertificationCommand command);
}
