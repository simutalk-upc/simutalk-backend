package pe.upc.simutalk.assessment.infrastructure.external.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.upc.simutalk.assessment.domain.services.AnswerScoringService.ScoringResult;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnswerScoringServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AnswerScoringServiceImpl service(String mode, String key) {
        return new AnswerScoringServiceImpl(mode, "https://generativelanguage.googleapis.com", "gemini-test", key,
                Duration.ofMillis(200), 3, Duration.ofMillis(5), 10, WebClient.builder(), objectMapper);
    }

    private static final String TRANSCRIPT = "Primero confirmaría que la caída es real. Luego separaría el volumen del ticket "
            + "promedio y descompondría los datos por región y canal para encontrar el problema de negocio.";

    @Test
    void mockModeIsDeterministicAndAnchoredInTheText() {
        var mock = service("mock", "");

        var first = mock.score(TRANSCRIPT, "Pensamiento analítico", "Descompone problemas de negocio y los resuelve con datos.");
        var second = mock.score(TRANSCRIPT, "Pensamiento analítico", "Descompone problemas de negocio y los resuelve con datos.");

        assertThat(first).isEqualTo(second);
        assertThat(first.isAvailable()).isTrue();
        assertThat(TRANSCRIPT.substring(first.startOffset(), first.endOffset())).isEqualTo(first.excerpt());
        assertThat(first.excerpt()).contains("descompondría los datos");
        assertThat(first.score()).isBetween(BigDecimal.ZERO, BigDecimal.TEN);
        assertThat(mock.engineVersion()).isEqualTo("mock-1");
    }

    @Test
    void liveModeRequiresAnApiKey() {
        assertThatThrownBy(() -> service("live", " ")).isInstanceOf(IllegalStateException.class);
        assertThat(service("live", "k").engineVersion()).isEqualTo("gemini:gemini-test");
    }

    @Test
    void retriesOnRateLimitWithBackoffThenSucceeds() {
        var attempts = new AtomicInteger();
        var call = Mono.defer(() -> attempts.incrementAndGet() < 3
                ? Mono.<ScoringResult>error(WebClientResponseException.create(429, "Too Many Requests", HttpHeaders.EMPTY, new byte[0], null))
                : Mono.just(new ScoringResult(BigDecimal.ONE, BigDecimal.ONE, "x", 0, 1, false)));

        assertThat(service("live", "k").callWithResilience(call).isAvailable()).isTrue();
        assertThat(attempts).hasValue(3);
    }

    @Test
    void fallsBackWhenTheProviderTimesOutOrFails() {
        assertThat(service("live", "k").callWithResilience(Mono.never()).isAvailable()).isFalse();
        assertThat(service("live", "k").callWithResilience(Mono.error(new IllegalStateException("boom"))).isAvailable()).isFalse();
    }

    @Test
    void discardsExcerptsThatAreNotLiteralFragments() throws Exception {
        var live = service("live", "k");
        var response = objectMapper.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":"
                + "\"{\\\"score\\\":9,\\\"confidence\\\":0.9,\\\"excerpt\\\":\\\"texto inventado por el modelo\\\"}\"}]}}]}");

        assertThat(live.parse(response, TRANSCRIPT).isAvailable()).isFalse();
    }

    @Test
    void clampsScoresAndLocatesTheExcerpt() throws Exception {
        var live = service("live", "k");
        var response = objectMapper.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":"
                + "\"{\\\"score\\\":12,\\\"confidence\\\":1.4,\\\"excerpt\\\":\\\"separaría el volumen del ticket promedio\\\",\\\"aiGeneratedSuspicion\\\":true}\"}]}}]}");

        var result = live.parse(response, TRANSCRIPT);

        assertThat(result.score()).isEqualByComparingTo("10.0");
        assertThat(result.confidence()).isEqualByComparingTo("1.00");
        assertThat(result.aiGeneratedSuspicion()).isTrue();
        assertThat(TRANSCRIPT.substring(result.startOffset(), result.endOffset())).isEqualTo(result.excerpt());
    }
}
