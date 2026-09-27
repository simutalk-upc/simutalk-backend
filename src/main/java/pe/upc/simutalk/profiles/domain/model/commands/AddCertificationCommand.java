package pe.upc.simutalk.profiles.domain.model.commands;

import java.time.LocalDate;

public record AddCertificationCommand(
        Long candidateId,
        String title,
        String issuer,
        String credentialCode,
        LocalDate issuedAt,
        LocalDate expiresAt) {
}
