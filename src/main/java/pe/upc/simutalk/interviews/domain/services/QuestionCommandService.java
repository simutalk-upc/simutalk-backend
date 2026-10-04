package pe.upc.simutalk.interviews.domain.services;

import pe.upc.simutalk.entities.Question;
import pe.upc.simutalk.interviews.domain.model.commands.CreateQuestionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.DeleteQuestionCommand;
import pe.upc.simutalk.interviews.domain.model.commands.ReorderQuestionsCommand;
import pe.upc.simutalk.interviews.domain.model.commands.UpdateQuestionCommand;

import java.util.List;

public interface QuestionCommandService {

    Question handle(CreateQuestionCommand command);

    Question handle(UpdateQuestionCommand command);

    void handle(DeleteQuestionCommand command);

    /** @return the script in its new order */
    List<Question> handle(ReorderQuestionsCommand command);
}
