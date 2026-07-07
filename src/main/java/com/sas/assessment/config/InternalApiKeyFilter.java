package com.sas.assessment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Guards the service-to-service callback endpoints under {@code /api/v1/internal/**} with a shared
 * secret presented in the {@code X-Internal-Api-Key} header. These endpoints are reached by the
 * external evaluation engine, which has no Auth0 session, so they are {@code permitAll()} in the
 * security filter chain and protected by this filter instead.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

  private static final String INTERNAL_PATH_PREFIX = "/api/v1/internal/";
  private static final String API_KEY_HEADER = "X-Internal-Api-Key";

  private final AwsProperties awsProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public InternalApiKeyFilter(AwsProperties awsProperties) {
    this.awsProperties = awsProperties;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String provided = request.getHeader(API_KEY_HEADER);
    String expected = awsProperties.getKnowledgeBase().getCallbackSecret();

    if (!StringUtils.hasText(expected) || !matches(provided, expected)) {
      writeUnauthorized(response);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean matches(String provided, String expected) {
    if (provided == null) {
      return false;
    }
    return MessageDigest.isEqual(
        provided.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
  }

  private void writeUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", OffsetDateTime.now().toString());
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("message", "Missing or invalid internal API key");

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
