package pe.upc.simutalk.profiles.infrastructure.external.credentials;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.upc.simutalk.profiles.domain.services.CredentialVerificationService.VerificationResult;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialVerificationServiceImplTest {

    private static CredentialVerificationServiceImpl service(String mode, Duration timeout) {
        return new CredentialVerificationServiceImpl(mode, timeout, 3, Duration.ofMillis(10), WebClient.builder());
    }

    private final CredentialVerificationServiceImpl mock = service("mock", Duration.ofSeconds(5));

    @Test
    void mockMatchesCodesWithEightOrMoreCharacters() {
        var result = mock.verify("Coursera", "ABCD1234", "Google Data Analytics", "Rosa Quispe");

        assertThat(result.matched()).isTrue();
        assertThat(result.conclusive()).isTrue();
    }

    @Test
    void mockDoesNotMatchShorterCodes() {
        var result = mock.verify("Credly", "ABC1234", "AWS", "Rosa Quispe");

        assertThat(result.matched()).isFalse();
        assertThat(result.conclusive()).isTrue();
    }

    @Test
    void mockIgnoresSurroundingSpaces() {
        assertThat(mock.verify("Credly", "  ABC1234  ", "AWS", "Rosa").matched()).isFalse();
    }

    @Test
    void missingCodeNeverMatches() {
        assertThat(mock.verify("CertiProf", null, "Scrum", "Rosa").matched()).isFalse();
        assertThat(mock.verify("CertiProf", " ", "Scrum", "Rosa").matched()).isFalse();
    }

    @Test
    void liveModeWithoutIssuerClientFallsBackToInconclusive() {
        var result = service("live", Duration.ofSeconds(5)).verify("Coursera", "ABCD1234", "Java", "Rosa");

        assertThat(result.matched()).isFalse();
        assertThat(result.conclusive()).isFalse();
    }

    @Test
    void retriesOnTooManyRequestsAndThenSucceeds() {
        var attempts = new AtomicInteger();
        var call = Mono.defer(() -> attempts.incrementAndGet() < 3
                ? Mono.<VerificationResult>error(tooManyRequests())
                : Mono.just(new VerificationResult(true, "ok")));

        var result = mock.callWithResilience(call);

        assertThat(result.matched()).isTrue();
        assertThat(attempts).hasValue(3);
    }

    @Test
    void fallsBackWhenRetriesAreExhausted() {
        var attempts = new AtomicInteger();
        var call = Mono.defer(() -> {
            attempts.incrementAndGet();
            return Mono.<VerificationResult>error(tooManyRequests());
        });

        var result = mock.callWithResilience(call);

        assertThat(result.conclusive()).isFalse();
        assertThat(attempts).hasValue(4);
    }

    @Test
    void doesNotRetryOtherErrorsAndFallsBack() {
        var attempts = new AtomicInteger();
        var call = Mono.defer(() -> {
            attempts.incrementAndGet();
            return Mono.<VerificationResult>error(new IllegalStateException("boom"));
        });

        assertThat(mock.callWithResilience(call).conclusive()).isFalse();
        assertThat(attempts).hasValue(1);
    }

    @Test
    void fallsBackOnTimeout() {
        var slow = service("live", Duration.ofMillis(50));

        var result = slow.callWithResilience(Mono.never());

        assertThat(result.conclusive()).isFalse();
    }

    @Test
    void rejectsUnknownMode() {
        assertThatThrownBy(() -> service("sandbox", Duration.ofSeconds(1))).isInstanceOf(IllegalStateException.class);
    }

    private static WebClientResponseException tooManyRequests() {
        return WebClientResponseException.create(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests",
                HttpHeaders.EMPTY, new byte[0], null);
    }
}
