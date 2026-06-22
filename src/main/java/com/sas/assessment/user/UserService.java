package com.sas.assessment.user;

import com.sas.assessment.common.dto.Auth0User;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User findById(String id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
  }

  public User findOrCreateUser(Auth0User userInfo) {
    return userRepository.findById(userInfo.userId()).orElseGet(() -> createNewUser(userInfo));
  }

  public User updateUserInfo(Auth0User userInfo) {
    User user = findOrCreateUser(userInfo);
    user.setName(userInfo.name());
    user.setNickname(userInfo.nickname());
    user.setEmailVerified(userInfo.emailVerified());
    return userRepository.save(user);
  }

  private User createNewUser(Auth0User userInfo) {
    User user =
        User.builder()
            .id(userInfo.userId())
            .email(userInfo.email())
            .name(userInfo.name())
            .nickname(userInfo.nickname())
            .emailVerified(userInfo.emailVerified())
            .build();

    return userRepository.save(user);
  }
}
