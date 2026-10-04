package pe.upc.simutalk.recruitment.application.internal.commandservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.SubmitApplicationCommand;
import pe.upc.simutalk.recruitment.domain.model.events.ApplicationStatusChangedEvent;
import pe.upc.simutalk.enums.ApplicationStatus;
import pe.upc.simutalk.enums.CriterionType;
import pe.upc.simutalk.entities.Weight;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.JobPostingRepository;
import pe.upc.simutalk.exceptions.BusinessRuleViolationException;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationCommandServiceImplTest {

    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final JobPostingRepository postings = mock(JobPostingRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ApplicationCommandServiceImpl service =
            new ApplicationCommandServiceImpl(applications, postings, eventPublisher);

    @BeforeEach
    void setUp() {
        var posting = new JobPosting(new CreateJobPostingCommand("Analista", "SQL", 1L, null, false));
        ReflectionTestUtils.setField(posting, "id", 100L);
        posting.addCriterion("Análisis", "x", new Weight(100), CriterionType.COMPETENCY, null, false);
        posting.publish(criterionId -> 1L);
        when(postings.findById(100L)).thenReturn(Optional.of(posting));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void submitsApplication() {
        var application = service.handle(new SubmitApplicationCommand(100L, 7L));

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.RECEIVED);
        verify(applications).save(application);
    }

    @Test
    void secondApplicationOfTheSameCandidateIsRejected() {
        when(applications.existsByJobPostingIdAndCandidateId(100L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.handle(new SubmitApplicationCommand(100L, 7L)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(applications, never()).save(any());
    }

    @Test
    void unknownJobPostingIsNotFound() {
        assertThatThrownBy(() -> service.handle(new SubmitApplicationCommand(999L, 7L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void invalidTransitionIsPropagatedAsIllegalState() {
        var application = Application.submit(postings.findById(100L).orElseThrow(), 7L, false, Instant.now());
        when(applications.findById(500L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.handle(new ChangeApplicationStatusCommand(500L, ApplicationStatus.HIRED)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.RECEIVED);
    }

    @Test
    void publishesTheNewStageSoTheCandidateCanBeNotified() {
        var application = service.handle(new SubmitApplicationCommand(100L, 7L));
        ReflectionTestUtils.setField(application, "id", 55L);
        when(applications.findById(55L)).thenReturn(Optional.of(application));

        service.handle(new ChangeApplicationStatusCommand(55L, ApplicationStatus.REJECTED));

        verify(eventPublisher).publishEvent(new ApplicationStatusChangedEvent(55L, 100L, 7L, ApplicationStatus.REJECTED));
    }

    @Test
    void anInvalidTransitionPublishesNothing() {
        var application = service.handle(new SubmitApplicationCommand(100L, 7L));
        ReflectionTestUtils.setField(application, "id", 55L);
        when(applications.findById(55L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.handle(new ChangeApplicationStatusCommand(55L, ApplicationStatus.HIRED)))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(eventPublisher);
    }
}
