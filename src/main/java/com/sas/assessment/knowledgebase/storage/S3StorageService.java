package com.sas.assessment.knowledgebase.storage;

import com.sas.assessment.config.AwsProperties;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** Thin wrapper over the S3 client for storing and deleting knowledge-base documents. */
@Slf4j
@Service
public class S3StorageService {

  private final S3Client s3Client;
  private final AwsProperties awsProperties;

  public S3StorageService(S3Client s3Client, AwsProperties awsProperties) {
    this.s3Client = s3Client;
    this.awsProperties = awsProperties;
  }

  public String bucket() {
    return awsProperties.getS3().getBucket();
  }

  /** Builds a stable, collision-free key: {@code exams/{examId}/kb/{documentId}/{safeFilename}}. */
  public String buildKey(UUID examId, UUID documentId, String originalFilename) {
    return "exams/%s/kb/%s/%s".formatted(examId, documentId, sanitize(originalFilename));
  }

  public void put(String key, MultipartFile file) throws IOException {
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket())
            .key(key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();
    s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    log.info("Stored knowledge-base object s3://{}/{}", bucket(), key);
  }

  public void delete(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket()).key(key).build());
    log.info("Deleted knowledge-base object s3://{}/{}", bucket(), key);
  }

  private static String sanitize(String filename) {
    if (filename == null || filename.isBlank()) {
      return "document.pdf";
    }
    // Keep only the base name and strip anything that could alter the key path.
    String base = filename.replace("\\", "/");
    base = base.substring(base.lastIndexOf('/') + 1);
    return base.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
