package pe.upc.simutalk.profiles.infrastructure.external.credentials;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.upc.simutalk.profiles.domain.services.CredentialVerificationService;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Adapter for {@link CredentialVerificationService}.
 * <p>
 * Mode {@code external.credentials.mode}:
 * <ul>
 *   <li>{@code mock} (default): no network. A code of 8 or more characters matches,
 *       a shorter one does not.</li>
 *   <li>{@code live}: calls the issuer through {@link WebClient}. No issuer is wired yet
 *       (see TODOs), so every live request answers "unavailable" and the certification
 *       stays UNVERIFIED.</li>
 * </ul>
 * Every live call has a timeout, retries with exponential backoff on HTTP 429 and falls
 * back to an inconclusive result instead of throwing.
 */
@Slf4j
@Service
public class CredentialVerificationServiceImpl implements CredentialVerificationService {

    static final int MOCK_MIN_CODE_LENGTH = 8;

    enum Mode { MOCK, LIVE }

    private final Mode mode;
    private final Duration timeout;
    private final int maxRetries;
    private final Duration initialBackoff;
    /** HTTP client for the issuer calls registered in {@link #issuerClients}. */
    private final WebClient webClient;

    /**
     * Issuer name (lower case) -> call that verifies a credential with that issuer.
     * TODO(coursera): wire the official verification API once access is granted; do not guess endpoints.
     * TODO(credly): wire the official Credly badge verification API once access is granted.
     * TODO(certiprof): wire CertiProf's verification service once its API is documented.
     */
    private final Map<String, Function<CredentialRequest, Mono<VerificationResult>>> issuerClients = Map.of();

    public CredentialVerificationServiceImpl(
            @Value("${external.credentials.mode:mock}") String mode,
            @Value("${external.credentials.timeout:5s}") Duration timeout,
            @Value("${external.credentials.max-retries:3}") int maxRetries,
            @Value("${external.credentials.initial-backoff:500ms}") Duration initialBackoff,
            WebClient.Builder webClientBuilder) {
        this.mode = parseMode(mode);
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff;
        this.webClient = webClientBuilder.build();
        log.info("Credential verification running in {} mode", this.mode);
    }

    @Override
    public VerificationResult verify(String issuer, String credentialCode, String title, String holderName) {
        if (credentialCode == null || credentialCode.isBlank()) {
            return new VerificationResult(false, "No credential code to verify");
        }
        return switch (mode) {
            case MOCK -> verifyMock(credentialCode);
            case LIVE -> verifyLive(new CredentialRequest(issuer, credentialCode.strip(), title, holderName));
        };
    }

    private VerificationResult verifyMock(String credentialCode) {
        return credentialCode.strip().length() >= MOCK_MIN_CODE_LENGTH
                ? new VerificationResult(true, "Mock: credential code accepted")
                : new VerificationResult(false, "Mock: credential code too short (min %d characters)"
                .formatted(MOCK_MIN_CODE_LENGTH));
    }

    private VerificationResult verifyLive(CredentialRequest request) {
        var issuerKey = request.issuer() == null ? "" : request.issuer().strip().toLowerCase(Locale.ROOT);
        var client = issuerClients.get(issuerKey);
        if (client == null) {
            return VerificationResult.unavailable(
                    "Live verification is not available yet for issuer '%s'".formatted(request.issuer()));
        }
        return callWithResilience(client.apply(request));
    }

    /**
     * Timeout, exponential backoff on HTTP 429 and an inconclusive fallback. Package
     * visible so the resilience policy can be tested without network.
     */
    VerificationResult callWithResilience(Mono<VerificationResult> call) {
        try {
            var result = call
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, initialBackoff)
                            .filter(CredentialVerificationServiceImpl::isTooManyRequests))
                    .onErrorResume(ex -> {
                        log.warn("Credential verification unavailable: {}", ex.toString());
                        return Mono.just(VerificationResult.unavailable("Issuer unavailable; try again later"));
                    })
                    .block();
            return result != null ? result : VerificationResult.unavailable("Issuer returned no answer");
        } catch (RuntimeException ex) {
            log.warn("Credential verification failed: {}", ex.toString());
            return VerificationResult.unavailable("Issuer unavailable; try again later");
        }
    }

    private static boolean isTooManyRequests(Throwable ex) {
        return ex instanceof WebClientResponseException response
                && response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private static Mode parseMode(String mode) {
        try {
            return Mode.valueOf(mode.strip().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("external.credentials.mode must be 'mock' or 'live', got: " + mode);
        }
    }

    record CredentialRequest(String issuer, String credentialCode, String title, String holderName) {
    }
}
