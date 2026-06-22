package com.sas.assessment.config;

import com.sas.assessment.user.domain.User;
import com.sas.assessment.common.dto.Auth0User;
import com.sas.assessment.user.UserService;
import com.sas.assessment.common.util.Auth0ClaimsExtractor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

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

    // Create authentication with the User object as the principal
    UsernamePasswordAuthenticationToken newAuth =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(newAuth);

    // Call parent to continue with default behavior (redirect to target URL)
    super.onAuthenticationSuccess(request, response, newAuth);
  }
}
