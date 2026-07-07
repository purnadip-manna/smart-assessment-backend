package com.sas.assessment.knowledgebase.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The integration contract published to AWS SNS after a knowledge-base PDF is stored in S3. The
 * external evaluation engine consumes this off its SQS subscription, reads the file directly from
 * S3 ({@code s3Bucket}/{@code s3Key}), ingests it into its vector DB, and reports progress back to
 * {@code callbackUrl}.
 *
 * <p>This same record is also used as the in-process Spring application event so that publishing to
 * SNS happens only after the DB transaction commits (see {@code DocumentEventPublisher}).
 *
 * <p>Keep schema changes additive; bump {@code schemaVersion} for breaking changes.
 */
public record DocumentUploadedEvent(
    String eventType,
    int schemaVersion,
    UUID documentId,
    UUID examId,
    String s3Bucket,
    String s3Key,
    String originalFilename,
    String contentType,
    long sizeBytes,
    String callbackUrl,
    OffsetDateTime occurredAt) {

  public static final String EVENT_TYPE = "DocumentUploaded";
  public static final int SCHEMA_VERSION = 1;

  public static DocumentUploadedEvent of(
      UUID documentId,
      UUID examId,
      String s3Bucket,
      String s3Key,
      String originalFilename,
      String contentType,
      long sizeBytes,
      String callbackUrl,
      OffsetDateTime occurredAt) {
    return new DocumentUploadedEvent(
        EVENT_TYPE,
        SCHEMA_VERSION,
        documentId,
        examId,
        s3Bucket,
        s3Key,
        originalFilename,
        contentType,
        sizeBytes,
        callbackUrl,
        occurredAt);
  }
}
