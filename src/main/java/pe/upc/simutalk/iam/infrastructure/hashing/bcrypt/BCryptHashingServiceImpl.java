package pe.upc.simutalk.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.upc.simutalk.iam.infrastructure.hashing.bcrypt.services.HashingService;

/**
 * BCrypt implementation. Registered as the {@code passwordEncoder} bean in
 * {@code WebSecurityConfiguration} so there is exactly one PasswordEncoder.
 */
public class BCryptHashingServiceImpl extends BCryptPasswordEncoder implements HashingService {
}
