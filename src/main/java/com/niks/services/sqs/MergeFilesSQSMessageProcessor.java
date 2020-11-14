package com.niks.services.sqs;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.niks.services.file.FileWriterService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.niks.constants.HelperConstants;
import com.niks.constants.PropertiesConstant;
import com.niks.model.SQSMessagePayload;
import com.niks.properties.EnvironmentProperties;
import com.niks.services.Exception.S3FileDownloadFailed;
import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.MessageProcessor;
import com.niks.services.file.FileReaderService;
import com.niks.services.merge.MArrayMergeService;
import com.niks.services.s3.S3OperationsHelperService;
import com.niks.utils.exceptions.AWSClientError;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MergeFilesSQSMessageProcessor implements MessageProcessor {

  @Autowired
  EnvironmentProperties environmentProperties;
  @Autowired
  FileReaderService fileReaderService;
  @Autowired
  MArrayMergeService mArrayMergeService;
  @Autowired
  FileWriterService fileWriterService;
  @Autowired
  SQSOperationsHelperService sqsOperationsHelperService;
  @Autowired
  S3OperationsHelperService s3OperationsHelperService;

  private static final Logger LOGGER = LoggerFactory.getLogger(MergeFilesSQSMessageProcessor.class);
  private static final SimpleDateFormat sdf = new SimpleDateFormat(HelperConstants.DATE_FORMAT);

  @Override
  public void processMessage(final String sqsMessageBody)
      throws InvalidSQSMessage, AWSClientError, S3FileDownloadFailed, S3FileUploadFailed {

    SQSMessagePayload sqsMessagePayload = sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody);

    Optional<ObjectListing> objectListing = s3OperationsHelperService
        .getBucketItems(sqsMessagePayload.getBucketName(), getPrefix(sqsMessagePayload));

    if (objectListing.isPresent()) {
      List<String> listOfFilePresentInTheDirectory = getListOfFilePresentInTheDirectory(objectListing.get());

      if (!listOfFilePresentInTheDirectory.isEmpty()) {
        Object[][] arrayToMerge = fileReaderService.readFiles(listOfFilePresentInTheDirectory, sqsMessagePayload);
        String[] sortedArrayToWrite = mArrayMergeService
            .mergeMSortedArray(arrayToMerge, arrayToMerge.length, sqsMessageBody);
        fileWriterService.writeFile(sqsMessagePayload, getOutPutFileName(), sortedArrayToWrite);
      }
    }
    LOGGER.info(String.format("Message successfully processed: %s ", sqsMessageBody));
  }

  public void deleteMessage(final String messageReceiptHandle, final String sqsMessageBody) {
    try {
      sqsOperationsHelperService
          .deleteSQSMessage(messageReceiptHandle, environmentProperties.getProperty(PropertiesConstant.QUEUE_URL));
      LOGGER.info(String.format("Message successfully deleted: %s ", sqsMessageBody));
    } catch (Exception ex) {
      LOGGER.error(String.format("Error occurred while deleting message: %s", sqsMessageBody), ex);
    }
  }

  private String getPrefix(final SQSMessagePayload sqsMessagePayload) {
    return sqsMessagePayload.getTenantId() + HelperConstants.S3_DELIMITER + sqsMessagePayload.getFolderName()
        + HelperConstants.S3_DELIMITER;
  }

  private List<String> getListOfFilePresentInTheDirectory(final ObjectListing objectListing) {
    List<String> listOfFilePresentInTheDirectory = new ArrayList<>();
    for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
      Optional<String> fileName = getFileNameFromPath(summary.getKey());
      fileName.ifPresent(listOfFilePresentInTheDirectory::add);
    }
    LOGGER.info(String.format("%s files present in the directory for current processing message",
        listOfFilePresentInTheDirectory.size()));
    return listOfFilePresentInTheDirectory;
  }

  private Optional<String> getFileNameFromPath(final String filePath) {
    String[] temp = filePath.split(HelperConstants.S3_FILE_NAME_DELIMITER);
    if (temp.length > 1) {
      return Optional.of(temp[1]);
    } else {
      Optional.empty();
    }
    return Optional.empty();
  }

  private String getOutPutFileName() {
    return HelperConstants.OUT_PUT_FILE_NAME + sdf.format(new Date()) + HelperConstants.OUT_PUT_FILE_FORMAT;
  }
}
