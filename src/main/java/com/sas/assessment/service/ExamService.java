package com.sas.assessment.service;

import com.sas.assessment.domain.Exam;
import com.sas.assessment.domain.ExamStatus;
import com.sas.assessment.dto.exam.CreateExamRequest;
import com.sas.assessment.dto.exam.ExamResponse;
import com.sas.assessment.dto.exam.UpdateExamRequest;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.repository.ExamRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExamService {
  private final ExamRepository examRepository;
  private final UserService userService;

  public ExamService(ExamRepository examRepository, UserService userService) {
    this.examRepository = examRepository;
    this.userService = userService;
  }

  public ExamResponse createExam(CreateExamRequest request) {
    Exam exam =
        Exam.builder()
            .examType(request.examType())
            .title(request.title())
            .description(request.description())
            .durationMins(request.durationMins())
            .openAt(request.openAt())
            .closeAt(request.closeAt())
            .createdBy(userService.getCurrentUser())
            .build();

    return ExamResponse.from(examRepository.save(exam));
  }

  public ExamResponse getExam(UUID examId) {
    return ExamResponse.from(findById(examId));
  }

  public List<ExamResponse> getExams() {
    return examRepository.findAll().stream().map(ExamResponse::from).toList();
  }

  public ExamResponse updateExam(UUID examId, UpdateExamRequest request) {
    Exam exam = findAndVerifyOwnership(examId);
    verifyExamIsDraft(exam);

    if (request.title() != null) exam.setTitle(request.title());
    if (request.description() != null) exam.setDescription(request.description());
    if (request.durationMins() != null) exam.setDurationMins(request.durationMins());
    if (request.openAt() != null) exam.setOpenAt(request.openAt());
    if (request.closeAt() != null) exam.setCloseAt(request.closeAt());

    return ExamResponse.from(examRepository.save(exam));
  }

  public void deleteExam(UUID examId) {
    Exam exam = findAndVerifyOwnership(examId);
    verifyExamIsDraft(exam);
    examRepository.delete(exam);
  }

  private Exam findAndVerifyOwnership(UUID examId) {
    return findById(examId);
  }

  public Exam findById(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
  }

  private void verifyExamIsDraft(Exam exam) {
    if (exam.getStatus() != ExamStatus.DRAFT) {
      throw new BadRequestException("Only DRAFT exams can be modified");
    }
  }
}
