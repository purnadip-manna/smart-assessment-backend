package com.sas.assessment.config;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

/**
 * Builds the S3 and SNS clients. When {@code aws.endpoint-override} is set the clients point at a
 * local emulator (LocalStack) with static credentials; otherwise they use the default AWS
 * credentials provider chain (env vars / IAM role) suitable for production.
 */
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfig {

  @Bean
  public S3Client s3Client(AwsProperties props) {
    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(props.getRegion()))
            .credentialsProvider(credentialsProvider(props));
    if (StringUtils.hasText(props.getEndpointOverride())) {
      builder
          .endpointOverride(URI.create(props.getEndpointOverride()))
          .forcePathStyle(true); // required for LocalStack (no virtual-host bucket URLs)
    }
    return builder.build();
  }

  @Bean
  public SnsClient snsClient(AwsProperties props) {
    SnsClientBuilder builder =
        SnsClient.builder()
            .region(Region.of(props.getRegion()))
            .credentialsProvider(credentialsProvider(props));
    if (StringUtils.hasText(props.getEndpointOverride())) {
      builder.endpointOverride(URI.create(props.getEndpointOverride()));
    }
    return builder.build();
  }

  private AwsCredentialsProvider credentialsProvider(AwsProperties props) {
    if (StringUtils.hasText(props.getEndpointOverride())) {
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
    }
    return DefaultCredentialsProvider.create();
  }
}
