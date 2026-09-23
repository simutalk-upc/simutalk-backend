package pe.upc.simutalk.recruitment.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.upc.simutalk.recruitment.domain.model.commands.DeleteJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.SearchJobPostingsQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;
import pe.upc.simutalk.recruitment.domain.services.JobPostingCommandService;
import pe.upc.simutalk.recruitment.domain.services.JobPostingQueryService;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.CreateJobPostingResource;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.JobPostingResource;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateJobPostingResource;
import pe.upc.simutalk.recruitment.interfaces.rest.resources.UpdateJobPostingStatusResource;
import pe.upc.simutalk.recruitment.interfaces.rest.transform.ChangeJobPostingStatusCommandFromResourceAssembler;
import pe.upc.simutalk.recruitment.interfaces.rest.transform.CreateJobPostingCommandFromResourceAssembler;
import pe.upc.simutalk.recruitment.interfaces.rest.transform.JobPostingResourceFromEntityAssembler;
import pe.upc.simutalk.recruitment.interfaces.rest.transform.UpdateJobPostingCommandFromResourceAssembler;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/job-postings", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Job Postings", description = "Vacantes y su ciclo de vida")
public class JobPostingsController {

    private final JobPostingCommandService jobPostingCommandService;
    private final JobPostingQueryService jobPostingQueryService;

    @PostMapping
    @Operation(summary = "Crear una vacante", description = "La vacante se crea en estado DRAFT y sin criterios.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vacante creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<JobPostingResource> createJobPosting(@Valid @RequestBody CreateJobPostingResource resource) {
        var command = CreateJobPostingCommandFromResourceAssembler.toCommandFromResource(resource);
        var jobPosting = jobPostingCommandService.handle(command);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(jobPosting.getId()).toUri();
        return ResponseEntity.created(location).body(JobPostingResourceFromEntityAssembler.toResourceFromEntity(jobPosting));
    }

    @GetMapping("/{jobPostingId}")
    @Operation(summary = "Obtener una vacante con sus criterios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacante encontrada"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada")
    })
    public ResponseEntity<JobPostingResource> getJobPostingById(@PathVariable Long jobPostingId) {
        var jobPosting = jobPostingQueryService.handle(new GetJobPostingByIdQuery(jobPostingId))
                .orElseThrow(() -> new ResourceNotFoundException("Job posting", jobPostingId));
        return ResponseEntity.ok(JobPostingResourceFromEntityAssembler.toResourceFromEntity(jobPosting));
    }

    @GetMapping
    @Operation(summary = "Listar vacantes", description = "Ambos filtros son opcionales y combinables.")
    public ResponseEntity<List<JobPostingResource>> getJobPostings(
            @Parameter(description = "Filtra por empresa") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filtra por estado") @RequestParam(required = false) JobPostingStatus status) {
        var jobPostings = jobPostingQueryService.handle(new SearchJobPostingsQuery(companyId, status));
        return ResponseEntity.ok(jobPostings.stream()
                .map(JobPostingResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PutMapping("/{jobPostingId}")
    @Operation(summary = "Actualizar los datos de una vacante",
            description = "No se permite sobre vacantes CLOSED. anonymizedScreening solo cambia en DRAFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacante actualizada"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio incumplida")
    })
    public ResponseEntity<JobPostingResource> updateJobPosting(@PathVariable Long jobPostingId,
                                                               @Valid @RequestBody UpdateJobPostingResource resource) {
        var command = UpdateJobPostingCommandFromResourceAssembler.toCommandFromResource(jobPostingId, resource);
        var jobPosting = jobPostingCommandService.handle(command);
        return ResponseEntity.ok(JobPostingResourceFromEntityAssembler.toResourceFromEntity(jobPosting));
    }

    @DeleteMapping("/{jobPostingId}")
    @Operation(summary = "Eliminar una vacante", description = "Una vacante PUBLISHED debe cerrarse antes de eliminarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vacante eliminada"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio incumplida")
    })
    public ResponseEntity<Void> deleteJobPosting(@PathVariable Long jobPostingId) {
        jobPostingCommandService.handle(new DeleteJobPostingCommand(jobPostingId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{jobPostingId}/status")
    @Operation(summary = "Cambiar el estado de una vacante",
            description = "DRAFT → PUBLISHED exige al menos un criterio y pesos que sumen exactamente 100. "
                    + "DRAFT/PUBLISHED → CLOSED. No se vuelve a DRAFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Vacante no encontrada"),
            @ApiResponse(responseCode = "422", description = "Transición no permitida")
    })
    public ResponseEntity<JobPostingResource> changeJobPostingStatus(@PathVariable Long jobPostingId,
                                                                     @Valid @RequestBody UpdateJobPostingStatusResource resource) {
        var command = ChangeJobPostingStatusCommandFromResourceAssembler.toCommandFromResource(jobPostingId, resource);
        var jobPosting = jobPostingCommandService.handle(command);
        return ResponseEntity.ok(JobPostingResourceFromEntityAssembler.toResourceFromEntity(jobPosting));
    }
}
