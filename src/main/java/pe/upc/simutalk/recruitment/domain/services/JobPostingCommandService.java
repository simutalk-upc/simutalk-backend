package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.entities.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.commands.*;
import pe.upc.simutalk.entities.EvaluationCriterion;

public interface JobPostingCommandService {

    JobPosting handle(CreateJobPostingCommand command);

    JobPosting handle(UpdateJobPostingCommand command);

    void handle(DeleteJobPostingCommand command);

    JobPosting handle(ChangeJobPostingStatusCommand command);

    EvaluationCriterion handle(AddEvaluationCriterionCommand command);

    EvaluationCriterion handle(UpdateEvaluationCriterionCommand command);

    void handle(RemoveEvaluationCriterionCommand command);
}
