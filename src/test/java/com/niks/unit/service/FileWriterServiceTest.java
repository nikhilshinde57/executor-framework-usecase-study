package com.niks.unit.service;

import com.niks.services.Exception.S3FileUploadFailed;
import com.niks.services.file.FileWriterService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.niks.constants.ErrorConstants;
import com.niks.model.SQSMessagePayload;
import com.niks.services.s3.S3OperationsHelperService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterServiceTest {

  @InjectMocks
  FileWriterService fileWriterService;

  @Mock
  S3OperationsHelperService s3OperationsHelperService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testWriteFileShouldSucceed() throws IOException, S3FileUploadFailed {

    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String fileName = "test.dat", dataToWrite = "SampleData";
    List list = new ArrayList<String>();
    list.add(dataToWrite);
    String[] sortedArrayToWrite = new String[]{"SampleData"};
    doNothing().when(s3OperationsHelperService)
        .multipartUploadToS3(sqsMessagePayload, fileName, list);
    fileWriterService.writeFile(sqsMessagePayload, fileName, sortedArrayToWrite);
    verify(s3OperationsHelperService, times(1))
        .multipartUploadToS3(sqsMessagePayload, fileName, list);
  }

  @Test
  public void testWriteFileShouldThrowS3FileUploadFailedException() throws IOException, S3FileUploadFailed {

    SQSMessagePayload sqsMessagePayload = getSQSMessagePayload();
    String fileName = "test.dat", dataToWrite = "SampleData";
    List list = new ArrayList<String>();
    list.add(dataToWrite);
    String[] sortedArrayToWrite = new String[]{"SampleData"};

    doThrow(new S3FileUploadFailed(ErrorConstants.S3_FILE_UPLOAD_FAILED)).when(s3OperationsHelperService)
        .multipartUploadToS3(sqsMessagePayload, fileName, list);
    try {
      fileWriterService.writeFile(sqsMessagePayload, fileName, sortedArrayToWrite);
    } catch (Exception ex) {
      assertTrue(ex instanceof S3FileUploadFailed);
    }
    verify(s3OperationsHelperService, times(1))
        .multipartUploadToS3(sqsMessagePayload, fileName, list);
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
