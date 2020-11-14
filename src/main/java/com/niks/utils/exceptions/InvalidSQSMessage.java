package com.niks.utils.exceptions;

public class InvalidSQSMessage extends Exception {

  public InvalidSQSMessage(final String errorMessage) {
    super(errorMessage);
  }

}
