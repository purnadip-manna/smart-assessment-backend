package com.sas.assessment.knowledgebase;

import com.sas.assessment.knowledgebase.dto.IngestionStatusCallbackRequest;
import com.sas.assessment.knowledgebase.dto.KnowledgeBaseDocumentResponse;
import com.sas.assessment.user.domain.User;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class KnowledgeBaseController {

  private final KnowledgeBaseService knowledgeBaseService;

  // ---- Teacher-facing endpoints (Auth0 session + ownership) ----

  @PostMapping(
      value = "/exams/{examId}/knowledge-base",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('TEACHER')")
  public KnowledgeBaseDocumentResponse upload(
      @PathVariable UUID examId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal User teacher) {
    return knowledgeBaseService.upload(examId, file, teacher);
  }

  @GetMapping("/exams/{examId}/knowledge-base")
  @PreAuthorize("hasRole('TEACHER')")
  public List<KnowledgeBaseDocumentResponse> list(
      @PathVariable UUID examId, @AuthenticationPrincipal User teacher) {
    return knowledgeBaseService.list(examId, teacher);
  }

  @DeleteMapping("/exams/{examId}/knowledge-base/{documentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('TEACHER')")
  public void delete(
      @PathVariable UUID examId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal User teacher) {
    knowledgeBaseService.delete(examId, documentId, teacher);
  }

  // ---- Service-to-service callback (guarded by InternalApiKeyFilter, not Auth0) ----

  @PostMapping("/internal/knowledge-base/{documentId}/status")
  @ResponseStatus(HttpStatus.OK)
  public void updateIngestionStatus(
      @PathVariable UUID documentId,
      @Valid @RequestBody IngestionStatusCallbackRequest request) {
    knowledgeBaseService.applyStatusCallback(documentId, request);
  }
}
