package com.niks.services.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.niks.services.Exception.S3FileUploadFailed;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.niks.constants.EnvironmentConstants;
import com.niks.constants.ErrorConstants;
import com.niks.constants.HelperConstants;
import com.niks.constants.PropertiesConstant;
import com.niks.model.SQSMessagePayload;
import com.niks.properties.AWSS3Properties;
import com.niks.properties.EnvironmentProperties;
import com.niks.utils.exceptions.AWSClientError;
import com.niks.utils.exceptions.InvalidSQSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class S3OperationsHelperService {

  @Autowired
  AWSS3Properties awss3Properties;
  @Autowired
  EnvironmentProperties environmentProperties;

  private final static long PART_SIZE = 50 * 1024 * 1024;
  private static final SimpleDateFormat sdf = new SimpleDateFormat(HelperConstants.DATE_FORMAT);
  private static final Logger LOGGER = LoggerFactory.getLogger(S3OperationsHelperService.class);

  public Optional<ObjectListing> getBucketItems(final String bucketName, final String prefix)
      throws InvalidSQSMessage, AWSClientError {
    try {
      ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
          .withBucketName(bucketName)
          .withPrefix(prefix);
      // Default keys returned by s3 are 1000 so we can loop over this if we have max keys
      //.withMaxKeys(1000);
      ObjectListing objectListing = awss3Properties.getAwsS3Connection().listObjects(listObjectsRequest);
      return Optional.of(objectListing);
    } catch (AmazonServiceException ase) {
      //Debug logs help us to trace an error
      LOGGER.debug("Caught an AmazonServiceException, " +
          "which means your request made it " +
          "to Amazon S3, but was rejected with an error response " +
          "for some reason.");
      LOGGER.debug("HTTP Status Code: " + ase.getStatusCode());
      LOGGER.debug("AWS Error Code:   " + ase.getErrorCode());
      LOGGER.debug("Error Type:       " + ase.getErrorType());
      LOGGER.debug("Request ID:       " + ase.getRequestId());
      LOGGER.error("Failed to fetch bucket items with exception: " + ase.getMessage());
      throw new InvalidSQSMessage("Invalid bucket name or prefix.");
    } catch (AmazonClientException ace) {
      LOGGER.debug("Caught an AmazonClientException, " +
          "which means the client encountered " +
          "an internal error while trying to communicate" +
          " with S3, " +
          "such as not being able to access the network.");
      LOGGER.debug("Error Message: " + ace.getMessage());
      LOGGER.error("Failed to fetch bucket items with exception: " + ace.getMessage());
      throw new AWSClientError("Something went wrong with S3 client");
    }
  }

  public Stream<String> downloadMultipartFileFromS3(SQSMessagePayload sqsMessagePayload, final String fileName)
      throws IOException {
    final String localFolderPath = getLocalFolderName(sqsMessagePayload.getTenantId());
    final String localFilePath = localFolderPath + fileName;
    final String s3FolderPath = getS3FolderName(sqsMessagePayload.getTenantId(),
        HelperConstants.PROCESSING_FOLDER_NAME);
    final String s3FilePath = s3FolderPath + fileName;
    final File newFile = new File(localFilePath);
    final File newFolder = new File(localFolderPath);
    newFolder.mkdirs();
    newFile.deleteOnExit();
    newFile.createNewFile();

    LOGGER.info("Initiated download request from S3: {}");
    //By providing explicit AmazonS3Client connection this will help to point
    // the local AWS env while running on local machine
    final TransferManager transferManager = new TransferManager(awss3Properties.getAwsS3Connection(),
        Executors.newFixedThreadPool(Integer
            .parseInt(environmentProperties.getProperty(EnvironmentConstants.TRANSFER_MANAGER_THREAD_POOL_SIZE))));
    final GetObjectRequest getObjectRequest = new GetObjectRequest(sqsMessagePayload.getBucketName(), s3FilePath);
    final Download download = transferManager.download(getObjectRequest, newFile);

    try {
      download.waitForCompletion();
    } catch (InterruptedException e) {
      LOGGER.error("Unable to perform S3 download. Aborting operation!");
      throw new IOException(e);
    } finally {
      //transferManager.shutdownNow();
    }
    Stream<String> stringStream = Files.lines(Paths.get(localFilePath));
    newFile.delete();
    return stringStream;
  }

  public void multipartUploadToS3(SQSMessagePayload sqsMessagePayload, final String fileName,
      final List<String> dataToWrite)
      throws S3FileUploadFailed {

    final String s3FilePath = getS3FolderName(sqsMessagePayload.getTenantId(), "output")
        + fileName;
    LOGGER.info("tenantId: {}, fileName: {}, bucketName: {}, s3FilePath: {}",
        sqsMessagePayload.getTenantId(), fileName, sqsMessagePayload.getBucketName(), s3FilePath);
    byte[] content = dataToWrite.stream().collect(Collectors.joining("\n")).getBytes();
    InputStream inputStreamToWrite = new ByteArrayInputStream(content);
    long contentLength = content.length;
    doMultipartUpload(inputStreamToWrite, sqsMessagePayload.getBucketName(), s3FilePath, contentLength,
        awss3Properties);
  }

  private String getLocalFolderName(final String tenantId) {
    return environmentProperties.getProperty(PropertiesConstant.LOCAL_STORAGE_PATH)
        + tenantId
        + HelperConstants.S3_DELIMITER + HelperConstants.PROCESSING_FOLDER_NAME + HelperConstants.S3_DELIMITER;
  }

  private String getS3FolderName(final String tenantId, final String processingFolderName) {
    return tenantId + HelperConstants.S3_DELIMITER + processingFolderName + HelperConstants.S3_DELIMITER;
  }

  private void doMultipartUpload(final InputStream inputStream, final String bucketName,
      final String s3FilePath,
      final long contentLength, final AWSS3Properties awss3Properties) throws S3FileUploadFailed {
    try {
      // Request server-side encryption (non-KMS)
      final ObjectMetadata objectMetadata = getObjectMetadata(contentLength);
      final InitiateMultipartUploadRequest initiateMultipartUploadRequest =
          new InitiateMultipartUploadRequest(bucketName, s3FilePath, objectMetadata);
      final InitiateMultipartUploadResult uploadResult = awss3Properties.getAwsS3Connection()
          .initiateMultipartUpload(initiateMultipartUploadRequest);
      LOGGER.info(String.format("Initiated S3 multipart upload request at %s", sdf.format(new Date())));

      final List<PartETag> partETags = new ArrayList<>();
      long partSize = PART_SIZE, filePosition = 0;

      for (int i = 1; filePosition < contentLength; i++) {
        partSize = Math.min(partSize, (contentLength - filePosition));
        final UploadPartRequest uploadPartRequest = new UploadPartRequest()
            .withBucketName(bucketName)
            .withUploadId(uploadResult.getUploadId())
            .withPartNumber(i)
            .withKey(s3FilePath)
            .withInputStream(inputStream)
            .withPartSize(partSize);
        final UploadPartResult uploadPartResult = awss3Properties.getAwsS3Connection().uploadPart(uploadPartRequest);
        partETags.add(uploadPartResult.getPartETag());
        filePosition += partSize;
        LOGGER.debug("i: {}", i);
      }

      LOGGER.info(String.format("Finished multipart S3 upload at %s", sdf.format(new Date())));
      final CompleteMultipartUploadRequest completeMultipartUpload = new CompleteMultipartUploadRequest(
          bucketName, s3FilePath, uploadResult.getUploadId(), partETags);
      awss3Properties.getAwsS3Connection().completeMultipartUpload(completeMultipartUpload);
    } catch (SdkClientException ex) {
      LOGGER.error(ErrorConstants.S3_FILE_UPLOAD_FAILED);
      throw new S3FileUploadFailed(ErrorConstants.S3_FILE_UPLOAD_FAILED);
    }
  }

  private ObjectMetadata getObjectMetadata(final long contentLength) {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
    objectMetadata.setContentLength(contentLength);
    return objectMetadata;
  }
}
