package pe.upc.simutalk.iam.interfaces.acl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.iam.domain.model.aggregates.User;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByIdQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByUsernameQuery;
import pe.upc.simutalk.iam.domain.services.UserQueryService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IamContextFacadeImplTest {

    private final UserQueryService userQueryService = mock(UserQueryService.class);
    private final IamContextFacadeImpl facade = new IamContextFacadeImpl(userQueryService);

    @Test
    void resolvesExistingUser() {
        var user = new User("ana", "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        when(userQueryService.handle(new GetUserByUsernameQuery("ana"))).thenReturn(Optional.of(user));
        when(userQueryService.handle(new GetUserByIdQuery(7L))).thenReturn(Optional.of(user));

        assertThat(facade.fetchUserIdByUsername("ana")).isEqualTo(7L);
        assertThat(facade.fetchUsernameByUserId(7L)).isEqualTo("ana");
        assertThat(facade.existsUserById(7L)).isTrue();
    }

    @Test
    void returnsNeutralValuesForMissingUser() {
        when(userQueryService.handle(any(GetUserByUsernameQuery.class))).thenReturn(Optional.empty());
        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.empty());

        assertThat(facade.fetchUserIdByUsername("nobody")).isZero();
        assertThat(facade.fetchUsernameByUserId(99L)).isEmpty();
        assertThat(facade.existsUserById(99L)).isFalse();
        assertThat(facade.existsUserById(null)).isFalse();
    }
}
