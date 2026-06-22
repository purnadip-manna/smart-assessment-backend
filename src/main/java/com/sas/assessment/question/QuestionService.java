package com.sas.assessment.question;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.question.dto.CreateQuestionRequest;
import com.sas.assessment.question.dto.QuestionResponse;
import com.sas.assessment.exam.ExamService;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;
  private final ExamService examService;

  public QuestionService(QuestionRepository questionRepository, ExamService examService) {
    this.questionRepository = questionRepository;
    this.examService = examService;
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
            .questionType(request.questionType())
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

  public List<QuestionResponse> getQuestions(UUID examId) {
    Exam exam = examService.findById(examId);
    List<Question> questions = questionRepository.findAllByExamOrderByQuestionOrder(exam);
    return questions.stream().map(QuestionResponse::from).toList();
  }
}
