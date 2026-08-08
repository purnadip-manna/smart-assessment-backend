package com.sas.assessment.attempt.dto;

import com.sas.assessment.attempt.domain.AnswerGradeStatus;
import com.sas.assessment.attempt.domain.AnswerSubmission;
import java.util.UUID;

/**
 * A student's saved answer to one question. Two views:
 *
 * <ul>
 *   <li>{@link #forState(AnswerSubmission)} - used while the attempt is in progress; hides
 *       grading info (points/feedback) so it cannot leak before grading completes.
 *   <li>{@link #forResult(AnswerSubmission)} - used on the result endpoint; includes scores and
 *       feedback.
 * </ul>
 */
public record AnswerResultResponse(
    UUID questionId,
    int maxPoints,
    String selectedOption,
    String answerText,
    Integer awardedPoints,
    AnswerGradeStatus gradeStatus,
    String feedback) {

  public static AnswerResultResponse forState(AnswerSubmission a) {
    return new AnswerResultResponse(
        a.getQuestion().getId(),
        a.getQuestion().getMaxPoints(),
        a.getSelectedOption(),
        a.getAnswerText(),
        null,
        null,
        null);
  }

  public static AnswerResultResponse forResult(AnswerSubmission a) {
    return new AnswerResultResponse(
        a.getQuestion().getId(),
        a.getQuestion().getMaxPoints(),
        a.getSelectedOption(),
        a.getAnswerText(),
        a.getAwardedPoints(),
        a.getGradeStatus(),
        a.getFeedback());
  }
}
