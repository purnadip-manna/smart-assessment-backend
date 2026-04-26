package com.sas.assessment.config;

import com.sas.assessment.domain.User;
import com.sas.assessment.dto.user.Auth0User;
import com.sas.assessment.service.UserService;
import com.sas.assessment.util.Auth0ClaimsExtractor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
  private final UserService userService;

  @Override
  public void onAuthenticationSuccess(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Authentication authentication)
      throws IOException, ServletException {
    // Extract user info from Auth0 token
    Auth0User userInfo = Auth0ClaimsExtractor.extractUserInfo(authentication);
    // Save or update user in database
    User user = userService.updateUserInfo(userInfo);

    OAuth2AuthenticationToken newAuth =
        new OAuth2AuthenticationToken(
            (OAuth2User) authentication.getPrincipal(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId());

    SecurityContextHolder.getContext().setAuthentication(newAuth);

    // Call parent to continue with default behavior (redirect to target URL)
    super.onAuthenticationSuccess(request, response, newAuth);
  }
}
