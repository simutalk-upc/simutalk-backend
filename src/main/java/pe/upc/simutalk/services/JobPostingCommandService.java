package pe.upc.simutalk.services;

import pe.upc.simutalk.recruitment.domain.model.commands.AddEvaluationCriterionCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeJobPostingStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.DeleteJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.RemoveEvaluationCriterionCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.UpdateEvaluationCriterionCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.UpdateJobPostingCommand;

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
