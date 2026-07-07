package com.sas.assessment.knowledgebase.domain;

/**
 * Lifecycle of a knowledge-base document, driven partly by this service and partly by the external
 * evaluation engine (via the status callback).
 *
 * <ul>
 *   <li>{@code PENDING} - stored in S3, row persisted, {@code DocumentUploaded} event published.
 *   <li>{@code INGESTING} - evaluation engine has picked up the file and started ingesting.
 *   <li>{@code READY} - ingested into the vector DB; usable for SAQ evaluation.
 *   <li>{@code FAILED} - ingestion failed; {@code errorMessage} carries the reason.
 * </ul>
 */
public enum KnowledgeBaseStatus {
  PENDING,
  INGESTING,
  READY,
  FAILED
}
