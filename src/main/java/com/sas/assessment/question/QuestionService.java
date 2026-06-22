package com.sas.assessment.question;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.question.dto.CreateQuestionRequest;
import com.sas.assessment.question.dto.QuestionResponse;
import com.sas.assessment.exam.ExamService;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.user.domain.Role;
import com.sas.assessment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;
  private final ExamService examService;

  public QuestionService(QuestionRepository questionRepository, ExamService examService) {
    this.questionRepository = questionRepository;
    this.examService = examService;
  }

  public List<QuestionResponse> getQuestions(UUID examId, User user) {
    Exam exam = examService.findById(examId);
    List<Question> questions = questionRepository.findAllByExamOrderByQuestionOrder(exam);
    if (Role.TEACHER.equals(user.getRole())) {
      return questions.stream().map(QuestionResponse::from).toList();
    } else {
      return questions.stream().map(QuestionResponse::fromForStudent).toList();
    }
  }

  @Transactional
  public List<QuestionResponse> addQuestions(
      UUID examId, List<CreateQuestionRequest> requests, User teacher) {
    return requests.stream().map(request -> addQuestion(examId, request, teacher)).toList();
  }

  @Transactional
  public QuestionResponse addQuestion(UUID examId, CreateQuestionRequest request, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);

    if (exam.getStatus() == ExamStatus.CLOSED) {
      throw new BadRequestException("Cannot add questions to a closed exam");
    }

    Question question =
        Question.builder()
            .exam(exam)
            .questionText(request.questionText())
            .questionType(exam.getExamType())
            .maxPoints(request.maxPoints())
            .questionOrder(request.questionOrder())
            .optionA(request.optionA())
            .optionB(request.optionB())
            .optionC(request.optionC())
            .optionD(request.optionD())
            .correctOption(request.correctOption())
            .build();

    return QuestionResponse.from(questionRepository.save(question));
  }

  public QuestionResponse updateQuestion(
      UUID examId, UUID questionId, CreateQuestionRequest request, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);
    Question question =
        questionRepository
            .findById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    if (question.getExam().getId() != exam.getId()) {
      throw new BadRequestException("Question does not belong to the specified exam");
    }
    return QuestionResponse.from(
        questionRepository.save(
            question.toBuilder()
                .questionText(request.questionText())
                .maxPoints(request.maxPoints())
                .questionOrder(request.questionOrder())
                .optionA(request.optionA())
                .optionB(request.optionB())
                .optionC(request.optionC())
                .optionD(request.optionD())
                .correctOption(request.correctOption())
                .build()));
  }

  public void deleteQuestion(UUID examId, UUID questionId, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);
    Question question = questionRepository.findById(questionId).orElse(null);
    if (Objects.isNull(question)) {
      return;
    }
    if (question.getExam().getId() != exam.getId()) {
      throw new BadRequestException("Question does not belong to the specified exam");
    }
    questionRepository.delete(question);
  }
}
