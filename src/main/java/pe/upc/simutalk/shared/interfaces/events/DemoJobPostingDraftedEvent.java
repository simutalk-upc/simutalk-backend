package pe.upc.simutalk.shared.interfaces.events;

import java.util.List;

/**
 * Integration event published by recruitment's demo seeder once the demo job posting and its
 * criteria exist in DRAFT, so interviews can write its script before it is published.
 * Handled synchronously inside the publisher's transaction.
 */
public record DemoJobPostingDraftedEvent(Long jobPostingId, List<Long> competencyCriterionIds) {
}
