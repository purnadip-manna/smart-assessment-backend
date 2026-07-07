package com.sas.assessment.knowledgebase.messaging;

import com.sas.assessment.config.AwsProperties;
import com.sas.assessment.knowledgebase.dto.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Publishes {@link DocumentUploadedEvent}s to SNS. The publish is bound to {@link
 * TransactionPhase#AFTER_COMMIT} so a signal is never emitted for a document row that ends up
 * rolled back (which would otherwise leave the evaluation engine ingesting a file with no matching
 * record and 404-ing the callback).
 *
 * <p>If the SNS publish itself fails after commit the document remains in {@code PENDING} and the
 * failure is logged; a re-publish mechanism (or transactional outbox) is intentionally out of
 * scope.
 */
@Slf4j
@Component
public class DocumentEventPublisher {

  private final SnsClient snsClient;
  private final AwsProperties awsProperties;
  private final JsonMapper objectMapper;

  public DocumentEventPublisher(
      SnsClient snsClient, AwsProperties awsProperties, JsonMapper objectMapper) {
    this.snsClient = snsClient;
    this.awsProperties = awsProperties;
    this.objectMapper = objectMapper;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDocumentUploaded(DocumentUploadedEvent event) {
    String topicArn = awsProperties.getSns().getDocumentUploadedTopicArn();
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
          "Published {} event for document {} to topic {}",
          event.eventType(),
          event.documentId(),
          topicArn);
    } catch (JacksonException e) {
      log.error(
          "Failed to serialize {} event for document {}", event.eventType(), event.documentId(), e);
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish {} event for document {} to topic {}; document stays PENDING",
          event.eventType(),
          event.documentId(),
          topicArn,
          e);
    }
  }
}
