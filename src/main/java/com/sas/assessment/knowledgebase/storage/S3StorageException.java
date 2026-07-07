package com.sas.assessment.knowledgebase.storage;

/** Raised when storing/deleting a document in S3 fails. Mapped to 502 by the global handler. */
public class S3StorageException extends RuntimeException {
  public S3StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
