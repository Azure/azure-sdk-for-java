/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.eventdata;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.LinkedList;
import java.util.List;

import com.microsoft.azure.eventhubs.*;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.message.Message;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.EventHubException;

public class InteropEventBodyTest extends ApiTestBase {
    
    static EventHubClient ehClient;
    static MessagingFactory msgFactory;
    static PartitionReceiver receiver;
    static MessageSender partitionMsgSender;
    static PartitionSender partitionSender;

    static final String partitionId = "0";
    static EventData receivedEvent;
    static EventData reSentAndReceivedEvent;
    static Message reSendAndReceivedMessage;

    @BeforeClass
    public static void initialize() throws EventHubException, IOException, InterruptedException, ExecutionException
    {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        final String connectionString = connStrBuilder.toString();

        ehClient = EventHubClient.createFromConnectionStringSync(connectionString, TestContext.EXECUTOR_SERVICE);
        msgFactory = MessagingFactory.createFromConnectionString(connectionString, TestContext.EXECUTOR_SERVICE).get();
        receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), partitionId, EventPosition.fromEnqueuedTime(Instant.now()));
        partitionSender = ehClient.createPartitionSenderSync(partitionId);
        partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEventHubName() + "/partitions/" + partitionId).get();
        
        // run out of messages in that specific partition - to account for clock-skew with Instant.now() on test machine vs eventhubs service
        receiver.setReceiveTimeout(Duration.ofSeconds(5));
        Iterable<EventData> clockSkewEvents;
        do {
            clockSkewEvents = receiver.receiveSync(100);
        } while (clockSkewEvents != null && clockSkewEvents.iterator().hasNext());
    }

    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpValue() throws EventHubException, InterruptedException, ExecutionException
    {
        Message originalMessage = Proton.message();
        String payload = "testmsg";
        originalMessage.setBody(new AmqpValue(payload));
        partitionMsgSender.send(originalMessage).get();
        receivedEvent = receiver.receiveSync(10).iterator().next();
        
        Assert.assertEquals(payload, receivedEvent.getObject());
        Assert.assertEquals(receivedEvent.getBytes(), null);
        
        partitionSender.sendSync(receivedEvent);
        reSentAndReceivedEvent = receiver.receiveSync(10).iterator().next();
        Assert.assertEquals(payload, reSentAndReceivedEvent.getObject());
        Assert.assertEquals(reSentAndReceivedEvent.getBytes(), null);
    }
    
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpSequence() throws EventHubException, InterruptedException, ExecutionException
    {
        Message originalMessage = Proton.message();
        String payload = "testmsg";
        LinkedList<Data> datas = new LinkedList<>();
        datas.add(new Data(new Binary(payload.getBytes())));
        originalMessage.setBody(new AmqpSequence(datas));
        
        partitionMsgSender.send(originalMessage).get();
        receivedEvent = receiver.receiveSync(10).iterator().next();
        
        Assert.assertEquals(payload, new String(((List<Data>) receivedEvent.getObject()).get(0).getValue().getArray()));
        Assert.assertEquals(receivedEvent.getBytes(), null);
        
        partitionSender.sendSync(receivedEvent);
        reSentAndReceivedEvent = receiver.receiveSync(10).iterator().next();
        Assert.assertEquals(payload, new String(((List<Data>) reSentAndReceivedEvent.getObject()).get(0).getValue().getArray()));
        Assert.assertArrayEquals(reSentAndReceivedEvent.getBytes(), null);
    }
    
    @AfterClass
    public static void cleanup() throws EventHubException
    {
        if (partitionMsgSender != null)
                partitionMsgSender.closeSync();

        if (receiver != null)
                receiver.closeSync();

        if (ehClient != null)
                ehClient.closeSync();

        if (msgFactory != null)
                msgFactory.closeSync();
    }
}
