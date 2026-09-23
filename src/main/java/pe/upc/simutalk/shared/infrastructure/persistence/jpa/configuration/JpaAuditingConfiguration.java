package pe.upc.simutalk.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables {@code @CreatedDate} and {@code @LastModifiedDate} population.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
