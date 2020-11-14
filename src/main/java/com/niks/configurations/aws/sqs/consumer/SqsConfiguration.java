package com.niks.configurations.aws.sqs.consumer;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.niks.constants.PropertiesConstant;
import com.niks.properties.EnvironmentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SqsConfiguration {

  @Autowired
  EnvironmentProperties environmentProperties;

  protected AsyncTaskExecutor createDefaultTaskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(
        Integer.parseInt(environmentProperties.getProperty(PropertiesConstant.SQS_CORE_POOL_SIZE)));
    threadPoolTaskExecutor.setMaxPoolSize(
        Integer.parseInt(environmentProperties.getProperty(PropertiesConstant.SQS_MAX_POOL_SIZE)));
    threadPoolTaskExecutor.setQueueCapacity(
        Integer.parseInt(environmentProperties.getProperty(PropertiesConstant.SQS_QUEUE_CAPACITY)));
    threadPoolTaskExecutor.afterPropertiesSet();
    return threadPoolTaskExecutor;
  }

  @Bean
  public AmazonSQSAsync amazonSqs() {
    return AmazonSQSAsyncClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder
            .EndpointConfiguration(environmentProperties.getProperty(PropertiesConstant.SQS_END_POINT),
                environmentProperties.getProperty(PropertiesConstant.SQS_AWS_REGION))).build();
  }

  @Bean
  public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(final AmazonSQSAsync amazonSqs) {
    SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
    factory.setWaitTimeOut(Integer
        .parseInt(environmentProperties.getProperty(PropertiesConstant.SQS_LISTENER_WAIT_TIMEOUT)));
    factory.setTaskExecutor(createDefaultTaskExecutor());

    return factory;
  }
}
