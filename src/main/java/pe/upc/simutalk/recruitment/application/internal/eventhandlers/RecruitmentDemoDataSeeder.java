package pe.upc.simutalk.recruitment.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import pe.upc.simutalk.recruitment.domain.model.commands.*;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;
import pe.upc.simutalk.recruitment.domain.services.ApplicationCommandService;
import pe.upc.simutalk.recruitment.domain.services.JobPostingCommandService;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Recruitment part of the demo data ({@code app.seed-demo-data=true}). Runs after the
 * profiles DemoDataSeeder: publishes a job posting for Consultora Andina and makes the six
 * demo candidates apply, spread over RECEIVED, INTERVIEWING and ASSESSED so every column of
 * the recruiter's pipeline has data. Users and profiles are resolved through the shared
 * facades; recruitment never imports profiles classes. Idempotent: skipped once any
 * application exists.
 */
@Slf4j
@Service
public class RecruitmentDemoDataSeeder {

    private static final String COMPANY_USERNAME = "consultora.andina";

    /** Demo candidates created by the profiles seeder, with the stage their application reaches. */
    private static final Map<String, ApplicationStatus> DEMO_CANDIDATES = Map.of(
            "rosa.quispe", ApplicationStatus.ASSESSED,
            "jorge.huaman", ApplicationStatus.ASSESSED,
            "carmen.ramos", ApplicationStatus.INTERVIEWING,
            "diego.salazar", ApplicationStatus.INTERVIEWING,
            "lucia.flores", ApplicationStatus.RECEIVED,
            "miguel.condori", ApplicationStatus.RECEIVED);

    private static final List<String> APPLICATION_ORDER = List.of(
            "rosa.quispe", "jorge.huaman", "lucia.flores", "miguel.condori", "carmen.ramos", "diego.salazar");

    private final boolean enabled;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final JobPostingCommandService jobPostingCommandService;
    private final ApplicationCommandService applicationCommandService;
    private final ApplicationRepository applicationRepository;
    private final TransactionTemplate transactionTemplate;

    public RecruitmentDemoDataSeeder(@Value("${app.seed-demo-data:false}") boolean enabled,
                                     IamContextFacade iamContextFacade,
                                     ProfilesContextFacade profilesContextFacade,
                                     JobPostingCommandService jobPostingCommandService,
                                     ApplicationCommandService applicationCommandService,
                                     ApplicationRepository applicationRepository,
                                     TransactionTemplate transactionTemplate) {
        this.enabled = enabled;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.jobPostingCommandService = jobPostingCommandService;
        this.applicationCommandService = applicationCommandService;
        this.applicationRepository = applicationRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(200)
    public void on(ApplicationReadyEvent event) {
        if (!enabled) {
            return;
        }
        if (applicationRepository.count() > 0) {
            log.info("Recruitment demo data skipped: applications already exist");
            return;
        }
        var companyId = profilesContextFacade.fetchCompanyIdByUserId(userIdOf(COMPANY_USERNAME));
        if (companyId == 0L) {
            log.warn("Recruitment demo data skipped: demo company '{}' not found", COMPANY_USERNAME);
            return;
        }
        transactionTemplate.executeWithoutResult(status -> seed(companyId));
        log.info("Recruitment demo data seeded: 1 published job posting and {} applications", DEMO_CANDIDATES.size());
    }

    private void seed(Long companyId) {
        var jobPosting = jobPostingCommandService.handle(new CreateJobPostingCommand(
                "Analista de Datos Junior",
                "Análisis de datos comerciales con SQL, Excel y Python; elaboración de reportes para clientes.",
                companyId, LocalDate.now().plusDays(60), true));
        var jobPostingId = jobPosting.getId();
        jobPostingCommandService.handle(new AddEvaluationCriterionCommand(jobPostingId, "Pensamiento analítico",
                "Descompone problemas de negocio y los resuelve con datos.", 40, CriterionType.COMPETENCY, null, false));
        jobPostingCommandService.handle(new AddEvaluationCriterionCommand(jobPostingId, "Comunicación efectiva",
                "Explica hallazgos a públicos no técnicos con claridad.", 30, CriterionType.COMPETENCY, null, false));
        jobPostingCommandService.handle(new AddEvaluationCriterionCommand(jobPostingId, "Certificación en análisis de datos",
                "Certificación reconocida en análisis de datos.", 30, CriterionType.CERTIFICATION,
                "Google Data Analytics", false));
        jobPostingCommandService.handle(new ChangeJobPostingStatusCommand(jobPostingId, JobPostingStatus.PUBLISHED));

        for (var username : APPLICATION_ORDER) {
            var candidateId = profilesContextFacade.fetchCandidateIdByUserId(userIdOf(username));
            if (candidateId == 0L) {
                log.warn("Demo candidate '{}' has no profile; skipped", username);
                continue;
            }
            var application = applicationCommandService.handle(new SubmitApplicationCommand(jobPostingId, candidateId));
            advance(application.getId(), DEMO_CANDIDATES.get(username));
        }
    }

    /** Walks the directed transitions from RECEIVED up to {@code target}. */
    private void advance(Long applicationId, ApplicationStatus target) {
        for (var next : List.of(ApplicationStatus.INTERVIEWING, ApplicationStatus.ASSESSED)) {
            if (target.ordinal() < next.ordinal()) {
                return;
            }
            applicationCommandService.handle(new ChangeApplicationStatusCommand(applicationId, next));
        }
    }

    private Long userIdOf(String username) {
        var userId = iamContextFacade.fetchUserIdByUsername(username);
        return userId == null ? 0L : userId;
    }
}
