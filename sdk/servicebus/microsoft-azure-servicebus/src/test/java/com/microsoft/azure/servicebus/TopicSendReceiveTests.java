// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class TopicSendReceiveTests extends SendReceiveTests {

    @Override
    public String getEntityNamePrefix() {
        return "TopicSendReceiveTests";
    }

    @Override
    public boolean isEntityQueue() {
        return false;
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }
    
    @Test
    public void testPeekMessageOnTopic() throws InterruptedException, ServiceBusException {
    	TopicClient topicClient = new TopicClient(this.factory, this.entityName);
    	Message message = new Message("AMQP Scheduled message");
    	message.setScheduledEnqueueTimeUtc(Instant.now().plusSeconds(60 * 60));
    	topicClient.send(message);
        message = new Message("AMQP Scheduled message2");
        message.setScheduledEnqueueTimeUtc(Instant.now().plusSeconds(60 * 60));
        topicClient.send(message);
        
        IMessage peekedMessage1 = topicClient.peek();
        long firstMessageSequenceNumber = peekedMessage1.getSequenceNumber();
        IMessage peekedMessage2 = topicClient.peek();
        Assert.assertNotEquals("Peek returned the same message again.", firstMessageSequenceNumber, peekedMessage2.getSequenceNumber());

        // Now peek with fromSequnceNumber.. May not work for partitioned entities
        IMessage peekedMessage5 = topicClient.peek(firstMessageSequenceNumber);
        Assert.assertEquals("Peek with sequence number failed.", firstMessageSequenceNumber, peekedMessage5.getSequenceNumber());
    }
}
