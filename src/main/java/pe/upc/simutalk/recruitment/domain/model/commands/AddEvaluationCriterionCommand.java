package pe.upc.simutalk.recruitment.domain.model.commands;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;

public record AddEvaluationCriterionCommand(
        Long jobPostingId,
        String name,
        String description,
        Integer weight,
        CriterionType criterionType,
        String certificationName,
        boolean mandatory) {
}
