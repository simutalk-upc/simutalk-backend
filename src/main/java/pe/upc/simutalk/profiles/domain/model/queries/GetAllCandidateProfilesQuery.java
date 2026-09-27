package pe.upc.simutalk.profiles.domain.model.queries;

/**
 * @param page zero-based page index
 */
public record GetAllCandidateProfilesQuery(int page, int size) {
}
