package pe.upc.simutalk.shared.infrastructure.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import pe.upc.simutalk.shared.interfaces.rest.resources.ErrorResource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Transitional security configuration.
 * <p>
 * The {@code iam} bounded context (users, roles and JWT issuing/validation) is not
 * implemented yet. Until it is, the API runs stateless with CSRF disabled and every
 * {@code /api/v1/**} route open, so the endpoints can be exercised from Swagger UI.
 * When {@code iam} lands, it must register the JWT filter and replace
 * {@code permitAll()} on {@code /api/v1/**} with {@code authenticated()}.
 */
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private static final String[] PUBLIC_DOCUMENTATION = {
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(request, response, HttpStatus.UNAUTHORIZED, "Authentication is required"))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(request, response, HttpStatus.FORBIDDEN, "Access denied")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_DOCUMENTATION).permitAll()
                        .requestMatchers("/api/v1/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll());
        return http.build();
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                            String message) throws IOException {
        var body = new ErrorResource(Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
