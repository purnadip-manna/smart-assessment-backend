package com.sas.assessment.service;

import com.sas.assessment.domain.User;
import com.sas.assessment.dto.user.Auth0User;
import com.sas.assessment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /** Find or create user from Auth0 claims */
  public User findOrCreateUser(Auth0User userInfo) {
    return userRepository.findByUserId(userInfo.userId()).orElseGet(() -> createNewUser(userInfo));
  }

  private User createNewUser(Auth0User userInfo) {
    User user =
        User.builder()
            .userId(userInfo.userId())
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
}
