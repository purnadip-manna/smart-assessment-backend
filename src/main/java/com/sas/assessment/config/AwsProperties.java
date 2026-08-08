package com.sas.assessment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for AWS access (S3 + SNS) and the knowledge-base ingestion flow. */
@Data
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

  /** AWS region, e.g. {@code eu-west-1}. */
  private String region;

  /**
   * Optional endpoint override. When set (e.g. {@code http://localhost:4566}) the S3/SNS clients
   * target LocalStack with static credentials and path-style S3 addressing. Leave blank in
   * production to use real AWS with the default credentials provider chain.
   */
  private String endpointOverride;

  /** Static access key, only used when {@link #endpointOverride} is set (local dev). */
  private String accessKey;

  /** Static secret key, only used when {@link #endpointOverride} is set (local dev). */
  private String secretKey;

  private final S3 s3 = new S3();
  private final Sns sns = new Sns();
  private final KnowledgeBase knowledgeBase = new KnowledgeBase();

  @Data
  public static class S3 {
    /** Bucket where knowledge-base PDFs are stored. */
    private String bucket;
  }

  @Data
  public static class Sns {
    /** Topic ARN the {@code DocumentUploaded} event is published to. */
    private String documentUploadedTopicArn;

    /** Topic ARN the {@code AnswerGradingRequested} event is published to (SAQ grading). */
    private String answerGradingTopicArn;
  }

  @Data
  public static class KnowledgeBase {
    /** Shared secret the evaluation engine must present (X-Internal-Api-Key) on the callback. */
    private String callbackSecret;

    /** Public base URL of this service, used to build the callback URL in the SNS event. */
    private String callbackBaseUrl;

    /** Max accepted upload size in bytes (defaults to 25 MB). */
    private long maxFileSizeBytes = 26_214_400L;
  }
}
