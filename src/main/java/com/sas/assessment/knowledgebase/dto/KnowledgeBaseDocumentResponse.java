package com.sas.assessment.knowledgebase.dto;

import com.sas.assessment.knowledgebase.domain.KnowledgeBaseDocument;
import com.sas.assessment.knowledgebase.domain.KnowledgeBaseStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record KnowledgeBaseDocumentResponse(
    UUID id,
    UUID examId,
    String originalFilename,
    String contentType,
    long sizeBytes,
    KnowledgeBaseStatus status,
    String errorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  public static KnowledgeBaseDocumentResponse from(KnowledgeBaseDocument doc) {
    return new KnowledgeBaseDocumentResponse(
        doc.getId(),
        doc.getExam().getId(),
        doc.getOriginalFilename(),
        doc.getContentType(),
        doc.getSizeBytes(),
        doc.getStatus(),
        doc.getErrorMessage(),
        doc.getCreatedAt(),
        doc.getUpdatedAt());
  }
}
