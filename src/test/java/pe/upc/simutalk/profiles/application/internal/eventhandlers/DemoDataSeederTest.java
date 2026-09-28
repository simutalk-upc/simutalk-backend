package pe.upc.simutalk.profiles.application.internal.eventhandlers;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileCommandService;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileCommandService;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CandidateProfileRepository;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CompanyProfileRepository;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DemoDataSeederTest {

    private final IamContextFacade iam = mock(IamContextFacade.class);
    private final CompanyProfileRepository companyProfileRepository = mock(CompanyProfileRepository.class);
    private final CandidateProfileRepository candidateProfileRepository = mock(CandidateProfileRepository.class);

    private final DemoDataSeeder seeder = new DemoDataSeeder(true, "Demo12345", iam,
            mock(CompanyProfileCommandService.class), mock(CandidateProfileCommandService.class),
            companyProfileRepository, candidateProfileRepository,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    @Test
    void skipsWhenProfilesAlreadyExist() {
        when(companyProfileRepository.count()).thenReturn(1L);

        seeder.on(null);

        verifyNoInteractions(iam);
    }

    @Test
    void aFailureIsLoggedAndNeverPropagates() {
        when(iam.signUpUserIfAbsent(anyString(), anyString(), any())).thenThrow(new IllegalStateException("iam down"));

        assertThatCode(() -> seeder.on(null)).doesNotThrowAnyException();
    }
}
