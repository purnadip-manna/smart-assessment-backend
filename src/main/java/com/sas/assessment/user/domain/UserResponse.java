package com.sas.assessment.user.domain;

import com.sas.assessment.user.domain.Role;
import com.sas.assessment.user.domain.User;

/** Current-user profile returned to the frontend so it can render role-based UI. */
public record UserResponse(String id, String email, String name, String nickname, Role role) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(), user.getEmail(), user.getName(), user.getNickname(), user.getRole());
  }
}
