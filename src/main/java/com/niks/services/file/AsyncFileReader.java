package com.niks.services.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import com.niks.model.SQSMessagePayload;
import com.niks.services.s3.S3OperationsHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncFileReader implements Callable<List<String>> {

  private S3OperationsHelperService s3OperationsHelperService;
  private String fileName;
  private SQSMessagePayload sqsMessagePayload;
  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFileReader.class);

  public AsyncFileReader(String fileName, SQSMessagePayload sqsMessagePayload, S3OperationsHelperService s3OperationsHelperService) {
    this.fileName = fileName;
    this.sqsMessagePayload = sqsMessagePayload;
    this.s3OperationsHelperService = s3OperationsHelperService;
  }

  @Override
  public List<String> call() throws IOException {

    Integer g= null;
    List<String> wordList = new ArrayList<>();
    Stream<String> stream = s3OperationsHelperService
        .downloadMultipartFileFromS3(this.sqsMessagePayload, this.fileName);
    stream.forEach(
        line -> {
          wordList.add(line.trim());
        }
    );

    LOGGER.debug(
        String.format("Async file reading completed for fileName %s and tenantId %s", this.fileName, this.sqsMessagePayload));
    return wordList;
  }
}
