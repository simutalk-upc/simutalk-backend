package pe.upc.simutalk.assessment.application.internal.eventhandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.assessment.domain.model.commands.ComputeAssessmentCommand;
import pe.upc.simutalk.assessment.domain.services.AnswerScoringService;
import pe.upc.simutalk.assessment.domain.services.AssessmentCommandService;
import pe.upc.simutalk.shared.interfaces.events.DemoInterviewsCompletedEvent;

/**
 * Assessment part of the demo data: scores the completed demo interviews through the same
 * command service as the API, so the evidences are real fragments of the demo transcripts.
 * Only runs with the mock scoring engine, so demo seeding never calls an external provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentDemoDataSeeder {

    private final AssessmentCommandService assessmentCommandService;
    private final AnswerScoringService answerScoringService;

    @EventListener
    public void on(DemoInterviewsCompletedEvent event) {
        if (!answerScoringService.engineVersion().startsWith("mock")) {
            log.info("Assessment demo data skipped: scoring engine is {}, not mock", answerScoringService.engineVersion());
            return;
        }
        event.interviewSessionIds().forEach(sessionId ->
                assessmentCommandService.handle(new ComputeAssessmentCommand(sessionId)));
        log.info("Assessment demo data: {} interviews assessed", event.interviewSessionIds().size());
    }
}
