package com.niks.services.sqs;

import com.niks.enums.SQSEventType;
import com.niks.services.MessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SQSMessageProcessorFactory {

  @Autowired
  MergeFilesSQSMessageProcessor mergeFilesSQSMessageProcessor;

  public MessageProcessor getMessageProcessor(final SQSEventType sqsEventType){

    switch (sqsEventType){
      case MERGE_SORTED_FILES:
        return mergeFilesSQSMessageProcessor;
      default:
        return mergeFilesSQSMessageProcessor;
    }
  }
}
