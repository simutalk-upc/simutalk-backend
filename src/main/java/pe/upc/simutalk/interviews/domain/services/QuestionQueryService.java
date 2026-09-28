package pe.upc.simutalk.interviews.domain.services;

import pe.upc.simutalk.interviews.domain.model.aggregates.Question;
import pe.upc.simutalk.interviews.domain.model.queries.CountQuestionsByCriterionIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionByIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionsByJobPostingIdQuery;

import java.util.List;
import java.util.Optional;

public interface QuestionQueryService {

    /** The script ordered by position. */
    List<Question> handle(GetQuestionsByJobPostingIdQuery query);

    Optional<Question> handle(GetQuestionByIdQuery query);

    long handle(CountQuestionsByCriterionIdQuery query);
}
