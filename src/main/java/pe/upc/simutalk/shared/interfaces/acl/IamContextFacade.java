package pe.upc.simutalk.shared.interfaces.acl;

/**
 * Contract other bounded contexts use to ask {@code iam} about users.
 * <p>
 * It lives in {@code shared} so that callers (e.g. {@code profiles}, {@code recruitment})
 * depend only on {@code shared}; {@code iam} provides the implementation. Never use
 * iam's repositories or domain classes from another context.
 */
public interface IamContextFacade {

    /**
     * @return the user id, or {@code 0L} if no user has that username
     */
    Long fetchUserIdByUsername(String username);

    /**
     * @return the username, or an empty string if the user does not exist
     */
    String fetchUsernameByUserId(Long userId);

    boolean existsUserById(Long userId);
}
