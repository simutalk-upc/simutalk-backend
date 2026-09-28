package pe.upc.simutalk.assessment.domain.services;

import java.math.BigDecimal;

/**
 * Port to the AI/NLP provider that scores one answer against one criterion. Adapters live in
 * infrastructure/external; the domain never calls a provider directly.
 * <p>
 * Privacy (Ley 29733): the only inputs are the ALREADY ANONYMIZED transcript and the criterion.
 * The port has no parameter for candidate ids, names, documents, age or district on purpose.
 */
public interface AnswerScoringService {

    ScoringResult score(String anonymizedTranscript, String criterionName, String criterionDescription);

    /** Identifies the engine and model that produced the scores, e.g. {@code mock-1} or {@code gemini:<model>}. */
    String engineVersion();

    /**
     * @param score                0.0 to 10.0
     * @param confidence           0.00 to 1.00; 0 with a {@code null} excerpt means "no result"
     * @param excerpt              literal fragment of the anonymized transcript that supports the score
     * @param startOffset          start of {@code excerpt} in the anonymized transcript (inclusive)
     * @param endOffset            end of {@code excerpt} in the anonymized transcript (exclusive)
     * @param aiGeneratedSuspicion whether the answer looks AI-generated
     */
    record ScoringResult(BigDecimal score, BigDecimal confidence, String excerpt, int startOffset, int endOffset,
                         boolean aiGeneratedSuspicion) {

        /** Fallback when the provider could not be consulted or answered something unusable. */
        public static ScoringResult unavailable() {
            return new ScoringResult(BigDecimal.ZERO, BigDecimal.ZERO, null, 0, 0, false);
        }

        public boolean isAvailable() {
            return excerpt != null && !excerpt.isBlank() && startOffset < endOffset;
        }
    }
}
