// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;

public class ReceiverRuntimeMetricsTest extends ApiTestBase {

    private static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();
    private static final String PARTITION_ID = "0";
    private static final int SENT_EVENTS = 25;

    private static EventHubClient ehClient;
    private static PartitionReceiver receiverWithOptions = null;
    private static PartitionReceiver receiverWithoutOptions = null;
    private static PartitionReceiver receiverWithOptionsDisabled = null;

    @BeforeClass
    public static void initializeEventHub() throws Exception {

        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);

        ReceiverOptions options = new ReceiverOptions();
        options.setReceiverRuntimeMetricEnabled(true);

        ReceiverOptions optionsWithMetricsDisabled = new ReceiverOptions();
        optionsWithMetricsDisabled.setReceiverRuntimeMetricEnabled(false);

        receiverWithOptions = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()), options);
        receiverWithoutOptions = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH));
        receiverWithOptionsDisabled = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH), optionsWithMetricsDisabled);

        TestBase.pushEventsToPartition(ehClient, PARTITION_ID, SENT_EVENTS).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {

        if (receiverWithOptions != null) {
            receiverWithOptions.closeSync();
        }

        if (receiverWithoutOptions != null) {
            receiverWithoutOptions.closeSync();
        }

        if (receiverWithOptionsDisabled != null) {
            receiverWithOptionsDisabled.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test()
    public void testRuntimeMetricsReturnedWhenEnabled() throws EventHubException {

        LinkedList<EventData> receivedEventsWithOptions = new LinkedList<>();
        while (receivedEventsWithOptions.size() < SENT_EVENTS) {
            for (EventData eData : receiverWithOptions.receiveSync(1)) {
                receivedEventsWithOptions.add(eData);
                Assert.assertEquals((Long) eData.getSystemProperties().getSequenceNumber(),
                    receiverWithOptions.getEventPosition().getSequenceNumber());
            }
        }

        HashSet<String> offsets = new HashSet<>();
        for (EventData eData : receivedEventsWithOptions) {
            offsets.add(eData.getSystemProperties().getOffset());
        }

        Assert.assertTrue(receiverWithOptions.getRuntimeInformation() != null);
        Assert.assertTrue(offsets.contains(receiverWithOptions.getRuntimeInformation().getLastEnqueuedOffset()));
        Assert.assertTrue(receiverWithOptions.getRuntimeInformation().getLastEnqueuedSequenceNumber() >= receivedEventsWithOptions.iterator().next().getSystemProperties().getSequenceNumber());
    }

    @Test()
    public void testRuntimeMetricsWhenDisabled() throws EventHubException {

        receiverWithOptionsDisabled.receiveSync(10);
        Assert.assertTrue(receiverWithOptionsDisabled.getRuntimeInformation() == null);
    }

    @Test()
    public void testRuntimeMetricsDefaultDisabled() throws EventHubException {

        receiverWithoutOptions.receiveSync(10);
        Assert.assertTrue(receiverWithoutOptions.getRuntimeInformation() == null);
    }
}
