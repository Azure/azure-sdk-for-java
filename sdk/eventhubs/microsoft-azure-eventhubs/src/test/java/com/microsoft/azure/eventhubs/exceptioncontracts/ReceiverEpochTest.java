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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

    @BeforeAll
    public static void initializeEventHub() throws EventHubException, IOException {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    }

    @AfterAll
    public static void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test
    public void testEpochReceiverWins() throws EventHubException, InterruptedException, ExecutionException {
        int sendEventCount = 5;

        PartitionReceiver receiverLowEpoch = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));

        try {
            receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
            TestBase.pushEventsToPartition(ehClient, PARTITION_ID, sendEventCount).get();

            receiverLowEpoch.receiveSync(20);
            receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);

            Assertions.assertThrows(ReceiverDisconnectedException.class, () -> {
                for (int retryCount = 0; retryCount < sendEventCount; retryCount++) {
                    // retry to flush all messages in cache
                    receiverLowEpoch.receiveSync(10);
                }
            });
        } finally {
            receiverLowEpoch.closeSync();
        }
    }

    @Test
    public void testOldHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException {
        Instant testStartTime = Instant.now();
        long epoch = Math.abs(new Random().nextLong());

        if (epoch < 11L) {
            epoch += 11L;
        }

        receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(testStartTime), epoch);
        receiver.setReceiveTimeout(Duration.ofSeconds(10));
        final long lowerEpoch = epoch - 10;
        Assertions.assertThrows(ReceiverDisconnectedException.class, () -> {
            PartitionReceiver epochReceiver = ehClient.createEpochReceiverSync(
                CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream(), lowerEpoch);
            try {
                TestBase.pushEventsToPartition(ehClient, PARTITION_ID, 5).get();
                Assertions.assertTrue(receiver.receiveSync(10).iterator().hasNext());
            } finally {
                epochReceiver.closeSync();
            }
        });
    }

    @Test
    public void testNewHighestEpochWins() throws EventHubException, InterruptedException, ExecutionException {
        int sendEventCount = 5;
        long epoch = new Random().nextInt(Integer.MAX_VALUE);

        PartitionReceiver receiverLowEpoch = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), epoch);

        try {
            receiverLowEpoch.setReceiveTimeout(Duration.ofSeconds(2));
            TestBase.pushEventsToPartition(ehClient, PARTITION_ID, sendEventCount).get();
            receiverLowEpoch.receiveSync(20);

            receiver = ehClient.createEpochReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), Long.MAX_VALUE);

            Assertions.assertThrows(ReceiverDisconnectedException.class, () -> {
                for (int retryCount = 0; retryCount < sendEventCount; retryCount++) {
                    // retry to flush all messages in cache
                    receiverLowEpoch.receiveSync(10);
                }
            });
        } finally {
            receiverLowEpoch.closeSync();
        }
    }

    @AfterEach
    public void testCleanup() throws EventHubException {
        if (receiver != null) {
            receiver.closeSync();
        }
    }
}
