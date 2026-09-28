package pe.upc.simutalk.analytics.domain.services;

import pe.upc.simutalk.analytics.domain.model.aggregates.CarbonSaving;
import pe.upc.simutalk.analytics.domain.model.commands.RecordCarbonSavingCommand;

import java.util.Optional;

public interface CarbonSavingCommandService {

    /** @return the saving, or empty if the application already had one (idempotent) */
    Optional<CarbonSaving> handle(RecordCarbonSavingCommand command);
}
