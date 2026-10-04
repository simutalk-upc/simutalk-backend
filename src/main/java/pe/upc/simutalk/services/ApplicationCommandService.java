package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.Application;
import pe.upc.simutalk.dtos.ChangeApplicationStatusCommand;
import pe.upc.simutalk.dtos.SubmitApplicationCommand;

public interface ApplicationCommandService {

    Application handle(SubmitApplicationCommand command);

    Application handle(ChangeApplicationStatusCommand command);
}
