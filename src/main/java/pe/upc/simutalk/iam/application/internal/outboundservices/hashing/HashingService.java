package pe.upc.simutalk.iam.application.internal.outboundservices.hashing;

/**
 * Password hashing as seen by the application layer.
 */
public interface HashingService {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
