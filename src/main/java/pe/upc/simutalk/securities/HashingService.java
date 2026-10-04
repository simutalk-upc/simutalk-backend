package pe.upc.simutalk.securities;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing contract for the whole application.
 *
 * <p>Before the layered layout there were two interfaces with this name: an
 * application port that declared {@code encode} and {@code matches}, and an
 * infrastructure interface that joined that port with Spring Security's
 * {@link PasswordEncoder}. Both declared the same two methods that
 * {@code PasswordEncoder} already defines, so flattening the packages merged
 * them into this single contract without losing anything: one bean still
 * serves both the sign-up/sign-in flow and Spring Security.
 */
public interface HashingService extends PasswordEncoder {
}
