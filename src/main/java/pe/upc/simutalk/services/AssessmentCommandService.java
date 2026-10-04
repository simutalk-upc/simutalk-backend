package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.Assessment;
import pe.upc.simutalk.assessment.domain.model.commands.ComputeAssessmentCommand;

public interface AssessmentCommandService {

    Assessment handle(ComputeAssessmentCommand command);
}
