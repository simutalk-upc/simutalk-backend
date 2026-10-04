package pe.upc.simutalk.interviews.interfaces.rest;

import pe.upc.simutalk.interviews.domain.model.commands.CreateInterviewSessionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.CreateQuestionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.RecordAnswerCommand;
import pe.upc.simutalk.interviews.domain.model.commands.StartInterviewSessionCommand;
import pe.upc.simutalk.interviews.domain.model.queries.GetAnswersByInterviewSessionIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByApplicationIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetInterviewSessionByIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionSuggestionsQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionsByJobPostingIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.HasInterviewSessionInProgressQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.entities.InterviewSession;
import pe.upc.simutalk.entities.Question;
import pe.upc.simutalk.interviews.domain.model.commands.*;
import pe.upc.simutalk.entities.Answer;
import pe.upc.simutalk.interviews.domain.model.queries.*;
import pe.upc.simutalk.enums.QuestionOrigin;
import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionSuggestion;
import pe.upc.simutalk.services.InterviewSessionCommandService;
import pe.upc.simutalk.services.InterviewSessionQueryService;
import pe.upc.simutalk.services.QuestionCommandService;
import pe.upc.simutalk.services.QuestionQueryService;
import pe.upc.simutalk.services.QuestionSuggestionQueryService;
import pe.upc.simutalk.interviews.interfaces.rest.authorization.InterviewsAccessPolicy;
import pe.upc.simutalk.interviews.interfaces.rest.resources.CreateInterviewSessionResource;
import pe.upc.simutalk.interviews.interfaces.rest.resources.CreateQuestionResource;
import pe.upc.simutalk.interviews.interfaces.rest.resources.RecordAnswerResource;
import pe.upc.simutalk.services.IamContextFacade;
import pe.upc.simutalk.services.ProfilesContextFacade;
import pe.upc.simutalk.services.RecruitmentContextFacade;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Real {@code @PreAuthorize} expressions of the interviews controllers. Company 1 owns job
 * posting 10; application 50 belongs to candidate 7 (user "rosa") and has session 900.
 */
@SpringJUnitWebConfig(InterviewsAuthorizationTest.Config.class)
class InterviewsAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean QuestionCommandService questionCommandService() { return mock(QuestionCommandService.class); }
        @Bean QuestionQueryService questionQueryService() { return mock(QuestionQueryService.class); }
        @Bean QuestionSuggestionQueryService questionSuggestionQueryService() { return mock(QuestionSuggestionQueryService.class); }
        @Bean InterviewSessionCommandService interviewSessionCommandService() { return mock(InterviewSessionCommandService.class); }
        @Bean InterviewSessionQueryService interviewSessionQueryService() { return mock(InterviewSessionQueryService.class); }
        @Bean IamContextFacade iamContextFacade() { return mock(IamContextFacade.class); }
        @Bean ProfilesContextFacade profilesContextFacade() { return mock(ProfilesContextFacade.class); }
        @Bean RecruitmentContextFacade recruitmentContextFacade() { return mock(RecruitmentContextFacade.class); }

        @Bean(name = "interviewsAccess")
        InterviewsAccessPolicy interviewsAccess(IamContextFacade iam, ProfilesContextFacade profiles,
                                                RecruitmentContextFacade recruitment, InterviewSessionQueryService sessions) {
            return new InterviewsAccessPolicy(iam, profiles, recruitment, sessions);
        }

        @Bean QuestionsController questionsController(QuestionCommandService commands, QuestionQueryService queries,
                                                      QuestionSuggestionQueryService suggestions) {
            return new QuestionsController(commands, queries, suggestions);
        }

        @Bean InterviewSessionsController interviewSessionsController(InterviewSessionCommandService commands,
                                                                      InterviewSessionQueryService queries) {
            return new InterviewSessionsController(commands, queries);
        }
    }

    @Autowired QuestionsController questions;
    @Autowired InterviewSessionsController sessions;
    @Autowired QuestionCommandService questionCommands;
    @Autowired QuestionQueryService questionQueries;
    @Autowired QuestionSuggestionQueryService questionSuggestions;
    @Autowired InterviewSessionCommandService sessionCommands;
    @Autowired InterviewSessionQueryService sessionQueries;
    @Autowired IamContextFacade iam;
    @Autowired ProfilesContextFacade profiles;
    @Autowired RecruitmentContextFacade recruitment;

    private InterviewSession session;

    @BeforeEach
    void setUp() {
        reset(questionCommands, questionQueries, questionSuggestions, sessionCommands, sessionQueries, iam, profiles, recruitment);
        var question = new Question(10L, 11L, "¿Qué es un LEFT JOIN?", 120, 1, QuestionOrigin.MANUAL, false);
        ReflectionTestUtils.setField(question, "id", 5L);
        session = new InterviewSession(50L, 10L, 7L, LocalDate.now().plusDays(7), Instant.now());
        ReflectionTestUtils.setField(session, "id", 900L);
        var answer = new Answer(5L, "Devuelve todas las filas de la izquierda.", null, 60, Instant.now(), false, null);

        when(iam.fetchUserIdByUsername("andina")).thenReturn(2L);
        when(iam.fetchUserIdByUsername("otra")).thenReturn(20L);
        when(iam.fetchUserIdByUsername("rosa")).thenReturn(3L);
        when(iam.fetchUserIdByUsername("jorge")).thenReturn(4L);
        when(profiles.fetchCompanyIdByUserId(2L)).thenReturn(1L);
        when(profiles.fetchCompanyIdByUserId(20L)).thenReturn(2L);
        when(profiles.fetchCandidateIdByUserId(3L)).thenReturn(7L);
        when(profiles.fetchCandidateIdByUserId(4L)).thenReturn(8L);
        when(recruitment.fetchCompanyIdByJobPostingId(10L)).thenReturn(1L);
        when(recruitment.fetchJobPostingIdByApplicationId(50L)).thenReturn(10L);
        when(recruitment.fetchCandidateIdByApplicationId(50L)).thenReturn(7L);

        when(questionQueries.handle(any(GetQuestionsByJobPostingIdQuery.class))).thenReturn(List.of(question));
        when(questionCommands.handle(any(CreateQuestionCommand.class))).thenReturn(question);
        when(sessionQueries.handle(any(GetInterviewSessionByIdQuery.class))).thenReturn(Optional.of(session));
        when(sessionQueries.handle(any(GetInterviewSessionByApplicationIdQuery.class))).thenReturn(Optional.of(session));
        when(sessionQueries.handle(any(GetAnswersByInterviewSessionIdQuery.class))).thenReturn(List.of(answer));
        when(sessionQueries.handle(any(HasInterviewSessionInProgressQuery.class))).thenReturn(false);
        when(sessionCommands.handle(any(CreateInterviewSessionCommand.class))).thenReturn(session);
        when(sessionCommands.handle(any(StartInterviewSessionCommand.class))).thenReturn(session);
        when(sessionCommands.handle(any(RecordAnswerCommand.class))).thenReturn(answer);
    }

    private static CreateQuestionResource questionBody() {
        return new CreateQuestionResource(11L, "¿Qué es un LEFT JOIN?", 120, null, false);
    }

    private static RecordAnswerResource answerBody() {
        return new RecordAnswerResource(5L, "Devuelve todas las filas de la izquierda.", null, 60, false, null);
    }

    @Test
    @WithMockUser(username = "andina", roles = "RECRUITER")
    void owningRecruiterManagesScriptAndInvites() {
        assertThat(questions.createQuestion(10L, questionBody()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(questions.getQuestions(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sessions.createSession(50L, new CreateInterviewSessionResource(LocalDate.now().plusDays(7)))
                .getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(sessions.getAnswers(900L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @WithMockUser(username = "otra", roles = "RECRUITER")
    void recruiterOfAnotherCompanyIsForbidden() {
        assertThatThrownBy(() -> questions.createQuestion(10L, questionBody())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> questions.getQuestions(10L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.createSession(50L, new CreateInterviewSessionResource(LocalDate.now())))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.getAnswers(900L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(questionCommands, sessionCommands);
    }

    @Test
    @WithMockUser(username = "andina", roles = "RECRUITER")
    void owningRecruiterCanAskForQuestionSuggestions() {
        when(questionSuggestions.handle(new GetQuestionSuggestionsQuery(10L, 11L)))
                .thenReturn(List.of(new QuestionSuggestion("¿Cómo validas un reporte?", "Rigor")));

        var response = questions.suggestQuestions(10L, 11L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).criterionId()).isEqualTo(11L);
        assertThat(response.getBody().get(0).origin()).isEqualTo(QuestionOrigin.AI_SUGGESTED);
        verifyNoInteractions(questionCommands);
    }

    @Test
    void onlyTheOwningRecruiterCanAskForQuestionSuggestions() {
        for (var user : List.of(
                User.withUsername("otra").password("x").roles("RECRUITER").build(),
                User.withUsername("rosa").password("x").roles("CANDIDATE").build(),
                User.withUsername("admin").password("x").roles("ADMIN").build())) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, "x", user.getAuthorities()));
            try {
                assertThatThrownBy(() -> questions.suggestQuestions(10L, 11L))
                        .as(user.getUsername()).isInstanceOf(AccessDeniedException.class);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        verifyNoInteractions(questionSuggestions);
    }

    @Test
    @WithMockUser(username = "rosa", roles = "CANDIDATE")
    void candidateCannotEditTheScriptNorInvite() {
        assertThatThrownBy(() -> questions.createQuestion(10L, questionBody())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.createSession(50L, new CreateInterviewSessionResource(LocalDate.now())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "rosa", roles = "CANDIDATE")
    void candidateReadsTheScriptOnlyWithAnInterviewInProgress() {
        assertThatThrownBy(() -> questions.getQuestions(10L)).isInstanceOf(AccessDeniedException.class);

        when(sessionQueries.handle(new HasInterviewSessionInProgressQuery(10L, 7L))).thenReturn(true);

        assertThat(questions.getQuestions(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @WithMockUser(username = "rosa", roles = "CANDIDATE")
    void owningCandidateRunsTheInterview() {
        assertThat(sessions.getSessionByApplication(50L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sessions.startSession(900L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sessions.recordAnswer(900L, answerBody()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(sessions.getAnswers(900L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @WithMockUser(username = "jorge", roles = "CANDIDATE")
    void anotherCandidateIsForbidden() {
        assertThatThrownBy(() -> sessions.getSessionByApplication(50L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.startSession(900L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.recordAnswer(900L, answerBody())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.completeSession(900L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.getAnswers(900L)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(sessionCommands);
    }

    @Test
    @WithMockUser(username = "andina", roles = "RECRUITER")
    void recruiterCannotAnswerOnBehalfOfTheCandidate() {
        assertThatThrownBy(() -> sessions.startSession(900L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.recordAnswer(900L, answerBody())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> sessions.completeSession(900L)).isInstanceOf(AccessDeniedException.class);
    }
}
