package pe.upc.simutalk.profiles.interfaces.rest.transform;

import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CompanyProfileResource;

public class CompanyProfileResourceFromEntityAssembler {

    public static CompanyProfileResource toResourceFromEntity(CompanyProfile entity) {
        return new CompanyProfileResource(entity.getId(), entity.getUserId(), entity.getLegalName(),
                entity.getTradeName(), entity.getIndustry(), entity.getRuc().value(), entity.getCompanySize(),
                entity.getDistrict(), entity.getEmail() == null ? null : entity.getEmail().value(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
