package com.niks.broker.sqs.consumer;

import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.MessageProcessor;
import com.niks.services.sqs.SQSMessageProcessorFactory;
import java.util.Map;
import javax.annotation.PostConstruct;
import com.niks.constants.HelperConstants;
import com.niks.constants.PropertiesConstant;
import com.niks.enums.SQSEventType;
import com.niks.properties.EnvironmentProperties;
import com.niks.services.Exception.S3FileDownloadFailed;
import com.niks.utils.exceptions.AWSClientError;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

@Service
@EnableSqs
public class SQSConsumer {

  @Autowired
  EnvironmentProperties environmentProperties;
  @Autowired
  SQSMessageProcessorFactory sqsMessageProcessorFactory;

  private static final Logger LOGGER = LoggerFactory.getLogger(SQSConsumer.class);

  @PostConstruct
  public void configure() {

    System.setProperty("aws.queue.url",
        environmentProperties.getProperty(PropertiesConstant.QUEUE_URL));
  }

  /*
   @SqsListener annotation listen to the provided SQS queue url
   Whenever message published on sqs it will automatically consume it
   *  */
  @SqsListener(value = "${aws.queue.url}")
  public void startListener(final String sqsMessageBody,
      @Headers() final Map<String, String> sqsHeaders) {

    //Get the RECEIPT_HANDLE from header used in the message deletion
    final String messageReceiptHandle = sqsHeaders.get(HelperConstants.RECEIPT_HANDLE);
    MessageProcessor messageProcessor = sqsMessageProcessorFactory
        .getMessageProcessor(SQSEventType.MERGE_SORTED_FILES);

    try {
      LOGGER.info(String.format("SQS message received: %s ", sqsMessageBody));
      messageProcessor.processMessage(sqsMessageBody);
      messageProcessor.deleteMessage(messageReceiptHandle, sqsMessageBody);
    } catch (InvalidSQSMessage ex) {
      //Delete invalid message
      messageProcessor.deleteMessage(messageReceiptHandle, sqsMessageBody);
    } catch (AWSClientError awsClientError) {
      //Don't delete the message from Queue will retry that after some time
      LOGGER.error(String.format("Failed to process message: %s", sqsMessageBody), awsClientError);

    } catch (S3FileDownloadFailed | S3FileUploadFailed s3OperationError) {
      //Don't  delete the message from Queue will retry that after some time
      LOGGER.error(String.format("Failed to process message: %s", sqsMessageBody), s3OperationError);
    }
  }
}
