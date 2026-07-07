package com.sas.assessment.attempt;

import com.sas.assessment.attempt.dto.AnswerResultResponse;
import com.sas.assessment.attempt.dto.AttemptResultResponse;
import com.sas.assessment.attempt.dto.AttemptStateResponse;
import com.sas.assessment.attempt.dto.GradeAnswerCallbackRequest;
import com.sas.assessment.attempt.dto.SaveAnswerRequest;
import com.sas.assessment.attempt.dto.SubmitAttemptResponse;
import com.sas.assessment.user.domain.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1")
public class AttemptController {

  private final AttemptService attemptService;

  // ---- Student-facing endpoints ----

  @PostMapping("/exams/{examId}/attempts")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('STUDENT')")
  public AttemptStateResponse start(
      @PathVariable UUID examId, @AuthenticationPrincipal User student) {
    return attemptService.start(examId, student);
  }

  @GetMapping("/exams/{examId}/attempts/me")
  @PreAuthorize("hasRole('STUDENT')")
  public AttemptStateResponse myAttempt(
      @PathVariable UUID examId, @AuthenticationPrincipal User student) {
    return attemptService.getState(examId, student);
  }

  @PutMapping("/attempts/{attemptId}/answers")
  @PreAuthorize("hasRole('STUDENT')")
  public AnswerResultResponse saveAnswer(
      @PathVariable UUID attemptId,
      @Valid @RequestBody SaveAnswerRequest request,
      @AuthenticationPrincipal User student) {
    return attemptService.saveAnswer(attemptId, request, student);
  }

  @PostMapping("/attempts/{attemptId}/submit")
  @PreAuthorize("hasRole('STUDENT')")
  public SubmitAttemptResponse submit(
      @PathVariable UUID attemptId, @AuthenticationPrincipal User student) {
    return attemptService.submit(attemptId, student);
  }

  @GetMapping("/attempts/{attemptId}/result")
  @PreAuthorize("hasRole('STUDENT')")
  public AttemptResultResponse result(
      @PathVariable UUID attemptId, @AuthenticationPrincipal User student) {
    return attemptService.getResult(attemptId, student);
  }

  // ---- Service-to-service callback (guarded by InternalApiKeyFilter) ----

  @PostMapping("/internal/attempts/{attemptId}/answers/{answerId}/grade")
  @ResponseStatus(HttpStatus.OK)
  public void gradeAnswer(
      @PathVariable UUID attemptId,
      @PathVariable UUID answerId,
      @Valid @RequestBody GradeAnswerCallbackRequest request) {
    attemptService.applyGradeCallback(attemptId, answerId, request);
  }
}
