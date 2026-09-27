package pe.upc.simutalk.recruitment.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.upc.simutalk.recruitment.domain.model.commands.SubmitApplicationCommand;
import pe.upc.simutalk.recruitment.domain.model.queries.GetApplicationsByJobPostingIdQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.ApplicationStatus;
import pe.upc.simutalk.recruitment.domain.services.ApplicationCommandService;
import pe.upc.simutalk.recruitment.domain.services.ApplicationQueryService;
import pe.upc.simutalk.recruitment.interfaces.rest.authorization.RecruitmentAccessPolicy;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.ApplicationResource;
import pe.upc.simutalk.recruitment.interfaces.rest.transform.ApplicationResourceFromEntityAssembler;
import pe.upc.simutalk.shared.domain.exceptions.BusinessRuleViolationException;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/job-postings/{jobPostingId}/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Job Posting Applications", description = "Postulaciones a una vacante (pipeline del reclutador)")
public class JobPostingApplicationsController {

    private final ApplicationCommandService applicationCommandService;
    private final ApplicationQueryService applicationQueryService;
    private final RecruitmentAccessPolicy recruitmentAccessPolicy;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Postular a una vacante",
            description = "El candidato se toma del usuario autenticado. Solo vacantes PUBLISHED y una vez por vacante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulación registrada en RECEIVED"),
            @ApiResponse(responseCode = "403", description = "No es candidato"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada"),
            @ApiResponse(responseCode = "422", description = "Vacante no publicada, ya postuló o sin perfil de candidato")
    })
    public ResponseEntity<ApplicationResource> apply(@PathVariable Long jobPostingId, Authentication authentication) {
        var candidateId = recruitmentAccessPolicy.currentCandidateId(authentication)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "The authenticated user has no candidate profile to apply with"));
        var application = applicationCommandService.handle(new SubmitApplicationCommand(jobPostingId, candidateId));
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/applications/{id}").buildAndExpand(application.getId()).toUri();
        return ResponseEntity.created(location).body(ApplicationResourceFromEntityAssembler.toResourceFromEntity(application));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RECRUITER') and @recruitmentAccess.ownsJobPosting(#jobPostingId, authentication))")
    @Operation(summary = "Listar postulantes de la vacante",
            description = "Solo el recruiter dueño (o un admin). Alimenta el pipeline del dashboard.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulaciones ordenadas por fecha de postulación"),
            @ApiResponse(responseCode = "403", description = "No es el recruiter dueño ni admin")
    })
    public ResponseEntity<List<ApplicationResource>> getApplications(
            @PathVariable Long jobPostingId,
            @Parameter(description = "Filtra por etapa del pipeline") @RequestParam(required = false) ApplicationStatus status) {
        var applications = applicationQueryService.handle(new GetApplicationsByJobPostingIdQuery(jobPostingId, status));
        return ResponseEntity.ok(applications.stream()
                .map(ApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
