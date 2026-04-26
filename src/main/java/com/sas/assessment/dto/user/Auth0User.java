package com.sas.assessment.dto.user;

import java.util.List;

public record Auth0User(
    String userId,
    String email,
    boolean emailVerified,
    String nickname,
    String name,
    List<String> permissions) {}
