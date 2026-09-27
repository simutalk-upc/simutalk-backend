package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.commands.CreateCandidateProfileCommand;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CreateCandidateProfileResource;

public class CreateCandidateProfileCommandFromResourceAssembler {

    public static CreateCandidateProfileCommand toCommandFromResource(CreateCandidateProfileResource resource) {
        return new CreateCandidateProfileCommand(resource.userId(), resource.firstName(), resource.lastName(),
                resource.documentNumber(), resource.birthDate(), resource.phone(), resource.district(),
                resource.yearsOfExperience());
    }
}
