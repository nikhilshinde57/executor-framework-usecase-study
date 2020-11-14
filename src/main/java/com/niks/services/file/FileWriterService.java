package com.niks.services.file;

import java.util.Arrays;
import com.niks.model.SQSMessagePayload;
import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.s3.S3OperationsHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileWriterService {

  @Autowired
  S3OperationsHelperService s3OperationsHelperService;
  private static final Logger LOGGER = LoggerFactory.getLogger(FileWriterService.class);

  //TODO: We can also make this async to free our consumer main thread
  public void writeFile(final SQSMessagePayload sqsMessagePayload, final String fileName,
      final String[] sortedArrayToWrite) throws S3FileUploadFailed {
    try {
      s3OperationsHelperService
          .multipartUploadToS3(sqsMessagePayload, fileName, Arrays.asList(sortedArrayToWrite));
    } catch (S3FileUploadFailed ex) {
      //Write file to temp local location and retry after some time to upload
      throw ex;
    }
  }
}
