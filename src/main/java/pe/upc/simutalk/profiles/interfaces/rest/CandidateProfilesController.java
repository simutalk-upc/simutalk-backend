package pe.upc.simutalk.profiles.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCandidateProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileCommandService;
import pe.upc.simutalk.profiles.domain.services.CandidateProfileQueryService;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CandidateProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CreateCandidateProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.resources.UpdateCandidateProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.transform.CandidateProfileResourceFromEntityAssembler;
import pe.upc.simutalk.profiles.interfaces.rest.transform.CreateCandidateProfileCommandFromResourceAssembler;
import pe.upc.simutalk.profiles.interfaces.rest.transform.UpdateCandidateProfileCommandFromResourceAssembler;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;

@RestController
@RequestMapping(value = "/api/v1/candidate-profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidate Profiles", description = "Perfiles de postulante")
public class CandidateProfilesController {

    private final CandidateProfileCommandService candidateProfileCommandService;
    private final CandidateProfileQueryService candidateProfileQueryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CANDIDATE') and @profileAccess.isCurrentUser(#resource.userId, authentication))")
    @Operation(summary = "Crear perfil de postulante", description = "Un candidato crea el suyo; un admin, cualquiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (menor de 18, documento, experiencia)"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "422", description = "Usuario inexistente, con perfil o documento duplicado")
    })
    public ResponseEntity<CandidateProfileResource> createCandidateProfile(
            @Valid @RequestBody CreateCandidateProfileResource resource) {
        var candidate = candidateProfileCommandService.handle(
                CreateCandidateProfileCommandFromResourceAssembler.toCommandFromResource(resource));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(candidate.getId()).toUri();
        return ResponseEntity.created(location)
                .body(CandidateProfileResourceFromEntityAssembler.toResourceFromEntity(candidate));
    }

    @GetMapping("/{candidateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER') or @profileAccess.ownsCandidate(#candidateId, authentication)")
    @Operation(summary = "Obtener perfil de postulante", description = "Admin, recruiter o el propio candidato.")
    public ResponseEntity<CandidateProfileResource> getCandidateProfileById(@PathVariable Long candidateId) {
        var candidate = candidateProfileQueryService.handle(new GetCandidateProfileByIdQuery(candidateId))
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile", candidateId));
        return ResponseEntity.ok(CandidateProfileResourceFromEntityAssembler.toResourceFromEntity(candidate));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER') or @profileAccess.isCurrentUser(#userId, authentication)")
    @Operation(summary = "Obtener perfil de postulante por usuario")
    public ResponseEntity<CandidateProfileResource> getCandidateProfileByUserId(@RequestParam Long userId) {
        var candidate = candidateProfileQueryService.handle(new GetCandidateProfileByUserIdQuery(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile for user " + userId + " not found"));
        return ResponseEntity.ok(CandidateProfileResourceFromEntityAssembler.toResourceFromEntity(candidate));
    }

    @PutMapping("/{candidateId}")
    @PreAuthorize("hasRole('ADMIN') or @profileAccess.ownsCandidate(#candidateId, authentication)")
    @Operation(summary = "Actualizar perfil de postulante",
            description = "Solo el propio candidato o un admin. El documento no se puede cambiar.")
    public ResponseEntity<CandidateProfileResource> updateCandidateProfile(
            @PathVariable Long candidateId, @Valid @RequestBody UpdateCandidateProfileResource resource) {
        var candidate = candidateProfileCommandService.handle(
                UpdateCandidateProfileCommandFromResourceAssembler.toCommandFromResource(candidateId, resource));
        return ResponseEntity.ok(CandidateProfileResourceFromEntityAssembler.toResourceFromEntity(candidate));
    }
}
