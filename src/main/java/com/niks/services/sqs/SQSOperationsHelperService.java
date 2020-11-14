package com.niks.services.sqs;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.niks.constants.ErrorConstants;
import com.niks.model.SQSMessagePayload;
import com.niks.properties.AWSSQSProperties;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SQSOperationsHelperService {

  @Autowired
  AWSSQSProperties awssqsProperties;

  private static final Logger LOGGER = LoggerFactory.getLogger(SQSOperationsHelperService.class);

  public SQSMessagePayload getMessageBodyObj(final String messageBodyJson) throws InvalidSQSMessage {
    try {
      Gson gson = new GsonBuilder().create();
      return gson.fromJson(messageBodyJson, SQSMessagePayload.class);
    } catch (JsonParseException ex) {
      LOGGER.error(String.format("Unable to parse message body: %s", messageBodyJson));
      LOGGER.error("Exception details: {}", ex.getMessage());
      throw new InvalidSQSMessage(ErrorConstants.INVALID_SQS_MESSAGE);
    }
  }

  public void deleteSQSMessage(final String messageReceiptHandle, final String queueUrl) {

    final AmazonSQSClient amazonSQSClient = awssqsProperties.getAwsSQSClientConnection();
    amazonSQSClient.deleteMessage(queueUrl, messageReceiptHandle);
  }
}
