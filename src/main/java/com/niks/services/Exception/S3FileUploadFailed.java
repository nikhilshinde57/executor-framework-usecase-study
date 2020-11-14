package com.niks.services.Exception;

public class S3FileUploadFailed extends Exception {
  public S3FileUploadFailed(final String errorMessage) {
    super(errorMessage);
  }
}
