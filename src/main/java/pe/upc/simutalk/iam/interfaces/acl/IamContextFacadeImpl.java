package pe.upc.simutalk.iam.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.iam.domain.model.aggregates.User;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByIdQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByUsernameQuery;
import pe.upc.simutalk.iam.domain.services.UserQueryService;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;

/**
 * iam's implementation of the {@link IamContextFacade} contract published in shared.
 */
@Service
@RequiredArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserQueryService userQueryService;

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
}
