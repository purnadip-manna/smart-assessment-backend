package com.sas.assessment.service;

import com.sas.assessment.domain.Exam;
import com.sas.assessment.domain.ExamStatus;
import com.sas.assessment.dto.exam.CreateExamRequest;
import com.sas.assessment.dto.exam.ExamResponse;
import com.sas.assessment.dto.exam.UpdateExamRequest;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExamService {
  private final ExamRepository examRepository;

  public ExamService(ExamRepository examRepository) {
    this.examRepository = examRepository;
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
            .build();

    return ExamResponse.from(examRepository.save(exam));
  }

  public ExamResponse getExam(UUID examId) {
    return ExamResponse.from(findById(examId));
  }

  public List<ExamResponse> getExamsByTeacher() {
    return examRepository.findAll().stream().map(ExamResponse::from).toList();
  }

  public ExamResponse updateExam(UUID examId, UpdateExamRequest request) {
    Exam exam =
        findAndVerifyOwnership(
            examId
            //            , teacher
            );
    if (exam.getStatus() != ExamStatus.DRAFT) {
      throw new BadRequestException("Only DRAFT exams can be updated");
    }

    if (request.title() != null) exam.setTitle(request.title());
    if (request.description() != null) exam.setDescription(request.description());
    if (request.durationMins() != null) exam.setDurationMins(request.durationMins());
    if (request.openAt() != null) exam.setOpenAt(request.openAt());
    if (request.closeAt() != null) exam.setCloseAt(request.closeAt());

    return ExamResponse.from(examRepository.save(exam));
  }

  public void deleteExam(UUID examId) {
    Exam exam = findAndVerifyOwnership(examId);
    if (exam.getStatus() != ExamStatus.DRAFT) {
      throw new BadRequestException("Only DRAFT exams can be deleted");
    }
    examRepository.delete(exam);
  }

  private Exam findAndVerifyOwnership(UUID examId
      //          , User teacher
      ) {
    Exam exam = findById(examId);
    //    if (!exam.getTeacher().getId().equals(teacher.getId())) {
    //      throw new ForbiddenException("You do not own this exam");
    //    }
    return exam;
  }

  public Exam findById(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
  }
}
