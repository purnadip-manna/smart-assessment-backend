package com.sas.assessment.attempt.dto;

import com.sas.assessment.attempt.domain.AnswerGradeStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload the evaluation engine POSTs back to grade one SAQ answer. {@code status} is {@code GRADED}
 * or {@code FAILED}; {@code score} is required (and clamped to the question's maxPoints) when {@code
 * GRADED}; {@code feedback} is optional explanatory text shown to the student.
 */
public record GradeAnswerCallbackRequest(
    @NotNull AnswerGradeStatus status, Integer score, String feedback) {}
