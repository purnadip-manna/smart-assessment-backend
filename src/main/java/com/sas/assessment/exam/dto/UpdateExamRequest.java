package com.sas.assessment.exam.dto;

import jakarta.validation.constraints.Min;
import java.time.OffsetDateTime;

public record UpdateExamRequest(
    String title,
    String description,
    @Min(1) Integer durationMins,
    OffsetDateTime openAt,
    OffsetDateTime closeAt) {}
