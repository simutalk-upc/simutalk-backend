package pe.upc.simutalk.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.entities.User;
import pe.upc.simutalk.iam.domain.model.commands.SignUpCommand;
import pe.upc.simutalk.entities.Role;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByIdQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByUsernameQuery;
import pe.upc.simutalk.services.UserCommandService;
import pe.upc.simutalk.services.UserQueryService;
import pe.upc.simutalk.services.IamContextFacade;

import java.util.List;

/**
 * iam's implementation of the {@link IamContextFacade} contract published in shared.
 */
@Service
@RequiredArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @Override
    public Long fetchUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return 0L;
        }
        return userQueryService.handle(new GetUserByUsernameQuery(username))
                .map(User::getId)
                .orElse(0L);
    }

    @Override
    public String fetchUsernameByUserId(Long userId) {
        if (userId == null) {
            return "";
        }
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(User::getUsername)
                .orElse("");
    }

    @Override
    public boolean existsUserById(Long userId) {
        return userId != null && userQueryService.handle(new GetUserByIdQuery(userId)).isPresent();
    }

    @Override
    public Long signUpUserIfAbsent(String username, String password, String roleName) {
        return userQueryService.handle(new GetUserByUsernameQuery(username))
                .or(() -> userCommandService.handle(
                        new SignUpCommand(username, password, List.of(Role.toRoleFromName(roleName)))))
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Could not sign up user " + username));
    }
}
