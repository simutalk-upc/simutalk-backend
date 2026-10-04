package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.Assessment;
import pe.upc.simutalk.dtos.ComputeAssessmentCommand;

public interface AssessmentCommandService {

    Assessment handle(ComputeAssessmentCommand command);
}
