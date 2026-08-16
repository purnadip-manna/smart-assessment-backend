package com.sas.assessment.exam;

import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.exam.dto.CreateExamRequest;
import com.sas.assessment.exam.dto.ExamResponse;
import com.sas.assessment.exam.dto.UpdateExamRequest;
import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.exception.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.sas.assessment.user.UserService;
import com.sas.assessment.user.domain.User;
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

  public ExamResponse createExam(CreateExamRequest request, User teacher) {
    Exam exam =
        Exam.builder()
            .examType(request.examType())
            .title(request.title())
            .description(request.description())
            .durationMins(request.durationMins())
            .openAt(request.openAt())
            .closeAt(request.closeAt())
            .createdBy(teacher)
            .build();

    return ExamResponse.from(examRepository.save(exam));
  }

  public ExamResponse getExam(UUID examId) {
    return ExamResponse.from(findById(examId));
  }

  public List<ExamResponse> getExams() {
    return examRepository.findAll().stream().map(ExamResponse::from).toList();
  }

  public ExamResponse updateExam(UUID examId, UpdateExamRequest request, User teacher) {
    Exam exam = findAndVerifyOwnership(examId, teacher);
    verifyExamIsDraft(exam);

    if (request.title() != null) exam.setTitle(request.title());
    if (request.description() != null) exam.setDescription(request.description());
    if (request.status() != null) exam.setStatus(request.status());
    if (request.durationMins() != null) exam.setDurationMins(request.durationMins());
    if (request.openAt() != null) exam.setOpenAt(request.openAt());
    if (request.closeAt() != null) exam.setCloseAt(request.closeAt());

    return ExamResponse.from(examRepository.save(exam));
  }

  public void deleteExam(UUID examId, User teacher) {
    Exam exam = findAndVerifyOwnership(examId, teacher);
    verifyExamIsDraft(exam);
    examRepository.delete(exam);
  }

  public ExamResponse showResults(UUID examId, User teacher) {
    Exam exam = findAndVerifyOwnership(examId, teacher);
    exam.setResultsVisibleAt(OffsetDateTime.now());
    return ExamResponse.from(examRepository.save(exam));
  }

  public Exam findAndVerifyOwnership(UUID examId, User teacher) {
    Exam exam = findById(examId);
    if (!exam.getCreatedBy().getId().equals(teacher.getId())) {
      throw new BadRequestException("You are not the owner of this exam");
    }
    return exam;
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
