package pe.upc.simutalk.recruitment.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.commands.*;
import pe.upc.simutalk.recruitment.domain.model.entities.EvaluationCriterion;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.Weight;
import pe.upc.simutalk.recruitment.domain.services.JobPostingCommandService;
import pe.upc.simutalk.recruitment.domain.services.JobPostingQueryService;
import pe.upc.simutalk.recruitment.interfaces.rest.authorization.RecruitmentAccessPolicy;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.*;
import pe.upc.simutalk.shared.domain.exceptions.BusinessRuleViolationException;
import pe.upc.simutalk.shared.interfaces.acl.IamContextFacade;
import pe.upc.simutalk.shared.interfaces.acl.ProfilesContextFacade;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Evaluates the real {@code @PreAuthorize} expressions of the recruitment controllers with
 * method security enabled, without web server or database. Company 1 owns job posting 100.
 */
@SpringJUnitWebConfig(RecruitmentAuthorizationTest.Config.class)
class RecruitmentAuthorizationTest {

    private static final long JOB_POSTING_ID = 100L;
    private static final long CRITERION_ID = 7L;

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean JobPostingCommandService jobPostingCommandService() { return mock(JobPostingCommandService.class); }
        @Bean JobPostingQueryService jobPostingQueryService() { return mock(JobPostingQueryService.class); }
        @Bean IamContextFacade iamContextFacade() { return mock(IamContextFacade.class); }
        @Bean ProfilesContextFacade profilesContextFacade() { return mock(ProfilesContextFacade.class); }

        @Bean(name = "recruitmentAccess")
        RecruitmentAccessPolicy recruitmentAccess(IamContextFacade iam, ProfilesContextFacade profiles,
                                                  JobPostingQueryService queries) {
            return new RecruitmentAccessPolicy(iam, profiles, queries);
        }

        @Bean JobPostingsController jobPostingsController(JobPostingCommandService commands,
                                                          JobPostingQueryService queries,
                                                          RecruitmentAccessPolicy policy) {
            return new JobPostingsController(commands, queries, policy);
        }

        @Bean JobPostingCriteriaController jobPostingCriteriaController(JobPostingCommandService commands) {
            return new JobPostingCriteriaController(commands);
        }
    }

    @Autowired JobPostingsController jobPostings;
    @Autowired JobPostingCriteriaController criteria;
    @Autowired JobPostingCommandService commandService;
    @Autowired JobPostingQueryService queryService;
    @Autowired IamContextFacade iam;
    @Autowired ProfilesContextFacade profiles;

    private JobPosting ownedByCompany1;
    private EvaluationCriterion criterion;

    @BeforeEach
    void setUp() {
        reset(commandService, queryService, iam, profiles);
        ownedByCompany1 = new JobPosting(new CreateJobPostingCommand("Backend", "APIs", 1L,
                LocalDate.now().plusDays(30), true));
        ReflectionTestUtils.setField(ownedByCompany1, "id", JOB_POSTING_ID);
        criterion = ownedByCompany1.addCriterion("Comunicación", "Claridad", new Weight(100),
                CriterionType.COMPETENCY, null, false);
        ReflectionTestUtils.setField(criterion, "id", CRITERION_ID);

        when(iam.fetchUserIdByUsername("recruiter.a")).thenReturn(10L);
        when(iam.fetchUserIdByUsername("recruiter.b")).thenReturn(20L);
        when(iam.fetchUserIdByUsername("recruiter.nocompany")).thenReturn(40L);
        when(iam.fetchUserIdByUsername("candidate")).thenReturn(30L);
        when(profiles.fetchCompanyIdByUserId(10L)).thenReturn(1L);
        when(profiles.fetchCompanyIdByUserId(20L)).thenReturn(2L);
        when(profiles.fetchCompanyIdByUserId(40L)).thenReturn(0L);
        when(profiles.fetchCompanyIdByUserId(30L)).thenReturn(0L);

        when(queryService.handle(any(GetJobPostingByIdQuery.class))).thenReturn(Optional.of(ownedByCompany1));
        when(commandService.handle(any(CreateJobPostingCommand.class))).thenReturn(ownedByCompany1);
        when(commandService.handle(any(UpdateJobPostingCommand.class))).thenReturn(ownedByCompany1);
        when(commandService.handle(any(ChangeJobPostingStatusCommand.class))).thenReturn(ownedByCompany1);
        when(commandService.handle(any(AddEvaluationCriterionCommand.class))).thenReturn(criterion);
        when(commandService.handle(any(UpdateEvaluationCriterionCommand.class))).thenReturn(criterion);
    }

    private List<Executable> everyWriteRoute() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var create = new CreateJobPostingResource("Backend", "APIs", LocalDate.now().plusDays(30), true);
        var update = new UpdateJobPostingResource("Backend Sr", "APIs", LocalDate.now().plusDays(30), true);
        var criterionBody = new CreateEvaluationCriterionResource("Liderazgo", "Guía", 10,
                CriterionType.COMPETENCY, null, false);
        var criterionUpdate = new UpdateEvaluationCriterionResource("Liderazgo", "Guía", 20,
                CriterionType.COMPETENCY, null, false);
        var status = new UpdateJobPostingStatusResource(JobPostingStatus.PUBLISHED);
        return List.of(
                () -> jobPostings.createJobPosting(create, auth),
                () -> jobPostings.updateJobPosting(JOB_POSTING_ID, update),
                () -> jobPostings.deleteJobPosting(JOB_POSTING_ID),
                () -> jobPostings.changeJobPostingStatus(JOB_POSTING_ID, status),
                () -> criteria.addCriterion(JOB_POSTING_ID, criterionBody),
                () -> criteria.updateCriterion(JOB_POSTING_ID, CRITERION_ID, criterionUpdate),
                () -> criteria.removeCriterion(JOB_POSTING_ID, CRITERION_ID));
    }

    @Test
    @WithMockUser(username = "candidate", roles = "CANDIDATE")
    void candidateIsForbiddenOnEveryWriteRoute() {
        assertAll(everyWriteRoute().stream()
                .map(route -> (Executable) () -> assertThrows(AccessDeniedException.class, route)));
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(username = "recruiter.b", roles = "RECRUITER")
    void recruiterOfAnotherCompanyIsForbiddenOnTheJobPosting() {
        var writesOnExistingPosting = everyWriteRoute().subList(1, 7);
        assertAll(writesOnExistingPosting.stream()
                .map(route -> (Executable) () -> assertThrows(AccessDeniedException.class, route)));
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(username = "recruiter.a", roles = "RECRUITER")
    void owningRecruiterCanWriteItsJobPosting() {
        var update = new UpdateJobPostingResource("Backend Sr", "APIs", LocalDate.now().plusDays(30), true);
        var criterionUpdate = new UpdateEvaluationCriterionResource("Comunicación", "Claridad", 100,
                CriterionType.COMPETENCY, null, false);

        assertThat(jobPostings.updateJobPosting(JOB_POSTING_ID, update).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jobPostings.changeJobPostingStatus(JOB_POSTING_ID,
                new UpdateJobPostingStatusResource(JobPostingStatus.PUBLISHED)).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(criteria.updateCriterion(JOB_POSTING_ID, CRITERION_ID, criterionUpdate).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(criteria.removeCriterion(JOB_POSTING_ID, CRITERION_ID).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(jobPostings.deleteJobPosting(JOB_POSTING_ID).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "recruiter.a", roles = "RECRUITER")
    void createTakesTheCompanyFromTheAuthenticatedRecruiter() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var response = jobPostings.createJobPosting(
                new CreateJobPostingResource("Backend", "APIs", LocalDate.now().plusDays(30), true), auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var command = ArgumentCaptor.forClass(CreateJobPostingCommand.class);
        verify(commandService).handle(command.capture());
        assertThat(command.getValue().companyId()).isEqualTo(1L);
    }

    @Test
    @WithMockUser(username = "recruiter.nocompany", roles = "RECRUITER")
    void recruiterWithoutCompanyProfileCannotCreate() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertThatThrownBy(() -> jobPostings.createJobPosting(
                new CreateJobPostingResource("Backend", "APIs", null, false), auth))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(commandService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanWriteAnyJobPosting() {
        var update = new UpdateJobPostingResource("Backend Sr", "APIs", LocalDate.now().plusDays(30), true);

        assertThat(jobPostings.updateJobPosting(JOB_POSTING_ID, update).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jobPostings.deleteJobPosting(JOB_POSTING_ID).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "candidate", roles = "CANDIDATE")
    void readsStayOpenToAnyAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertThat(jobPostings.getJobPostingById(JOB_POSTING_ID, auth).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jobPostings.getJobPostings(null, null, auth).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
