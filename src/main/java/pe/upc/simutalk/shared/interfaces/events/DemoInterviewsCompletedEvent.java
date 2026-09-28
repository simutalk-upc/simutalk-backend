package pe.upc.simutalk.shared.interfaces.events;

import java.util.List;

/**
 * Integration event published by interviews' demo seeder once the demo interviews of the
 * ASSESSED candidates are COMPLETED, so assessment can score them. Handled synchronously
 * inside the demo seeding transaction.
 */
public record DemoInterviewsCompletedEvent(Long jobPostingId, List<Long> interviewSessionIds) {
}
