package pe.upc.simutalk.services;

import pe.upc.simutalk.entities.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.commands.CreateCompanyProfileCommand;
import pe.upc.simutalk.profiles.domain.model.commands.UpdateCompanyProfileCommand;

public interface CompanyProfileCommandService {

    CompanyProfile handle(CreateCompanyProfileCommand command);

    CompanyProfile handle(UpdateCompanyProfileCommand command);
}
