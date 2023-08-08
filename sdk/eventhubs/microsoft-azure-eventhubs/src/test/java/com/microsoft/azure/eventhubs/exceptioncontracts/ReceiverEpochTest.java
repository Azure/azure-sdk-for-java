// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverDisconnectedException;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ReceiverEpochTest extends ApiTestBase {
    private static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();
    private static final String PARTITION_ID = "0";

    private static EventHubClient ehClient;

    private PartitionReceiver receiver;

    @BeforeClass
    public static void initializeEventHub() throws EventHubException, IOException {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test(expected = ReceiverDisconnectedException.class)
    public void testEpochReceiverWins() throws EventHubException, InterruptedException, ExecutionException {
        int sendEventCount = 5;

        PartitionReceiver receiverLowEpoch = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));

        try {
            receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
            TestBase.pushEventsToPartition(ehClient, PARTITION_ID, sendEventCount).get();

            receiverLowEpoch.receiveSync(20);
            receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);

            for (int retryCount = 0; retryCount < sendEventCount; retryCount++) {
                // retry to flush all messages in cache
                receiverLowEpoch.receiveSync(10);
            }
        } finally {
            receiverLowEpoch.closeSync();
        }
    }

    @Test(expected = ReceiverDisconnectedException.class)
    public void testOldHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException {
        Instant testStartTime = Instant.now();
        long epoch = Math.abs(new Random().nextLong());

        if (epoch < 11L) {
            epoch += 11L;
        }

        receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime), epoch);
        receiver.setReceiveTimeout(Duration.ofSeconds(10));
        PartitionReceiver epochReceiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream(), epoch - 10);
        try {
            TestBase.pushEventsToPartition(ehClient, PARTITION_ID, 5).get();
            Assert.assertTrue(receiver.receiveSync(10).iterator().hasNext());
        } finally {
            epochReceiver.closeSync();
        }
    }

    @Test(expected = ReceiverDisconnectedException.class)
    public void testNewHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException {
        int sendEventCount = 5;
        long epoch = new Random().nextInt(Integer.MAX_VALUE);

        PartitionReceiver receiverLowEpoch = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), epoch);

        try {
            receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
            TestBase.pushEventsToPartition(ehClient, PARTITION_ID, sendEventCount).get();
            receiverLowEpoch.receiveSync(20);

            receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);

            for (int retryCount = 0; retryCount < sendEventCount; retryCount++) {
                // retry to flush all messages in cache
                receiverLowEpoch.receiveSync(10);
            }
        } finally {
            receiverLowEpoch.closeSync();
        }
    }

    @After
    public void testCleanup() throws EventHubException {
        if (receiver != null) {
            receiver.closeSync();
        }
    }
}
