package pe.upc.simutalk.assessment.interfaces.rest.resources;

import pe.upc.simutalk.assessment.domain.model.valueobjects.FlagSeverity;
import pe.upc.simutalk.assessment.domain.model.valueobjects.IntegrityFlagType;

import java.time.Instant;

public record IntegrityFlagResource(Long id, IntegrityFlagType flagType, FlagSeverity severity, String detail,
                                    Instant raisedAt) {
}
