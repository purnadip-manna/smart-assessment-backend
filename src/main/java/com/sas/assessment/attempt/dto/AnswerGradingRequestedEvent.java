package com.sas.assessment.attempt.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Integration contract published to SNS (one event per SAQ answer) when a student submits an SAQ
 * attempt. The evaluation engine consumes it off SQS, grades {@code answerText} against the exam's
 * knowledge base (already ingested, keyed by {@code examId}) using an LLM, and POSTs the score +
 * feedback back to {@code callbackUrl}.
 *
 * <p>Also used as the in-process Spring event so the SNS publish happens only after the submit
 * transaction commits. Keep schema changes additive; bump {@code schemaVersion} for breaking ones.
 */
public record AnswerGradingRequestedEvent(
    String eventType,
    int schemaVersion,
    UUID attemptId,
    UUID answerId,
    UUID questionId,
    UUID examId,
    String questionText,
    String answerText,
    int maxPoints,
    String callbackUrl,
    OffsetDateTime occurredAt) {

  public static final String EVENT_TYPE = "AnswerGradingRequested";
  public static final int SCHEMA_VERSION = 1;

  public static AnswerGradingRequestedEvent of(
      UUID attemptId,
      UUID answerId,
      UUID questionId,
      UUID examId,
      String questionText,
      String answerText,
      int maxPoints,
      String callbackUrl,
      OffsetDateTime occurredAt) {
    return new AnswerGradingRequestedEvent(
        EVENT_TYPE,
        SCHEMA_VERSION,
        attemptId,
        answerId,
        questionId,
        examId,
        questionText,
        answerText,
        maxPoints,
        callbackUrl,
        occurredAt);
  }
}
