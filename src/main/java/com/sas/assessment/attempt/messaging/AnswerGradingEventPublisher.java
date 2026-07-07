package com.sas.assessment.attempt.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sas.assessment.attempt.dto.AnswerGradingRequestedEvent;
import com.sas.assessment.config.AwsProperties;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes {@link AnswerGradingRequestedEvent}s to SNS. Bound to {@link
 * TransactionPhase#AFTER_COMMIT} so no grading request is emitted for a submit that rolls back
 * (which would have the engine grade an answer with no committed row and 404 on its callback).
 *
 * <p>A publish failure after commit leaves the answer {@code PENDING} and is logged; re-publishing
 * (or a transactional outbox) is intentionally out of scope, matching {@code
 * DocumentEventPublisher}.
 */
@Slf4j
@Component
public class AnswerGradingEventPublisher {

  private final SnsClient snsClient;
  private final AwsProperties awsProperties;
  private final JsonMapper objectMapper;

  public AnswerGradingEventPublisher(
      SnsClient snsClient, AwsProperties awsProperties, JsonMapper objectMapper) {
    this.snsClient = snsClient;
    this.awsProperties = awsProperties;
    this.objectMapper = objectMapper;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onAnswerGradingRequested(AnswerGradingRequestedEvent event) {
    String topicArn = awsProperties.getSns().getAnswerGradingTopicArn();
    try {
      String payload = objectMapper.writeValueAsString(event);
      PublishRequest request =
          PublishRequest.builder()
              .topicArn(topicArn)
              .message(payload)
              .messageAttributes(
                  Map.of(
                      "eventType",
                      MessageAttributeValue.builder()
                          .dataType("String")
                          .stringValue(event.eventType())
                          .build()))
              .build();
      snsClient.publish(request);
      log.info(
          "Published {} event for answer {} (attempt {}) to topic {}",
          event.eventType(),
          event.answerId(),
          event.attemptId(),
          topicArn);
    } catch (JacksonException e) {
      log.error(
          "Failed to serialize {} event for answer {}", event.eventType(), event.answerId(), e);
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish {} event for answer {} to topic {}; answer stays PENDING",
          event.eventType(),
          event.answerId(),
          topicArn,
          e);
    }
  }
}
