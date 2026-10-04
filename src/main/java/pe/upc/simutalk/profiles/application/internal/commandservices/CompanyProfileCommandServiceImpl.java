package pe.upc.simutalk.profiles.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.profiles.application.internal.outboundservices.acl.ExternalIamService;
import pe.upc.simutalk.profiles.domain.model.aggregates.CompanyProfile;
import pe.upc.simutalk.profiles.domain.model.commands.CreateCompanyProfileCommand;
import pe.upc.simutalk.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import pe.upc.simutalk.profiles.domain.model.valueobjects.Ruc;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileCommandService;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CandidateProfileRepository;
import pe.upc.simutalk.profiles.infrastructure.persistence.jpa.repositories.CompanyProfileRepository;
import pe.upc.simutalk.exceptions.BusinessRuleViolationException;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyProfileCommandServiceImpl implements CompanyProfileCommandService {

    private final CompanyProfileRepository companyProfileRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ExternalIamService externalIamService;

    @Override
    public CompanyProfile handle(CreateCompanyProfileCommand command) {
        // Validates the value object first so a malformed RUC is reported as 400 before lookups.
        var ruc = new Ruc(command.ruc());
        if (!externalIamService.existsUser(command.userId())) {
            throw new BusinessRuleViolationException("User %s does not exist".formatted(command.userId()));
        }
        if (companyProfileRepository.existsByUserId(command.userId())
                || candidateProfileRepository.existsByUserId(command.userId())) {
            throw new BusinessRuleViolationException("User %s already has a profile".formatted(command.userId()));
        }
        if (companyProfileRepository.existsByRuc(ruc)) {
            throw new BusinessRuleViolationException("A company with RUC %s already exists".formatted(ruc.value()));
        }
        return companyProfileRepository.save(new CompanyProfile(command));
    }

    @Override
    public CompanyProfile handle(UpdateCompanyProfileCommand command) {
        var company = companyProfileRepository.findById(command.companyProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Company profile", command.companyProfileId()));
        company.updateDetails(command.legalName(), command.tradeName(), command.industry(), command.companySize(),
                command.district(), command.email());
        companyProfileRepository.flush();
        return company;
    }
}
