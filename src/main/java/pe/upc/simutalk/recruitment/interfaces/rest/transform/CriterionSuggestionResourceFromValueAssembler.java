package pe.upc.simutalk.recruitment.interfaces.rest.transform;

import pe.upc.simutalk.enums.CriterionOrigin;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionSuggestion;
import pe.upc.simutalk.enums.CriterionType;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.CriterionSuggestionResource;

public class CriterionSuggestionResourceFromValueAssembler {

    public static CriterionSuggestionResource toResourceFromValue(CriterionSuggestion suggestion) {
        return new CriterionSuggestionResource(suggestion.name(), suggestion.description(), CriterionType.COMPETENCY,
                CriterionOrigin.AI_SUGGESTED, suggestion.rationale());
    }
}
