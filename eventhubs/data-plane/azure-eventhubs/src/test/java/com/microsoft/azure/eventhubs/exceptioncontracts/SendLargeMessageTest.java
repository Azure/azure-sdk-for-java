// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.Locale;

public class SendLargeMessageTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";

    private static EventHubClient ehClient;
    private static PartitionSender sender;

    private static EventHubClient receiverHub;
    private static PartitionReceiver receiver;

    @BeforeClass
    public static void initialize() throws Exception {
        initializeEventHubClients(TestContext.getConnectionString());
    }

    public static void initializeEventHubClients(ConnectionStringBuilder connStr) throws Exception {
        ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString(), TestContext.EXECUTOR_SERVICE);
        sender = ehClient.createPartitionSender(PARTITION_ID).get();

        receiverHub = EventHubClient.createFromConnectionStringSync(connStr.toString(), TestContext.EXECUTOR_SERVICE);
        receiver = receiverHub.createReceiver(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now())).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        if (receiver != null) {
            receiver.closeSync();
        }

        if (receiverHub != null) {
            receiverHub.closeSync();
        }

        if (sender != null) {
            sender.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test()
    public void sendMsgLargerThan64k() throws EventHubException {
        this.sendLargeMessageTest(100 * 1024);
    }

    @Test(expected = PayloadSizeExceededException.class)
    public void sendMsgLargerThan1024K() throws EventHubException {
        int msgSize = 1024 * 1024 * 2;
        byte[] body = new byte[msgSize];
        for (int i = 0; i < msgSize; i++) {
            body[i] = 1;
        }

        EventData largeMsg = EventData.create(body);
        sender.sendSync(largeMsg);
    }

    @Test()
    public void sendMsgLargerThan128k() throws EventHubException {
        this.sendLargeMessageTest(129 * 1024);
    }

    private void sendLargeMessageTest(int msgSize) throws EventHubException {
        byte[] body = new byte[msgSize];
        for (int i = 0; i < msgSize; i++) {
            body[i] = 1;
        }

        EventData largeMsg = EventData.create(body);
        sender.sendSync(largeMsg);

        Iterable<EventData> messages = receiver.receiveSync(100);
        Assert.assertTrue(messages != null && messages.iterator().hasNext());

        EventData recdMessage = messages.iterator().next();

        Assert.assertEquals(String.format(Locale.US, "sent msg size: %s, recvd msg size: %s", msgSize, recdMessage.getBytes().length), recdMessage.getBytes().length, msgSize);
    }
}
