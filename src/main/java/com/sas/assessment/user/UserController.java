package com.sas.assessment.user;

import com.sas.assessment.user.domain.User;
import com.sas.assessment.user.domain.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal User user) {
    return UserResponse.from(user);
  }
}
