package com.sas.assessment.attempt.domain;

/**
 * Grade state of a single answer. MCQ answers are set to {@code GRADED} synchronously at submit. SAQ
 * answers start {@code PENDING} and move to {@code GRADED}/{@code FAILED} via the evaluation-engine
 * callback.
 */
public enum AnswerGradeStatus {
  PENDING,
  GRADED,
  FAILED
}
