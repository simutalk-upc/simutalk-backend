package pe.upc.simutalk.assessment.infrastructure.external.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.upc.simutalk.assessment.domain.services.AnswerScoringService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Adapter of the {@link AnswerScoringService} port.
 * <ul>
 *   <li>{@code external.ai.mode=mock} (default): deterministic, no network ({@link MockAnswerScorer}).</li>
 *   <li>{@code external.ai.mode=live}: Google Gemini {@code generateContent} REST API with the model and
 *       key from {@code external.ai.gemini.*}, called through {@link RestClient}. Timeouts, exponential backoff on HTTP 429/503, an LRU cache
 *       keyed by the SHA-256 of the request, and {@link ScoringResult#unavailable()} as fallback.</li>
 * </ul>
 * The excerpt returned by the model is located again in the anonymized transcript; if it is not a
 * literal fragment, the result is discarded (CLAUDE.md: a score without real evidence is not shown).
 */
@Slf4j
@Service
public class AnswerScoringServiceImpl implements AnswerScoringService {

    static final String MOCK_ENGINE_VERSION = "mock-1";

    enum Mode { MOCK, LIVE }

    private final Mode mode;
    private final String model;
    private final String apiKey;
    private final int maxRetries;
    private final Duration initialBackoff;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, ScoringResult> cache;

    public AnswerScoringServiceImpl(@Value("${external.ai.mode:mock}") String mode,
                                    @Value("${external.ai.gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
                                    @Value("${external.ai.gemini.model:gemini-2.5-flash}") String model,
                                    @Value("${external.ai.gemini.api-key:}") String apiKey,
                                    @Value("${external.ai.timeout:20s}") Duration timeout,
                                    @Value("${external.ai.max-retries:3}") int maxRetries,
                                    @Value("${external.ai.initial-backoff:1s}") Duration initialBackoff,
                                    @Value("${external.ai.cache-size:500}") int cacheSize,
                                    RestClient.Builder restClientBuilder,
                                    ObjectMapper objectMapper) {
        this.mode = parseMode(mode);
        this.model = model;
        this.apiKey = apiKey;
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = restClientBuilder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ScoringResult> eldest) {
                return size() > cacheSize;
            }
        });
        if (this.mode == Mode.LIVE && (apiKey == null || apiKey.isBlank())) {
            throw new IllegalStateException("external.ai.mode=live requires GEMINI_API_KEY");
        }
        log.info("Answer scoring running in {} mode{}", this.mode, this.mode == Mode.LIVE ? " (" + model + ")" : "");
    }

    @Override
    public String engineVersion() {
        return mode == Mode.MOCK ? MOCK_ENGINE_VERSION : "gemini:" + model;
    }

    @Override
    public ScoringResult score(String anonymizedTranscript, String criterionName, String criterionDescription) {
        if (anonymizedTranscript == null || anonymizedTranscript.isBlank()) {
            return ScoringResult.unavailable();
        }
        if (mode == Mode.MOCK) {
            return MockAnswerScorer.score(anonymizedTranscript, criterionName, criterionDescription);
        }
        var key = cacheKey(anonymizedTranscript, criterionName, criterionDescription);
        var cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        var result = callWithResilience(() -> parse(
                requestGemini(anonymizedTranscript, criterionName, criterionDescription), anonymizedTranscript));
        if (result.isAvailable()) {
            cache.put(key, result);
        }
        return result;
    }

    private JsonNode requestGemini(String anonymizedTranscript, String criterionName, String criterionDescription) {
        return restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(GeminiRequestFactory.build(anonymizedTranscript, criterionName, criterionDescription))
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Exponential backoff on 429/503 and fallback; the timeouts live in {@link #restClient}.
     * Package visible for tests without network.
     */
    ScoringResult callWithResilience(Supplier<ScoringResult> call) {
        var backoff = initialBackoff;
        for (var attempt = 0; ; attempt++) {
            try {
                var result = call.get();
                return result != null ? result : ScoringResult.unavailable();
            } catch (RuntimeException ex) {
                if (!isRetryable(ex) || attempt >= maxRetries) {
                    log.warn("Answer scoring unavailable: {}", ex.toString());
                    return ScoringResult.unavailable();
                }
            }
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return ScoringResult.unavailable();
            }
            backoff = backoff.multipliedBy(2);
        }
    }

    /** Reads Gemini's JSON answer; anything malformed or not literally anchored becomes unavailable. */
    ScoringResult parse(JsonNode response, String anonymizedTranscript) {
        try {
            var text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            if (text.isBlank()) {
                return ScoringResult.unavailable();
            }
            var json = objectMapper.readTree(text);
            var excerpt = json.path("excerpt").asText("").strip();
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

    private String cacheKey(String transcript, String criterionName, String criterionDescription) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest((model + "\u0000" + criterionName + "\u0000" + criterionDescription + "\u0000" + transcript)
                            .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private static boolean isRetryable(RuntimeException ex) {
        return ex instanceof RestClientResponseException response
                && (response.getStatusCode().value() == 429 || response.getStatusCode().value() == 503);
    }

    private static Mode parseMode(String mode) {
        try {
            return Mode.valueOf(mode.strip().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("external.ai.mode must be 'mock' or 'live', got: " + mode);
        }
    }
}
