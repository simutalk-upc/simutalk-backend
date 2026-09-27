package pe.upc.simutalk.profiles.domain.model.commands;

import java.time.LocalDate;

/** The document number is not part of the update: it cannot change. */
public record UpdateCandidateProfileCommand(
        Long candidateId,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String phone,
        String district,
        int yearsOfExperience) {
}
