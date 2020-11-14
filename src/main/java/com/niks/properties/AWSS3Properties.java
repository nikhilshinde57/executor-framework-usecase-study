package com.niks.properties;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.niks.constants.EnvironmentConstants;
import com.niks.constants.PropertiesConstant;
import javax.annotation.PostConstruct;
import lombok.ToString;
import com.niks.utils.EnvironmentVarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@ToString
public class AWSS3Properties {

  @Autowired
  EnvironmentProperties environmentProperties;

  private AmazonS3 connection;
  private static final Logger LOGGER = LoggerFactory.getLogger(AWSS3Properties.class);

  @PostConstruct
  private void init() {
    connection = createAndGetAwsS3Connection();
  }

  public AmazonS3Client getAwsS3Connection() {
    if (connection == null) {
      return createAndGetAwsS3Connection();
    }
    return (AmazonS3Client) connection;
  }

  //To work this AmazonS3Client with local AWS setup we are making this function as public
  //While downloading a file from S3 with TransferManager we need to give the AmazonS3Client to point to local or any env
  //After file download transferManager.shutdownNow() method get called and that will close this connection
  //So evey time while initializing TransferManager we need new AmazonS3Client connection so we make this methods as public
  public AmazonS3Client createAndGetAwsS3Connection() {
    if (getProfileFromEnvironmentVar().equals(EnvironmentConstants.LOCAL_ENVIRONMENT)) {
      final AwsClientBuilder.EndpointConfiguration endpoint =
          new AwsClientBuilder.EndpointConfiguration(environmentProperties.getProperty(PropertiesConstant.S3_END_POINT),
              environmentProperties.getProperty(PropertiesConstant.S3_AWS_REGION));
      return (AmazonS3Client) AmazonS3ClientBuilder.standard()
          .withPathStyleAccessEnabled(true)
          .withEndpointConfiguration(endpoint)
          .build();
    } else {
      return (AmazonS3Client) AmazonS3ClientBuilder.standard().build();
    }
  }

  private String getProfileFromEnvironmentVar() {
    final String profileName = EnvironmentVarUtils.getEnvironmentVar(EnvironmentConstants.ENVIRONMENT_VAR_NAME);

    if (profileName == null) {
      return EnvironmentConstants.LOCAL_ENVIRONMENT;
    }
    return profileName;
  }
}
