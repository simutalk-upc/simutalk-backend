package pe.upc.simutalk.iam.domain.model.commands;

/**
 * Creates the initial administrator if it does not exist yet. Credentials come from
 * environment variables, never from the repository.
 */
public record SeedAdminUserCommand(String username, String password) {
}
