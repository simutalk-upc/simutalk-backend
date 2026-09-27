package pe.upc.simutalk.iam.infrastructure.hashing.bcrypt.services;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Joins the application hashing port with Spring Security's {@link PasswordEncoder},
 * so a single bean serves both the sign-up/sign-in flow and Spring Security.
 */
public interface HashingService
        extends pe.upc.simutalk.iam.application.internal.outboundservices.hashing.HashingService, PasswordEncoder {
}
