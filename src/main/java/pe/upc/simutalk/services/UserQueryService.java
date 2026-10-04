package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.User;
import pe.upc.simutalk.iam.domain.model.queries.GetAllUsersQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByIdQuery;
import pe.upc.simutalk.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    List<User> handle(GetAllUsersQuery query);

    Optional<User> handle(GetUserByIdQuery query);

    Optional<User> handle(GetUserByUsernameQuery query);
}
