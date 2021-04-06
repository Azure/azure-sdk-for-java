package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;

import java.security.Provider;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * ServiceBusReceivedMessage constructor is invisible outside its package, hence this TestBuilder helps construct instances for test module use.
 */
public class TestBuilder {
    private ServiceBusReceivedMessage inboundMessage;
    public TestBuilder(BinaryData body){
        this.inboundMessage = new ServiceBusReceivedMessage(body);

    }


   public TestBuilder  setContentType( String contentType){
       inboundMessage.setContentType(contentType);
       return this;

   }

   public TestBuilder  setReplyTo(String replyTo){
        inboundMessage.setReplyTo(replyTo);
        return this;
   }


   public TestBuilder setMessageID(String messageId){
        inboundMessage.setMessageId(messageId);
        return this;
   }
    public TestBuilder setTimeToLive(Duration  timeToLive){
        inboundMessage.setTimeToLive(timeToLive);
        return this;
    }

    public TestBuilder setScheduledEnqueueTime(OffsetDateTime scheduledEnqueueTime){
        inboundMessage.setScheduledEnqueueTime(scheduledEnqueueTime);
        return this;
    }
    public TestBuilder setSessionId(String sessionId){
        inboundMessage.setSessionId(sessionId);
        return this;
    }
    public TestBuilder setCorrelationId(String correlationId){
        inboundMessage.setSessionId(correlationId);
        return this;
    }

    public TestBuilder setTo(String to){
        inboundMessage.setTo(to);
        return this;
    }

    public TestBuilder setReplyToSessionId(String replyToSessionId){
        inboundMessage.setReplyToSessionId(replyToSessionId);
        return this;
    }
    public TestBuilder setPartitionKey(String partitionKey){
        inboundMessage.setPartitionKey(partitionKey);
        return this;
    }


    public ServiceBusReceivedMessage build(){
        return inboundMessage;
    }

}
