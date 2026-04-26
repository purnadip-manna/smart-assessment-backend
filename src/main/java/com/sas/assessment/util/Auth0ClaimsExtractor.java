package com.sas.assessment.util;

import com.sas.assessment.dto.user.Auth0User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.List;

public class Auth0ClaimsExtractor {
  public static Auth0User extractUserInfo(Authentication authentication) {
    if (authentication == null) {
      throw new IllegalArgumentException("Authentication is null");
    }

    if (!(authentication.getPrincipal() instanceof DefaultOidcUser oidcUser)) {
      throw new IllegalArgumentException("Authentication is not a OidcUser");
    }

    return new Auth0User(
        oidcUser.getSubject(),
        oidcUser.getEmail(),
        oidcUser.getEmailVerified(),
        oidcUser.getNickName(),
        oidcUser.getFullName(),
        List.of());
  }
}
