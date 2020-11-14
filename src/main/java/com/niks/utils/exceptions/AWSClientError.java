package com.niks.utils.exceptions;

public class AWSClientError extends Exception {

  public AWSClientError(final String errorMessage) {
    super(errorMessage);
  }
}
