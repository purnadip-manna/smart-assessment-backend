package com.sas.assessment.service;

import com.sas.assessment.domain.User;
import com.sas.assessment.dto.user.Auth0User;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /** Find or create user from Auth0 claims */
  public User findOrCreateUser(Auth0User userInfo) {
    return userRepository.findById(userInfo.userId()).orElseGet(() -> createNewUser(userInfo));
  }

  private User createNewUser(Auth0User userInfo) {
    User user =
        User.builder()
            .id(userInfo.userId())
            .email(userInfo.email())
            .name(userInfo.name())
            .nickname(userInfo.nickname())
            .emailVerified(userInfo.emailVerified())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    return userRepository.save(user);
  }

  /** Update user info from Auth0 claims */
  public User updateUserInfo(Auth0User userInfo) {
    User user = findOrCreateUser(userInfo);
    user.setName(userInfo.name());
    user.setNickname(userInfo.nickname());
    user.setEmailVerified(userInfo.emailVerified());
    return userRepository.save(user);
  }

  public User getCurrentUser() {
    String email =
        ((DefaultOidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getEmail();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
  }
}
