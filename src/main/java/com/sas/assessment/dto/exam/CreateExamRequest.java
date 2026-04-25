package com.sas.assessment.dto.exam;

import com.sas.assessment.domain.ExamType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateExamRequest(
    @NotBlank String title,
    String description,
    @NotNull ExamType examType,
    @NotNull @Min(1) Integer durationMins,
    OffsetDateTime openAt,
    OffsetDateTime closeAt
    // TODO:  Add UUID knowledgeBaseId after implementing KnowledgeBase feature implementation
    ) {}
