package com.sas.assessment.question.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateQuestionRequest(
    @NotBlank String questionText,
    @Min(1) int maxPoints,
    int questionOrder,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption) {}
