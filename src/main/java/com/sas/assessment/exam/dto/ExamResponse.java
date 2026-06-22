package com.sas.assessment.exam.dto;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.exam.domain.ExamType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExamResponse(
    UUID id,
    String title,
    String description,
    ExamType examType,
    int durationMins,
    OffsetDateTime openAt,
    OffsetDateTime closeAt,
    ExamStatus status,
    OffsetDateTime createdAt
    // TODO: Need to add the following fields: questionCount, created_by (Teacher Name),
    //  KnowledgeBase ID
    ) {

  public static ExamResponse from(Exam exam) {
    return new ExamResponse(
        exam.getId(),
        exam.getTitle(),
        exam.getDescription(),
        exam.getExamType(),
        exam.getDurationMins(),
        exam.getOpenAt(),
        exam.getCloseAt(),
        exam.getStatus(),
        exam.getCreatedAt());
  }
}
