package pe.upc.simutalk.dtos;

import pe.upc.simutalk.entities.Certification;
import pe.upc.simutalk.services.CredentialVerificationService.VerificationResult;

/**
 * Outcome of a verification request: the certification in its resulting state and
 * what the issuer answered.
 */
public record CertificationVerification(Certification certification, VerificationResult result) {
}
