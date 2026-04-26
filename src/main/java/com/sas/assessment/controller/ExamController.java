package com.sas.assessment.controller;

import com.sas.assessment.dto.exam.CreateExamRequest;
import com.sas.assessment.dto.exam.ExamResponse;
import com.sas.assessment.dto.exam.UpdateExamRequest;
import com.sas.assessment.service.ExamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {
  private final ExamService examService;

  public ExamController(ExamService examService) {
    this.examService = examService;
  }

  @GetMapping
  public List<ExamResponse> list() {
    return examService.getExams();
  }

  @PostMapping
  @PreAuthorize("hasRole('TEACHER')")
  public ExamResponse create(@Valid @RequestBody CreateExamRequest request) {
    return examService.createExam(request);
  }

  @GetMapping("/{id}")
  public ExamResponse get(@PathVariable UUID id) {
    return examService.getExam(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('TEACHER')")
  public ExamResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateExamRequest request) {
    return examService.updateExam(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('TEACHER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    examService.deleteExam(id);
  }
}
