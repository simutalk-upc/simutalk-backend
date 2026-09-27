package pe.upc.simutalk.profiles.domain.model.commands;

import java.time.LocalDate;

public record CreateCandidateProfileCommand(
        Long userId,
        String firstName,
        String lastName,
        String documentNumber,
        LocalDate birthDate,
        String phone,
        String district,
        int yearsOfExperience) {
}
