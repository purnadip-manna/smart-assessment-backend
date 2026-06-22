package com.sas.assessment.question.dto;

import com.sas.assessment.exam.domain.ExamType;
import com.sas.assessment.question.Question;

import java.util.UUID;

public record QuestionResponse(
    UUID id,
    String questionText,
    ExamType questionType,
    int maxPoints,
    int questionOrder,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption) {
  public static QuestionResponse from(Question q) {
    return new QuestionResponse(
        q.getId(),
        q.getQuestionText(),
        q.getQuestionType(),
        q.getMaxPoints(),
        q.getQuestionOrder(),
        q.getOptionA(),
        q.getOptionB(),
        q.getOptionC(),
        q.getOptionD(),
        q.getCorrectOption());
  }

  public static QuestionResponse fromForStudent(Question q) {
    return new QuestionResponse(
        q.getId(),
        q.getQuestionText(),
        q.getQuestionType(),
        q.getMaxPoints(),
        q.getQuestionOrder(),
        q.getOptionA(),
        q.getOptionB(),
        q.getOptionC(),
        q.getOptionD(),
        null // hide correct answer from students
        );
  }
}
