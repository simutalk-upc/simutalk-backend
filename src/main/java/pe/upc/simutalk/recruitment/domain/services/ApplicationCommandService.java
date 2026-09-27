package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.recruitment.domain.model.aggregates.Application;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.SubmitApplicationCommand;

public interface ApplicationCommandService {

    Application handle(SubmitApplicationCommand command);

    Application handle(ChangeApplicationStatusCommand command);
}
