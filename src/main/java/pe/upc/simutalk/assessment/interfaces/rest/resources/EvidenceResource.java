package pe.upc.simutalk.assessment.interfaces.rest.resources;

/**
 * @param startOffset position of the excerpt in the original transcript (inclusive)
 * @param endOffset   position of the excerpt in the original transcript (exclusive)
 */
public record EvidenceResource(Long id, Long answerId, String excerpt, int startOffset, int endOffset) {
}
