package pe.upc.simutalk.profiles.interfaces.rest.resources;

/**
 * @param conclusive false when the issuer could not be consulted; the certification stays UNVERIFIED
 */
public record CertificationVerificationResource(
        CertificationResource certification,
        boolean matched,
        boolean conclusive,
        String detail) {
}
