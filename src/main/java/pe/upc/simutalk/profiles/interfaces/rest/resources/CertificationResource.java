package pe.upc.simutalk.profiles.interfaces.rest.resources;

import pe.upc.simutalk.profiles.domain.model.valueobjects.VerificationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record CertificationResource(
        Long id,
        String title,
        String issuer,
        String credentialCode,
        LocalDate issuedAt,
        LocalDate expiresAt,
        boolean expired,
        VerificationStatus verificationStatus,
        Instant verifiedAt,
        Instant createdAt,
        Instant updatedAt) {
}
