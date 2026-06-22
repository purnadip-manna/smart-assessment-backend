package com.sas.assessment.question.dto;

import com.sas.assessment.exam.domain.ExamType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuestionRequest(
    @NotBlank String questionText,
    @NotNull ExamType questionType,
    @Min(1) int maxPoints,
    int questionOrder,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption) {}
