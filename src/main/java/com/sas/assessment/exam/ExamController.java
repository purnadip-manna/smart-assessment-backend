package com.sas.assessment.exam;

import com.sas.assessment.exam.dto.CreateExamRequest;
import com.sas.assessment.exam.dto.ExamResponse;
import com.sas.assessment.exam.dto.UpdateExamRequest;
import com.sas.assessment.question.dto.CreateQuestionRequest;
import com.sas.assessment.question.dto.QuestionResponse;
import com.sas.assessment.question.QuestionService;
import com.sas.assessment.user.domain.User;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {
  private final ExamService examService;
  private final QuestionService questionService;

  @GetMapping
  public List<ExamResponse> list() {
    return examService.getExams();
  }

  @PostMapping
  @PreAuthorize("hasRole('TEACHER')")
  public ExamResponse create(
      @Valid @RequestBody CreateExamRequest request, @AuthenticationPrincipal User teacher) {
    return examService.createExam(request, teacher);
  }

  @GetMapping("/{id}")
  public ExamResponse get(@PathVariable UUID id) {
    return examService.getExam(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('TEACHER')")
  public ExamResponse update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateExamRequest request,
      @AuthenticationPrincipal User teacher) {
    return examService.updateExam(id, request, teacher);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('TEACHER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal User teacher) {
    examService.deleteExam(id, teacher);
  }

  @PostMapping("/{examId}/questions")
  @PreAuthorize("hasRole('TEACHER')")
  public QuestionResponse addQuestion(
      @PathVariable UUID examId,
      @Valid @RequestBody CreateQuestionRequest request,
      @AuthenticationPrincipal User teacher) {
    return questionService.addQuestion(examId, request, teacher);
  }

  @GetMapping("/{examId}/questions")
  public List<QuestionResponse> getQuestions(@PathVariable UUID examId) {
    return questionService.getQuestions(examId);
  }
}
