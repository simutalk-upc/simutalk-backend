package pe.upc.simutalk.profiles.domain.model.queries;

/**
 * @param page zero-based page index
 */
public record GetAllCompanyProfilesQuery(int page, int size) {
}
