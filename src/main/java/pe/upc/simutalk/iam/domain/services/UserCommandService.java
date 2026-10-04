package pe.upc.simutalk.iam.domain.services;

import pe.upc.simutalk.entities.User;
import pe.upc.simutalk.iam.domain.model.commands.SeedAdminUserCommand;
import pe.upc.simutalk.iam.domain.model.commands.SignInCommand;
import pe.upc.simutalk.iam.domain.model.commands.SignUpCommand;
import pe.upc.simutalk.iam.domain.model.valueobjects.AuthenticatedUser;

import java.util.Optional;

public interface UserCommandService {

    Optional<User> handle(SignUpCommand command);

    Optional<AuthenticatedUser> handle(SignInCommand command);

    void handle(SeedAdminUserCommand command);
}
