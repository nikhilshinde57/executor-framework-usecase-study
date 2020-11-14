package com.niks.unit.service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.google.gson.Gson;
import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.file.FileReaderService;
import com.niks.services.file.FileWriterService;
import com.niks.services.merge.MArrayMergeService;
import com.niks.services.sqs.MergeFilesSQSMessageProcessor;
import com.niks.services.sqs.SQSOperationsHelperService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.niks.constants.ErrorConstants;
import com.niks.constants.PropertiesConstant;
import com.niks.model.SQSMessagePayload;
import com.niks.properties.AWSS3Properties;
import com.niks.properties.EnvironmentProperties;
import com.niks.services.Exception.S3FileDownloadFailed;
import com.niks.services.s3.S3OperationsHelperService;
import com.niks.utils.exceptions.AWSClientError;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MergeFilesSQSMessageProcessorTest {

  @InjectMocks
  MergeFilesSQSMessageProcessor mergeFilesSqsMessageProcessor;
  @Mock
  AWSS3Properties awss3Properties;
  @Mock
  EnvironmentProperties environmentProperties;
  @Mock
  FileReaderService fileReaderService;
  @Mock
  MArrayMergeService mArrayMergeService;
  @Mock
  FileWriterService fileWriterService;
  @Mock
  SQSOperationsHelperService sqsOperationsHelperService;
  @Mock
  S3OperationsHelperService s3OperationsHelperService;

  private static final String OUTPUT_FILE_NAME = "output.dat";
  private static final String PREFIX = "119D3831852F51/input/";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testProcessMessageValidMessageShouldSucceed()
      throws S3FileDownloadFailed, InvalidSQSMessage, AWSClientError, S3FileUploadFailed {

    ObjectListing objectListing = new ObjectListing();
    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String sqsMessageBody = new Gson().toJson(sqsMessagePayload);
    Optional<ObjectListing> objectListingOptional = Optional.of(objectListing);
    when(sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody)).thenReturn(sqsMessagePayload);
    when(s3OperationsHelperService
        .getBucketItems(sqsMessagePayload.getBucketName(), PREFIX)).thenReturn(objectListingOptional);
    mergeFilesSqsMessageProcessor.processMessage(sqsMessageBody);
    verify(sqsOperationsHelperService, times(1)).getMessageBodyObj(sqsMessageBody);
    verify(s3OperationsHelperService, times(1))
        .getBucketItems(sqsMessagePayload.getBucketName(), PREFIX);
  }

  @Test
  public void testProcessMessageShouldThrowS3FileDownloadFailedException()
      throws InvalidSQSMessage, AWSClientError {

    ObjectListing objectListing = new ObjectListing();
    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String sqsMessageBody = new Gson().toJson(sqsMessagePayload);
    Optional<ObjectListing> objectListingOptional = Optional.of(objectListing);
    when(sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody)).thenReturn(sqsMessagePayload);
    when(s3OperationsHelperService
        .getBucketItems(sqsMessagePayload.getBucketName(), PREFIX)).thenReturn(objectListingOptional);

    try {
      mergeFilesSqsMessageProcessor.processMessage(sqsMessageBody);
    } catch (Exception ex) {
      assertTrue(ex instanceof S3FileDownloadFailed);
    }
  }

  @Test
  public void testProcessMessageShouldThrowInvalidSQSMessageException()
      throws InvalidSQSMessage {
    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String sqsMessageBody = new Gson().toJson(sqsMessagePayload);
    when(sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody))
        .thenThrow(new InvalidSQSMessage(ErrorConstants.INVALID_SQS_MESSAGE));

    try {
      mergeFilesSqsMessageProcessor.processMessage(sqsMessageBody);
    } catch (Exception ex) {
      assertTrue(ex instanceof InvalidSQSMessage);
    }
  }

  @Test
  public void testProcessMessageShouldThrowS3FileUploadFailedException()
      throws InvalidSQSMessage, AWSClientError {

    ObjectListing objectListing = new ObjectListing();
    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String sqsMessageBody = new Gson().toJson(sqsMessagePayload);
    Optional<ObjectListing> objectListingOptional = Optional.of(objectListing);
    when(sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody)).thenReturn(sqsMessagePayload);
    when(s3OperationsHelperService
        .getBucketItems(sqsMessagePayload.getBucketName(), PREFIX)).thenReturn(objectListingOptional);

    try {
      mergeFilesSqsMessageProcessor.processMessage(sqsMessageBody);
    } catch (Exception ex) {
      assertTrue(ex instanceof S3FileUploadFailed);
    }

    verify(sqsOperationsHelperService, times(1)).getMessageBodyObj(sqsMessageBody);
    verify(s3OperationsHelperService, times(1))
        .getBucketItems(sqsMessagePayload.getBucketName(), PREFIX);
  }

  @Test
  public  void testDeleteMessage(){
    StringBuilder messageReceiptHandle = new StringBuilder();
    messageReceiptHandle.append("MbZj6wDWli%2BJvwwJaBV%2B3dcjk2YW2vA3%2BSTFFljT");
    messageReceiptHandle.append("M8tJJg6HRG6PYSasuWXPJB%2BCwLj1FjgXUv1uSj1gUPAWV66FU/WeR4mq2OKpEGY");
    messageReceiptHandle.append("WbnLmpRCJVAyeMjeU5ZBdtcQ%2BQEauMZc8ZRv37sIW2iJKq3M9MFx1YvV11A2x/KSbkJ0=");
    String queueUrl ="niks-merge-files-event";
    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String sqsMessageBody = new Gson().toJson(sqsMessagePayload);
    doNothing().when(sqsOperationsHelperService).deleteSQSMessage(messageReceiptHandle.toString(), queueUrl);
    when(environmentProperties.getProperty(PropertiesConstant.QUEUE_URL)).thenReturn(queueUrl);
    mergeFilesSqsMessageProcessor.deleteMessage(messageReceiptHandle.toString(),sqsMessageBody);
    verify(sqsOperationsHelperService,times(1)).deleteSQSMessage(messageReceiptHandle.toString(), queueUrl);
  }

  private SQSMessagePayload getSQSMessagePayload() {
    SQSMessagePayload sqsMessagePayload = new SQSMessagePayload();
    sqsMessagePayload.setBucketName("niks");
    sqsMessagePayload.setFolderName("input");
    sqsMessagePayload.setFolderPath("119D3831852F51/input/test.dat");
    sqsMessagePayload.setTenantId("119D3831852F51");
    return sqsMessagePayload;
  }
}
