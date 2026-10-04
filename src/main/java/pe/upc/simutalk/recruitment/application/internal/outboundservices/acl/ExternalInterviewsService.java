package pe.upc.simutalk.recruitment.application.internal.outboundservices.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.services.InterviewQuestionCounter;
import pe.upc.simutalk.services.InterviewsContextFacade;

/**
 * Anti-corruption layer from recruitment to interviews, through the InterviewsContextFacade
 * contract in shared.
 */
@Service
@RequiredArgsConstructor
public class ExternalInterviewsService implements InterviewQuestionCounter {

    private final InterviewsContextFacade interviewsContextFacade;

    @Override
    public long countQuestionsByCriterionId(Long criterionId) {
        return interviewsContextFacade.countQuestionsByCriterionId(criterionId);
    }
}
