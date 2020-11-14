package com.niks.unit.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.niks.services.sqs.SQSOperationsHelperService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.niks.model.SQSMessagePayload;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SQSOperationsHelperServiceTest {

  @InjectMocks
  SQSOperationsHelperService sqsOperationsHelperService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetMessageBodyObjShouldReturnPayloadObject() throws InvalidSQSMessage {
    String sqsMessageBody = "{"
        + "\"bucketName\":\"niks\",\"folderName\":\"input\",\"folderPath\":\"119D3831852F51/input/test.dat\",\"tenantId\":\"119D3831852F51\"}";

    SQSMessagePayload sqsMessagePayload = sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody);
    assertEquals("niks", sqsMessagePayload.getBucketName());
    assertEquals("input", sqsMessagePayload.getFolderName());
    assertEquals("119D3831852F51/input/test.dat", sqsMessagePayload.getFolderPath());
    assertEquals("119D3831852F51", sqsMessagePayload.getTenantId());

  }

  @Test
  public void testGetMessageBodyObjShouldThrowInvalidSQSMessageException() throws InvalidSQSMessage {
    String sqsMessageBody = "{"
        + "\"bucketName\":,\"folderName\":\"input\",\"folderPath\":\"119D3831852F51/input/test.dat\",\"tenantId\":\"119D3831852F51\"}";

    try {
      SQSMessagePayload sqsMessagePayload = sqsOperationsHelperService.getMessageBodyObj(sqsMessageBody);
    } catch (Exception ex) {
      assertTrue(ex instanceof InvalidSQSMessage);
    }
  }
}
