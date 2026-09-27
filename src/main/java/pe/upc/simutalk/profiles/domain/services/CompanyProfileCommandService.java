package pe.upc.simutalk.profiles.domain.services;

import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.commands.CreateCompanyProfileCommand;
import pe.upc.simutalk.profiles.domain.model.commands.UpdateCompanyProfileCommand;

public interface CompanyProfileCommandService {

    CompanyProfile handle(CreateCompanyProfileCommand command);

    CompanyProfile handle(UpdateCompanyProfileCommand command);
}
