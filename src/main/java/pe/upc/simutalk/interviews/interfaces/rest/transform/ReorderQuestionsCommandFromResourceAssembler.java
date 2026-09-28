package pe.upc.simutalk.interviews.interfaces.rest.transform;

import pe.upc.simutalk.interviews.domain.model.commands.ReorderQuestionsCommand;
import pe.upc.simutalk.interviews.interfaces.rest.resources.ReorderQuestionsResource;

public class ReorderQuestionsCommandFromResourceAssembler {

    public static ReorderQuestionsCommand toCommandFromResource(Long jobPostingId, ReorderQuestionsResource resource) {
        return new ReorderQuestionsCommand(jobPostingId, resource.orderedIds());
    }
}
