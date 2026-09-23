package pe.upc.simutalk.recruitment.domain.model.commands;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;

public record UpdateEvaluationCriterionCommand(
        Long jobPostingId,
        Long criterionId,
        String name,
        String description,
        Integer weight,
        CriterionType criterionType,
        String certificationName,
        boolean mandatory) {
}
