package com.niks.properties;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
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
public class AWSSQSProperties {

  @Autowired
  EnvironmentProperties environmentProperties;
  private AmazonSQSClient amazonSQSClient;
  private String secureOriginalBucketName;
  private static final Logger LOGGER = LoggerFactory.getLogger(AWSS3Properties.class);

  @PostConstruct
  private void init() {
    amazonSQSClient = getAwsSQSClient();
  }

  private AmazonSQSClient getAwsSQSClient() {
    if (getProfileFromEnvironmentVar().equals(EnvironmentConstants.LOCAL_ENVIRONMENT)) {
      final AwsClientBuilder.EndpointConfiguration endpoint =
          new AwsClientBuilder.EndpointConfiguration(
              environmentProperties.getProperty(PropertiesConstant.SQS_END_POINT),
              environmentProperties.getProperty(PropertiesConstant.SQS_AWS_REGION));

      return (AmazonSQSClient) AmazonSQSClientBuilder.standard()
          .withEndpointConfiguration(endpoint)
          .build();
    } else {
      return (AmazonSQSClient) AmazonSQSClientBuilder.standard().build();
    }
  }

  public AmazonSQSClient getAwsSQSClientConnection() {
    return amazonSQSClient;
  }

  private String getProfileFromEnvironmentVar() {
    final String profileName = EnvironmentVarUtils.getEnvironmentVar(EnvironmentConstants.ENVIRONMENT_VAR_NAME);

    if (profileName == null) {
      return EnvironmentConstants.LOCAL_ENVIRONMENT;
    }
    return profileName;
  }
}
