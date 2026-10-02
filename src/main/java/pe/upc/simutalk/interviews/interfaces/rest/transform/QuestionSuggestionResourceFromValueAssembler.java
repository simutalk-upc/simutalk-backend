package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionOrigin;
import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionSuggestion;
import pe.upc.simutalk.interviews.interfaces.rest.resources.QuestionSuggestionResource;

public class QuestionSuggestionResourceFromValueAssembler {

    public static QuestionSuggestionResource toResourceFromValue(Long criterionId, QuestionSuggestion suggestion) {
        return new QuestionSuggestionResource(criterionId, suggestion.statement(), QuestionOrigin.AI_SUGGESTED,
                suggestion.rationale());
    }
}
