package pe.upc.simutalk.interviews.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.interviews.domain.model.queries.CountQuestionsByCriterionIdQuery;
import pe.upc.simutalk.interviews.domain.services.QuestionQueryService;
import pe.upc.simutalk.shared.interfaces.acl.InterviewsContextFacade;

/**
 * interviews' implementation of the {@link InterviewsContextFacade} contract published in shared.
 */
@Service
@RequiredArgsConstructor
public class InterviewsContextFacadeImpl implements InterviewsContextFacade {

    private final QuestionQueryService questionQueryService;

    @Override
    public long countQuestionsByCriterionId(Long criterionId) {
        return questionQueryService.handle(new CountQuestionsByCriterionIdQuery(criterionId));
    }
}
