package pe.upc.simutalk.interviews.domain.services;

import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionSuggestionsQuery;
import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionSuggestion;

import java.util.List;

public interface QuestionSuggestionQueryService {

    /** Proposed questions for a COMPETENCY criterion of a DRAFT job posting; nothing is persisted. */
    List<QuestionSuggestion> handle(GetQuestionSuggestionsQuery query);
}
