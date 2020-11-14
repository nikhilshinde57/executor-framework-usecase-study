package com.niks.services.Exception;

public class S3FileDownloadFailed extends Exception {
  public S3FileDownloadFailed(final String errorMessage) {
    super(errorMessage);
  }
}
