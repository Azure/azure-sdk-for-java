/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.microsoft.azure.servicebus.QuotaExceededException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceiverIdentifierTest extends ApiTestBase {

    static final String cgName = TestContext.getConsumerGroupName();
    static final String partitionId = "0";
    static final Instant beforeTestStart = Instant.now();
    static final int sentEvents = 25;
    static final List<PartitionReceiver> receivers = new LinkedList<>();

    static EventHubClient ehClient;

    @BeforeClass
    public static void initializeEventHub()  throws Exception {

        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());

        TestBase.pushEventsToPartition(ehClient, partitionId, sentEvents).get();
    }

    @Test()
    public void testReceiverIdentierShowsUpInQuotaErrors() throws ServiceBusException {

        final String receiverIdentifierPrefix = UUID.randomUUID().toString();
        for (int receiverCount = 0; receiverCount < 5; receiverCount ++) {
            final ReceiverOptions options = new ReceiverOptions();
            options.setIdentifier(receiverIdentifierPrefix + receiverCount);
            ehClient.createReceiverSync(cgName, partitionId, PartitionReceiver.START_OF_STREAM, options);
        }

        try {
            ehClient.createReceiverSync(cgName, partitionId, PartitionReceiver.START_OF_STREAM);
            Assert.assertTrue(false);
        }
        catch (QuotaExceededException quotaError) {
            final String errorMsg = quotaError.getMessage();
            for (int receiverCount=0; receiverCount < 5; receiverCount++) {
                Assert.assertTrue(errorMsg.contains(receiverIdentifierPrefix + receiverCount));
            }
        }
    }

    @AfterClass()
    public static void cleanup() throws ServiceBusException {

        for (PartitionReceiver receiver : receivers)
            receiver.closeSync();

        if (ehClient != null)
            ehClient.closeSync();
    }
}
