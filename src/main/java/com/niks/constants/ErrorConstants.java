package com.niks.constants;

public class ErrorConstants {

  public static final String INVALID_SQS_MESSAGE = "Invalid SQS message received, unable to parse it";
  public static final String S3_FILE_DOWNLOAD_FAILED = "Failed to download file from S3";
  public static final String S3_FILE_UPLOAD_FAILED = "Failed to upload file on S3";

  public static final String ENV_PROP_LOADING_FAILED = "Failed to load environment properties";
  public static final String ENV_LOCAL_CONFIG_ACCESS_DENIED = "Cannot access environment config from local, aborting";
  public static final String ENV_PROP_FILE_EMPTY = "Failed to load environment properties due to empty file";
  public static final String ENV_K8S_CONFIG_ACCESS_DENIED = "Cannot access environment config from kubernetes, aborting";
  public static final String ENV_PROPERTY_NOT_FOUND = "Environment file does not contain requested property ";
}
