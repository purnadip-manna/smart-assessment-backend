package com.sas.assessment.attempt.dto;

import com.sas.assessment.attempt.domain.AttemptStatus;
import com.sas.assessment.attempt.domain.ExamAttempt;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Returned immediately after submit. For MCQ the attempt is already {@code GRADED} with a {@code
 * totalScore}; for SAQ it is {@code GRADING} and {@code totalScore} is null until the engine
 * finishes.
 */
public record SubmitAttemptResponse(
    UUID attemptId,
    AttemptStatus status,
    Integer totalScore,
    int maxScore,
    OffsetDateTime submittedAt) {

  public static SubmitAttemptResponse from(ExamAttempt attempt) {
    return new SubmitAttemptResponse(
        attempt.getId(),
        attempt.getStatus(),
        attempt.getTotalScore(),
        attempt.getMaxScore(),
        attempt.getSubmittedAt());
  }
}
