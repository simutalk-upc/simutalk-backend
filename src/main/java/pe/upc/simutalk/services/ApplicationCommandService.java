package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.Application;
import pe.upc.simutalk.recruitment.domain.model.commands.ChangeApplicationStatusCommand;
import pe.upc.simutalk.recruitment.domain.model.commands.SubmitApplicationCommand;

public interface ApplicationCommandService {

    Application handle(SubmitApplicationCommand command);

    Application handle(ChangeApplicationStatusCommand command);
}
