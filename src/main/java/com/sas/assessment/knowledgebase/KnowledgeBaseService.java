package com.sas.assessment.knowledgebase;

import com.sas.assessment.config.AwsProperties;
import com.sas.assessment.exam.ExamService;
import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.knowledgebase.domain.KnowledgeBaseDocument;
import com.sas.assessment.knowledgebase.domain.KnowledgeBaseStatus;
import com.sas.assessment.knowledgebase.dto.DocumentUploadedEvent;
import com.sas.assessment.knowledgebase.dto.IngestionStatusCallbackRequest;
import com.sas.assessment.knowledgebase.dto.KnowledgeBaseDocumentResponse;
import com.sas.assessment.knowledgebase.storage.S3StorageException;
import com.sas.assessment.knowledgebase.storage.S3StorageService;
import com.sas.assessment.user.domain.User;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class KnowledgeBaseService {

  private static final String PDF_CONTENT_TYPE = "application/pdf";

  private final KnowledgeBaseRepository repository;
  private final ExamService examService;
  private final S3StorageService storageService;
  private final ApplicationEventPublisher eventPublisher;
  private final AwsProperties awsProperties;

  public KnowledgeBaseService(
      KnowledgeBaseRepository repository,
      ExamService examService,
      S3StorageService storageService,
      ApplicationEventPublisher eventPublisher,
      AwsProperties awsProperties) {
    this.repository = repository;
    this.examService = examService;
    this.storageService = storageService;
    this.eventPublisher = eventPublisher;
    this.awsProperties = awsProperties;
  }

  /**
   * Teacher uploads a PDF for an exam they own. Order matters: S3 first, then persist {@code
   * PENDING}, then publish the SNS event only after commit. A rollback therefore never leaves an
   * event pointing at a non-existent document row.
   */
  @Transactional
  public KnowledgeBaseDocumentResponse upload(UUID examId, MultipartFile file, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);
    if (exam.getStatus() == ExamStatus.CLOSED) {
      throw new BadRequestException("Cannot add knowledge-base documents to a closed exam");
    }
    validate(file);

    // The S3 "folder" id only needs to be unique; the document's identity is its DB-generated id.
    String key = storageService.buildKey(examId, UUID.randomUUID(), file.getOriginalFilename());
    try {
      storageService.put(key, file);
    } catch (IOException e) {
      throw new S3StorageException("Failed to read uploaded file", e);
    }

    KnowledgeBaseDocument saved =
        repository.save(
            KnowledgeBaseDocument.builder()
                .exam(exam)
                .originalFilename(file.getOriginalFilename())
                .s3Bucket(storageService.bucket())
                .s3Key(key)
                .contentType(PDF_CONTENT_TYPE)
                .sizeBytes(file.getSize())
                .status(KnowledgeBaseStatus.PENDING)
                .build());

    eventPublisher.publishEvent(
        DocumentUploadedEvent.of(
            saved.getId(),
            exam.getId(),
            saved.getS3Bucket(),
            saved.getS3Key(),
            saved.getOriginalFilename(),
            saved.getContentType(),
            saved.getSizeBytes(),
            buildCallbackUrl(saved.getId()),
            saved.getCreatedAt()));

    return KnowledgeBaseDocumentResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public List<KnowledgeBaseDocumentResponse> list(UUID examId, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);
    return repository.findAllByExamOrderByCreatedAt(exam).stream()
        .map(KnowledgeBaseDocumentResponse::from)
        .toList();
  }

  /**
   * Deletes the document row (committed immediately by Spring Data) and then best-effort removes the
   * S3 object. A failed S3 delete is logged but does not fail the request; orphaned objects are left
   * to bucket lifecycle rules.
   */
  public void delete(UUID examId, UUID documentId, User teacher) {
    Exam exam = examService.findAndVerifyOwnership(examId, teacher);
    KnowledgeBaseDocument doc = findOwnedDocument(examId, documentId, exam);

    String key = doc.getS3Key();
    repository.delete(doc);
    try {
      storageService.delete(key);
    } catch (RuntimeException e) {
      log.warn("Deleted document {} but failed to remove S3 object {}", documentId, key, e);
    }
  }

  /**
   * Applies an ingestion status update from the evaluation engine. Idempotent for re-delivered
   * terminal statuses; forward-only (never reverts to {@code PENDING}).
   */
  @Transactional
  public void applyStatusCallback(UUID documentId, IngestionStatusCallbackRequest request) {
    KnowledgeBaseDocument doc =
        repository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Knowledge-base document not found"));

    KnowledgeBaseStatus newStatus = request.status();
    if (newStatus == KnowledgeBaseStatus.PENDING) {
      throw new BadRequestException("Cannot revert ingestion status to PENDING");
    }
    if (doc.getStatus() == newStatus) {
      return; // idempotent re-delivery
    }
    if (isTerminal(doc.getStatus())) {
      throw new BadRequestException(
          "Document is already in terminal status " + doc.getStatus());
    }

    doc.setStatus(newStatus);
    doc.setErrorMessage(newStatus == KnowledgeBaseStatus.FAILED ? request.errorMessage() : null);
    repository.save(doc);
    log.info("Document {} status updated to {}", documentId, newStatus);
  }

  private KnowledgeBaseDocument findOwnedDocument(UUID examId, UUID documentId, Exam exam) {
    KnowledgeBaseDocument doc =
        repository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Knowledge-base document not found"));
    if (!doc.getExam().getId().equals(exam.getId())) {
      throw new BadRequestException("Document does not belong to the specified exam");
    }
    return doc;
  }

  private void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Uploaded file is empty");
    }
    long maxBytes = awsProperties.getKnowledgeBase().getMaxFileSizeBytes();
    if (file.getSize() > maxBytes) {
      throw new BadRequestException("File exceeds the maximum allowed size of " + maxBytes + " bytes");
    }
    boolean looksLikePdf =
        PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())
            || (file.getOriginalFilename() != null
                && file.getOriginalFilename().toLowerCase().endsWith(".pdf"));
    if (!looksLikePdf) {
      throw new BadRequestException("Only PDF files are accepted");
    }
  }

  private boolean isTerminal(KnowledgeBaseStatus status) {
    return status == KnowledgeBaseStatus.READY || status == KnowledgeBaseStatus.FAILED;
  }

  private String buildCallbackUrl(UUID documentId) {
    String base = awsProperties.getKnowledgeBase().getCallbackBaseUrl();
    if (base == null) {
      base = "";
    }
    String trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    return trimmed + "/api/v1/internal/knowledge-base/" + documentId + "/status";
  }
}
