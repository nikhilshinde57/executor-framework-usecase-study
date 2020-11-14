package com.niks.model;

import lombok.Data;

@Data
public class SQSMessagePayload
{
  private String bucketName;
  private String folderName;
  private String folderPath;
  private String tenantId;
}