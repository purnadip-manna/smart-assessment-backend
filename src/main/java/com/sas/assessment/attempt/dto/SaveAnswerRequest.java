package com.sas.assessment.attempt.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Upserts a single answer within an in-progress attempt. For MCQ exams set {@code selectedOption}
 * ('A'-'D'); for SAQ exams set {@code answerText}. The other field is ignored based on exam type.
 */
public record SaveAnswerRequest(
    @NotNull UUID questionId, String selectedOption, String answerText) {}
