package com.niks.services.file;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.niks.constants.EnvironmentConstants;
import com.niks.constants.ErrorConstants;
import com.niks.constants.HelperConstants;
import com.niks.model.SQSMessagePayload;
import com.niks.properties.AWSS3Properties;
import com.niks.properties.EnvironmentProperties;
import com.niks.services.Exception.S3FileDownloadFailed;
import com.niks.services.s3.S3OperationsHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileReaderService {

  @Autowired
  private EnvironmentProperties environmentProperties;
  @Autowired
  AWSS3Properties awss3Properties;
  @Autowired
  S3OperationsHelperService s3OperationsHelperService;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileReaderService.class);
  private static final SimpleDateFormat sdf = new SimpleDateFormat(HelperConstants.DATE_FORMAT);

  public Object[][] readFiles(final List<String> listOfS3FileToBeRead, final SQSMessagePayload sqsMessagePayload)
      throws S3FileDownloadFailed {

    final int threadPoolSize = Integer
        .parseInt(environmentProperties.getProperty(EnvironmentConstants.ENVIRONMENT_BATCH_SIZE));
    final int noOfFilesToProcess = listOfS3FileToBeRead.size();
    int noOfBatches = getNumberOfBatchesToProcess(noOfFilesToProcess, threadPoolSize);
    Object[][] arrayToMerge = new Object[noOfFilesToProcess][];

    for (int currentBatchNumber = 0; currentBatchNumber < noOfBatches; currentBatchNumber++) {

      int start = currentBatchNumber * threadPoolSize;
      int end = start + threadPoolSize;
      end = Math.min(end, noOfFilesToProcess);

      LOGGER.info(
          String.format("Reading of batch no %s has been started at %s", currentBatchNumber, sdf.format(new Date())));
      List<List<String>> receivedData = readFilesInBatchSize(listOfS3FileToBeRead, start, end, threadPoolSize,
          sqsMessagePayload);
      LOGGER.info(
          String.format("Reading of batch no %s has been completed at %s", currentBatchNumber, sdf.format(new Date())));

      for (int i = start, j = 0; i < end; i++, j++) {
        arrayToMerge[i] = (receivedData.get(j)).toArray();
      }
    }
    return arrayToMerge;
  }

  private int getNumberOfBatchesToProcess(final int noOfFilesToProcess, final int threadPoolSize) {
    return (int) (Math.ceil((double) noOfFilesToProcess / threadPoolSize));
  }

  private List<List<String>> readFilesInBatchSize(final List<String> listOfS3FileToBeRead,
      final int start, final int end,
      final int threadPoolSize, final SQSMessagePayload sqsMessagePayload)
      throws S3FileDownloadFailed {

    List<Future<List<String>>> result = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    List<AsyncFileReader> list = new ArrayList<>();

    for (int i = start; i < end; i++) {
      list.add(
          new AsyncFileReader(listOfS3FileToBeRead.get(i), sqsMessagePayload, s3OperationsHelperService));
    }

    //submitting task with callable
    for (int i = 0; i < list.size(); i++) {
      result.add(executorService.submit(list.get(i)));
    }
    try {
      List<List<String>> finalResult = new ArrayList<>();
      //calculating result
      for (int i = 0; i < list.size(); i++) {
        finalResult.add(result.get(i).get());
      }
      return finalResult;
    } catch (ExecutionException | InterruptedException ex) {
      LOGGER.error(String.format("Failed to download file from S3 for message body: %s", sqsMessagePayload.toString()));
      LOGGER.error(String.format("Exception: %s", ex.getMessage()));
      throw new S3FileDownloadFailed(ErrorConstants.S3_FILE_DOWNLOAD_FAILED);
    } finally {
      executorService.shutdown();
    }
  }
}
