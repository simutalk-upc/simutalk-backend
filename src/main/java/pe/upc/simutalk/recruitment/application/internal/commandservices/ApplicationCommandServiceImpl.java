package pe.upc.simutalk.recruitment.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.entities.Application;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.SubmitApplicationCommand;
import pe.upc.simutalk.recruitment.domain.model.events.ApplicationStatusChangedEvent;
import pe.upc.simutalk.recruitment.domain.services.ApplicationCommandService;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.JobPostingRepository;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationCommandServiceImpl implements ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Application handle(SubmitApplicationCommand command) {
        var jobPosting = jobPostingRepository.findById(command.jobPostingId())
                .orElseThrow(() -> new ResourceNotFoundException("Job posting", command.jobPostingId()));
        var alreadyApplied = applicationRepository.existsByJobPostingIdAndCandidateId(
                command.jobPostingId(), command.candidateId());
        return applicationRepository.save(
                Application.submit(jobPosting, command.candidateId(), alreadyApplied, Instant.now()));
    }

    @Override
    public Application handle(ChangeApplicationStatusCommand command) {
        var application = applicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", command.applicationId()));
        application.changeStatus(command.status());
        applicationRepository.flush();
        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(application.getId(), application.getJobPostingId(),
                application.getCandidateId(), application.getStatus()));
        return application;
    }
}
