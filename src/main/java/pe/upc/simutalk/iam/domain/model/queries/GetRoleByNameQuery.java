package pe.upc.simutalk.iam.domain.model.queries;

import pe.upc.simutalk.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}
