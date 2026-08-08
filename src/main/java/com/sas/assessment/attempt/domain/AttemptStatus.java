package com.sas.assessment.attempt.domain;

/**
 * Lifecycle of a student's exam attempt.
 *
 * <ul>
 *   <li>{@code IN_PROGRESS} - started; the student can save/update answers.
 *   <li>{@code SUBMITTED} - finalized; transient state for SAQ before grading kicks in.
 *   <li>{@code GRADING} - SAQ answers dispatched to the evaluation engine, awaiting callbacks.
 *   <li>{@code GRADED} - all answers scored; {@code totalScore} is final.
 * </ul>
 *
 * <p>MCQ exams are graded synchronously at submit and land directly on {@code GRADED}.
 */
public enum AttemptStatus {
  IN_PROGRESS,
  SUBMITTED,
  GRADING,
  GRADED
}
