package com.sas.assessment.attempt.dto;

import com.sas.assessment.attempt.domain.AttemptStatus;
import com.sas.assessment.attempt.domain.ExamAttempt;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Final (or in-progress-of-grading) result of an attempt. While the status is {@code GRADING},
 * {@code totalScore} is null and individual SAQ answers may still be {@code PENDING}.
 */
public record AttemptResultResponse(
    UUID attemptId,
    UUID examId,
    AttemptStatus status,
    Integer totalScore,
    int maxScore,
    OffsetDateTime submittedAt,
    OffsetDateTime gradedAt,
    List<AnswerResultResponse> answers) {

  public static AttemptResultResponse of(ExamAttempt attempt, List<AnswerResultResponse> answers) {
    return new AttemptResultResponse(
        attempt.getId(),
        attempt.getExam().getId(),
        attempt.getStatus(),
        attempt.getTotalScore(),
        attempt.getMaxScore(),
        attempt.getSubmittedAt(),
        attempt.getGradedAt(),
        answers);
  }
}
