package com.sas.assessment.knowledgebase.dto;

import com.sas.assessment.knowledgebase.domain.KnowledgeBaseStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload the evaluation engine POSTs back to update a document's ingestion status. Expected values
 * are {@code INGESTING}, {@code READY} or {@code FAILED}; {@code errorMessage} is populated for
 * {@code FAILED}.
 */
public record IngestionStatusCallbackRequest(
    @NotNull KnowledgeBaseStatus status, String errorMessage) {}
