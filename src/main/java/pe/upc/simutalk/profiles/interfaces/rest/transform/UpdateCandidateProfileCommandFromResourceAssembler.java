package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.commands.UpdateCandidateProfileCommand;
import pe.upc.simutalk.profiles.interfaces.rest.resources.UpdateCandidateProfileResource;

public class UpdateCandidateProfileCommandFromResourceAssembler {

    public static UpdateCandidateProfileCommand toCommandFromResource(Long candidateId,
                                                                      UpdateCandidateProfileResource resource) {
        return new UpdateCandidateProfileCommand(candidateId, resource.firstName(), resource.lastName(),
                resource.birthDate(), resource.phone(), resource.district(), resource.yearsOfExperience());
    }
}
