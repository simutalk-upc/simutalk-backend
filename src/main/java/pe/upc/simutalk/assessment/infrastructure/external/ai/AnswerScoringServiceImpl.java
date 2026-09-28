package pe.upc.simutalk.assessment.infrastructure.external.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.upc.simutalk.assessment.domain.services.AnswerScoringService;
import pe.upc.simutalk.shared.infrastructure.external.ai.GenerativeAiClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * Adapter of the {@link AnswerScoringService} port. It builds assessment's own prompt, validates the
 * answer against assessment's own schema and uses the shared {@link GenerativeAiClient} only as
 * transport (mode, timeout, retries and cache live there).
 * <ul>
 *   <li>mock mode: deterministic, no network ({@link MockAnswerScorer}).</li>
 *   <li>live mode: Google Gemini; the excerpt returned by the model is located again in the anonymized
 *       transcript and, if it is not a literal fragment, the result is discarded (CLAUDE.md: a score
 *       without real evidence is not shown).</li>
 * </ul>
 * Privacy (Ley 29733): callers pass the ALREADY ANONYMIZED transcript; nothing else about the
 * candidate reaches the prompt.
 */
@Slf4j
@Service
public class AnswerScoringServiceImpl implements AnswerScoringService {

    static final String MOCK_ENGINE_VERSION = "mock-1";

    private final GenerativeAiClient aiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AnswerScoringServiceImpl(GenerativeAiClient aiClient, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    /** Builds its own client from explicit settings; used by tests that talk to a local HTTP server. */
    public AnswerScoringServiceImpl(String mode, String baseUrl, String model, String apiKey, Duration timeout,
                                    int maxRetries, Duration initialBackoff, int cacheSize,
                                    RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this(new GenerativeAiClient(mode, baseUrl, model, apiKey, timeout, maxRetries, initialBackoff, cacheSize,
                restClientBuilder), objectMapper);
    }

    @Override
    public String engineVersion() {
        return aiClient.isLive() ? "gemini:" + aiClient.model() : MOCK_ENGINE_VERSION;
    }

    @Override
    public ScoringResult score(String anonymizedTranscript, String criterionName, String criterionDescription) {
        if (anonymizedTranscript == null || anonymizedTranscript.isBlank()) {
            return ScoringResult.unavailable();
        }
        if (!aiClient.isLive()) {
            return MockAnswerScorer.score(anonymizedTranscript, criterionName, criterionDescription);
        }
        return aiClient.generate(GeminiRequestFactory.prompt(anonymizedTranscript, criterionName, criterionDescription))
                .map(text -> parse(text, anonymizedTranscript))
                .orElseGet(ScoringResult::unavailable);
    }

    /** Reads the model's JSON answer; anything malformed or not literally anchored becomes unavailable. */
    ScoringResult parse(String text, String anonymizedTranscript) {
        try {
            if (text == null || text.isBlank()) {
                return ScoringResult.unavailable();
            }
            var json = objectMapper.readTree(text);
            var excerpt = json.path("excerpt").asString("").strip();
            var start = excerpt.isEmpty() ? -1 : anonymizedTranscript.indexOf(excerpt);
            if (start < 0) {
                log.info("Discarding AI score: the excerpt is not a literal fragment of the answer");
                return ScoringResult.unavailable();
            }
            var score = clamp(json.path("score").asDouble(-1), 0, 10);
            var confidence = clamp(json.path("confidence").asDouble(-1), 0, 1);
            if (score < 0 || confidence < 0) {
                return ScoringResult.unavailable();
            }
            return new ScoringResult(BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP),
                    excerpt, start, start + excerpt.length(), json.path("aiGeneratedSuspicion").asBoolean(false));
        } catch (Exception ex) {
            log.warn("Could not read the AI answer: {}", ex.toString());
            return ScoringResult.unavailable();
        }
    }

    private static double clamp(double value, double min, double max) {
        return value < min ? (value == -1 ? -1 : min) : Math.min(value, max);
    }
}
