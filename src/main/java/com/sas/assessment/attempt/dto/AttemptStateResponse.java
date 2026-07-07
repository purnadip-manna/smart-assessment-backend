package com.sas.assessment.attempt.dto;

import com.sas.assessment.attempt.domain.AttemptStatus;
import com.sas.assessment.attempt.domain.ExamAttempt;
import com.sas.assessment.question.dto.QuestionResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The state a student needs to take the exam: attempt metadata, the exam's questions (with correct
 * answers hidden via {@link QuestionResponse#fromForStudent}) and any answers already saved.
 */
public record AttemptStateResponse(
    UUID attemptId,
    UUID examId,
    AttemptStatus status,
    OffsetDateTime startedAt,
    OffsetDateTime deadlineAt,
    int maxScore,
    List<QuestionResponse> questions,
    List<AnswerResultResponse> answers) {

  public static AttemptStateResponse of(
      ExamAttempt attempt,
      List<QuestionResponse> questions,
      List<AnswerResultResponse> answers) {
    return new AttemptStateResponse(
        attempt.getId(),
        attempt.getExam().getId(),
        attempt.getStatus(),
        attempt.getStartedAt(),
        attempt.getDeadlineAt(),
        attempt.getMaxScore(),
        questions,
        answers);
  }
}
