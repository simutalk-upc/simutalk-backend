package pe.upc.simutalk.shared.domain.exceptions;

/**
 * Thrown when a sign-in attempt fails. The message never reveals whether the
 * username exists. Mapped to HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
