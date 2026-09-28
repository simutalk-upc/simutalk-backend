package pe.upc.simutalk.assessment.application.internal.eventhandlers;

import org.junit.jupiter.api.Test;
import pe.upc.simutalk.assessment.domain.services.AnswerScoringService;
import pe.upc.simutalk.assessment.domain.services.AssessmentCommandService;
import pe.upc.simutalk.assessment.infrastructure.persistence.jpa.repositories.AssessmentRepository;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.InterviewsContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.RecruitmentContextFacade;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentDemoDataSeederTest {

    @Test
    void aFailureIsLoggedAndNeverPropagates() {
        var iam = mock(IamContextFacade.class);
        when(iam.fetchUserIdByUsername(any())).thenThrow(new IllegalStateException("iam down"));
        var seeder = new AssessmentDemoDataSeeder(true, mock(AssessmentCommandService.class),
                mock(AnswerScoringService.class), mock(AssessmentRepository.class), iam,
                mock(ProfilesContextFacade.class), mock(RecruitmentContextFacade.class),
                mock(InterviewsContextFacade.class));

        assertThatCode(() -> seeder.on(null)).doesNotThrowAnyException();
    }
}
