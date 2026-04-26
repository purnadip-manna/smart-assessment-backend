package com.sas.assessment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

  public SecurityConfig(OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) {
    this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, AccessDeniedHandler accessDeniedHandler) {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
        .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler))
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");

      Map<String, Object> body = new LinkedHashMap<>();
      body.put("timestamp", OffsetDateTime.now().toString());
      body.put("status", HttpServletResponse.SC_FORBIDDEN);
      body.put("error", "Forbidden");
      body.put("message", "You do not have permission to access this resource");

      ObjectMapper mapper = new ObjectMapper();
      response.getWriter().write(mapper.writeValueAsString(body));
    };
  }
}
