package com.sas.assessment.exam.dto;

import com.sas.assessment.exam.domain.ExamStatus;
import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;

public record UpdateExamRequest(
    String title,
    String description,
    ExamStatus status,
    @Min(1) Integer durationMins,
    OffsetDateTime openAt,
    OffsetDateTime closeAt) {}
