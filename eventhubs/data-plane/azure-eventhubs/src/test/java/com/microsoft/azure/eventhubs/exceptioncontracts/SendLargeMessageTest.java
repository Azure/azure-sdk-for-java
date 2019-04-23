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

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class SendLargeMessageTest extends ApiTestBase {
    static String partitionId = "0";

    static EventHubClient ehClient;
    static PartitionSender sender;

    static EventHubClient receiverHub;
    static PartitionReceiver receiver;

    @BeforeClass
    public static void initialize() throws Exception {
        initializeEventHubClients(TestContext.getConnectionString());
    }

    public static void initializeEventHubClients(ConnectionStringBuilder connStr) throws Exception {

        ehClient = EventHubClient.createSync(connStr.toString(), TestContext.EXECUTOR_SERVICE);
        sender = ehClient.createPartitionSender(partitionId).get();

        receiverHub = EventHubClient.createSync(connStr.toString(), TestContext.EXECUTOR_SERVICE);
        receiver = receiverHub.createReceiver(TestContext.getConsumerGroupName(), partitionId, EventPosition.fromEnqueuedTime(Instant.now())).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        if (receiverHub != null) {
            receiverHub.close();
        }

        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Test()
    public void sendMsgLargerThan64k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        this.sendLargeMessageTest(100 * 1024);
    }

    @Test(expected = PayloadSizeExceededException.class)
    public void sendMsgLargerThan1024K() throws EventHubException, InterruptedException, ExecutionException, IOException {
        int msgSize = 1024 * 1024 * 2;
        byte[] body = new byte[msgSize];
        for (int i = 0; i < msgSize; i++) {
            body[i] = 1;
        }

        EventData largeMsg = EventData.create(body);
        sender.sendSync(largeMsg);
    }

    @Test()
    public void sendMsgLargerThan128k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        this.sendLargeMessageTest(129 * 1024);
    }

    public void sendLargeMessageTest(int msgSize) throws InterruptedException, ExecutionException, EventHubException {
        byte[] body = new byte[msgSize];
        for (int i = 0; i < msgSize; i++) {
            body[i] = 1;
        }

        EventData largeMsg = EventData.create(body);
        sender.sendSync(largeMsg);

        Iterable<EventData> messages = receiver.receiveSync(100);
        Assert.assertTrue(messages != null && messages.iterator().hasNext());

        EventData recdMessage = messages.iterator().next();

        Assert.assertTrue(
                String.format("sent msg size: %s, recvd msg size: %s", msgSize, recdMessage.getBytes().length),
                recdMessage.getBytes().length == msgSize);
    }
}
