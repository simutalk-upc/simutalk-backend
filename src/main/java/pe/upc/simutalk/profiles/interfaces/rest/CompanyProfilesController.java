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
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByIdQuery;
import pe.upc.simutalk.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileCommandService;
import pe.upc.simutalk.profiles.domain.services.CompanyProfileQueryService;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CompanyProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.resources.CreateCompanyProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.resources.UpdateCompanyProfileResource;
import pe.upc.simutalk.profiles.interfaces.rest.transform.CompanyProfileResourceFromEntityAssembler;
import pe.upc.simutalk.profiles.interfaces.rest.transform.CreateCompanyProfileCommandFromResourceAssembler;
import pe.upc.simutalk.profiles.interfaces.rest.transform.UpdateCompanyProfileCommandFromResourceAssembler;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;

@RestController
@RequestMapping(value = "/api/v1/company-profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Company Profiles", description = "Perfiles de empresa")
public class CompanyProfilesController {

    private final CompanyProfileCommandService companyProfileCommandService;
    private final CompanyProfileQueryService companyProfileQueryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RECRUITER') and @profileAccess.isCurrentUser(#resource.userId, authentication))")
    @Operation(summary = "Crear perfil de empresa", description = "Un recruiter crea el suyo; un admin, cualquiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (p. ej. RUC)"),
            @ApiResponse(responseCode = "403", description = "Sin permiso"),
            @ApiResponse(responseCode = "422", description = "Usuario inexistente, con perfil o RUC duplicado")
    })
    public ResponseEntity<CompanyProfileResource> createCompanyProfile(
            @Valid @RequestBody CreateCompanyProfileResource resource) {
        var company = companyProfileCommandService.handle(
                CreateCompanyProfileCommandFromResourceAssembler.toCommandFromResource(resource));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(company.getId()).toUri();
        return ResponseEntity.created(location).body(CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(company));
    }

    @GetMapping("/{companyProfileId}")
    @Operation(summary = "Obtener perfil de empresa")
    public ResponseEntity<CompanyProfileResource> getCompanyProfileById(@PathVariable Long companyProfileId) {
        var company = companyProfileQueryService.handle(new GetCompanyProfileByIdQuery(companyProfileId))
                .orElseThrow(() -> new ResourceNotFoundException("Company profile", companyProfileId));
        return ResponseEntity.ok(CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(company));
    }

    @GetMapping
    @Operation(summary = "Obtener perfil de empresa por usuario")
    public ResponseEntity<CompanyProfileResource> getCompanyProfileByUserId(@RequestParam Long userId) {
        var company = companyProfileQueryService.handle(new GetCompanyProfileByUserIdQuery(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Company profile for user " + userId + " not found"));
        return ResponseEntity.ok(CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(company));
    }

    @PutMapping("/{companyProfileId}")
    @PreAuthorize("hasRole('ADMIN') or @profileAccess.ownsCompany(#companyProfileId, authentication)")
    @Operation(summary = "Actualizar perfil de empresa", description = "El RUC no se puede cambiar.")
    public ResponseEntity<CompanyProfileResource> updateCompanyProfile(
            @PathVariable Long companyProfileId, @Valid @RequestBody UpdateCompanyProfileResource resource) {
        var company = companyProfileCommandService.handle(
                UpdateCompanyProfileCommandFromResourceAssembler.toCommandFromResource(companyProfileId, resource));
        return ResponseEntity.ok(CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(company));
    }
}
