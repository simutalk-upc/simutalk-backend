package pe.upc.simutalk.profiles.interfaces.acl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.profiles.domain.model.aggregates.CandidateProfile;
import pe.upc.simutalk.profiles.domain.model.commands.CreateCandidateProfileCommand;
import pe.upc.simutalk.profiles.domain.model.queries.*;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileQueryService;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileQueryService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfilesContextFacadeImplTest {

    private final CandidateProfileQueryService candidates = mock(CandidateProfileQueryService.class);
    private final CompanyProfileQueryService companies = mock(CompanyProfileQueryService.class);
    private final ProfilesContextFacadeImpl facade = new ProfilesContextFacadeImpl(candidates, companies);

    @Test
    void resolvesCandidateData() {
        var candidate = new CandidateProfile(new CreateCandidateProfileCommand(10L, "Rosa", "Quispe", "45879632",
                LocalDate.now().minusYears(25), "+51987654321", "Comas", 2));
        ReflectionTestUtils.setField(candidate, "id", 3L);
        when(candidates.handle(new GetCandidateProfileByUserIdQuery(10L))).thenReturn(Optional.of(candidate));
        when(candidates.handle(new GetCandidateProfileByIdQuery(3L))).thenReturn(Optional.of(candidate));
        when(candidates.handle(new GetVerifiedCertificationCountQuery(3L))).thenReturn(2L);

        assertThat(facade.fetchCandidateIdByUserId(10L)).isEqualTo(3L);
        assertThat(facade.fetchCandidateDistrict(3L)).isEqualTo("Comas");
        assertThat(facade.fetchVerifiedCertificationCount(3L)).isEqualTo(2L);
    }

    @Test
    void returnsNeutralValuesWhenMissing() {
        when(candidates.handle(any(GetCandidateProfileByUserIdQuery.class))).thenReturn(Optional.empty());
        when(candidates.handle(any(GetCandidateProfileByIdQuery.class))).thenReturn(Optional.empty());
        when(companies.handle(any(GetCompanyProfileByUserIdQuery.class))).thenReturn(Optional.empty());

        assertThat(facade.fetchCandidateIdByUserId(99L)).isZero();
        assertThat(facade.fetchCompanyIdByUserId(99L)).isZero();
        assertThat(facade.fetchCandidateDistrict(99L)).isEmpty();
        assertThat(facade.fetchVerifiedCertificationCount(null)).isZero();
    }
}
