// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.QuotaExceededException;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ReceiverIdentifierTest extends ApiTestBase {

    private static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final int SENT_EVENTS = 25;
    private static final List<PartitionReceiver> RECEIVERS = new LinkedList<>();

    private static EventHubClient ehClient;

    @BeforeClass
    public static void initializeEventHub() throws Exception {

        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);

        TestBase.pushEventsToPartition(ehClient, PARTITION_ID, SENT_EVENTS).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {

        for (PartitionReceiver receiver : RECEIVERS) {
            receiver.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test()
    public void testReceiverIdentifierShowsUpInQuotaErrors() throws EventHubException {

        final String receiverIdentifierPrefix = UUID.randomUUID().toString();
        for (int receiverCount = 0; receiverCount < 5; receiverCount++) {
            final ReceiverOptions options = new ReceiverOptions();
            options.setIdentifier(receiverIdentifierPrefix + receiverCount);
            ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream(), options);
        }

        try {
            ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream());
            Assert.assertTrue(false);
        } catch (QuotaExceededException quotaError) {
            final String errorMsg = quotaError.getMessage();
            for (int receiverCount = 0; receiverCount < 5; receiverCount++) {
                Assert.assertTrue(errorMsg.contains(receiverIdentifierPrefix + receiverCount));
            }
        }
    }
}
