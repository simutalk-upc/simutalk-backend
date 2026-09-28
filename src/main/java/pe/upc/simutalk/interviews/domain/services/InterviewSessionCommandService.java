package pe.upc.simutalk.interviews.domain.services;

import pe.upc.simutalk.interviews.domain.model.aggregates.InterviewSession;
import pe.upc.simutalk.interviews.domain.model.commands.CompleteInterviewSessionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.CreateInterviewSessionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.RecordAnswerCommand;
import pe.upc.simutalk.interviews.domain.model.commands.StartInterviewSessionCommand;
import pe.upc.simutalk.interviews.domain.model.entities.Answer;

public interface InterviewSessionCommandService {

    InterviewSession handle(CreateInterviewSessionCommand command);

    InterviewSession handle(StartInterviewSessionCommand command);

    Answer handle(RecordAnswerCommand command);

    InterviewSession handle(CompleteInterviewSessionCommand command);
}
