// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.MessageSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class InteropEventBodyTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";

    private static EventHubClient ehClient;
    private static MessagingFactory msgFactory;
    private static PartitionReceiver receiver;
    private static MessageSender partitionMsgSender;
    private static PartitionSender partitionSender;
    private static EventData receivedEvent;
    private static EventData reSentAndReceivedEvent;

    @BeforeClass
    public static void initialize() throws EventHubException, IOException, InterruptedException, ExecutionException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        final String connectionString = connStrBuilder.toString();

        ehClient = EventHubClient.createFromConnectionStringSync(connectionString, TestContext.EXECUTOR_SERVICE);
        msgFactory = MessagingFactory.createFromConnectionString(connectionString, TestContext.EXECUTOR_SERVICE).get();
        receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
        partitionSender = ehClient.createPartitionSenderSync(PARTITION_ID);
        partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEventHubName() + "/partitions/" + PARTITION_ID).get();

        // run out of messages in that specific partition - to account for clock-skew with Instant.now() on test machine vs eventhubs service
        receiver.setReceiveTimeout(Duration.ofSeconds(5));
        Iterable<EventData> clockSkewEvents;
        do {
            clockSkewEvents = receiver.receiveSync(100);
        } while (clockSkewEvents != null && clockSkewEvents.iterator().hasNext());
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (partitionMsgSender != null) {
            partitionMsgSender.closeSync();
        }

        if (partitionSender != null) {
            partitionSender.closeSync();
        }

        if (receiver != null) {
            receiver.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }

        if (msgFactory != null) {
            msgFactory.closeSync();
        }
    }

    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpValue() throws EventHubException, InterruptedException, ExecutionException {
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
        Assert.assertNull(reSentAndReceivedEvent.getBytes());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interopWithProtonAmqpMessageBodyAsAmqpSequence() throws EventHubException, InterruptedException, ExecutionException {
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
}
