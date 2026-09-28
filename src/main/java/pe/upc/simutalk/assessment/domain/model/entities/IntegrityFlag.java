package pe.upc.simutalk.assessment.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.upc.simutalk.assessment.domain.model.valueobjects.FlagSeverity;
import pe.upc.simutalk.assessment.domain.model.valueobjects.IntegrityFlagType;
import pe.upc.simutalk.shared.domain.model.entities.AuditableModel;

import java.time.Instant;

/**
 * Something a human should review before trusting the assessment. Flags never change scores.
 */
@Getter
@Entity
@Table(name = "integrity_flags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntegrityFlag extends AuditableModel {

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, length = 30)
    private IntegrityFlagType flagType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FlagSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;

    public IntegrityFlag(IntegrityFlagType flagType, FlagSeverity severity, String detail, Instant raisedAt) {
        if (flagType == null || severity == null || raisedAt == null) {
            throw new IllegalArgumentException("Flag type, severity and instant are required");
        }
        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException("Integrity flag detail is required");
        }
        this.flagType = flagType;
        this.severity = severity;
        this.detail = detail.strip();
        this.raisedAt = raisedAt;
    }
}
