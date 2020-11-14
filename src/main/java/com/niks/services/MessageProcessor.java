package com.niks.services;

import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.Exception.S3FileDownloadFailed;
import com.niks.utils.exceptions.AWSClientError;
import com.niks.utils.exceptions.InvalidSQSMessage;

public interface MessageProcessor {

  void processMessage(final String sqsMessageBody)
      throws InvalidSQSMessage, AWSClientError, S3FileDownloadFailed, S3FileUploadFailed;
  void deleteMessage(String messageReceiptHandle,String sqsMessageBody);

}
