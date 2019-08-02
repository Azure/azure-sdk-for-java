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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.LinkedList;

public class SetPrefetchCountTest extends ApiTestBase {
    static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();
    static final String PARTITION_ID = "0";

    // since we cannot test receiving very large prefetch like 100000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    static final int EVENT_COUNT = PartitionReceiver.DEFAULT_PREFETCH_COUNT * 3;

    static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    static EventHubClient ehClient;

    PartitionReceiver testReceiver = null;

    @BeforeClass
    public static void initializeEventHub() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        TestBase.pushEventsToPartition(ehClient, PARTITION_ID, EVENT_COUNT).get();
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Test()
    public void testSetPrefetchCountToLargeValue() throws EventHubException {
        ReceiverOptions options = new ReceiverOptions();
        options.setPrefetchCount(2000);
        testReceiver = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream(), options);
        testReceiver.setReceiveTimeout(Duration.ofSeconds(2));
        int eventsReceived = 0;
        int retryCount = 0;
        while (eventsReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Iterable<EventData> events = testReceiver.receiveSync(EVENT_COUNT);
            if (events == null || !events.iterator().hasNext()) {
                retryCount++;
            } else {
                eventsReceived += ((LinkedList<EventData>) events).size();
            }
        }

        Assert.assertTrue(eventsReceived >= EVENT_COUNT);
    }

    @Test()
    public void testSetPrefetchCountToSmallValue() throws EventHubException {
        ReceiverOptions options = new ReceiverOptions();
        options.setPrefetchCount(11);
        testReceiver = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream(), options);
        testReceiver.setReceiveTimeout(Duration.ofSeconds(2));
        int eventsReceived = 0;
        int retryCount = 0;
        while (eventsReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Iterable<EventData> events = testReceiver.receiveSync(10);
            if (events == null || !events.iterator().hasNext()) {
                retryCount++;
            } else {
                eventsReceived += ((LinkedList<EventData>) events).size();
            }
        }

        Assert.assertTrue(eventsReceived >= EVENT_COUNT);
    }

    @After
    public void testCleanup() throws EventHubException {

        if (testReceiver != null) {
            testReceiver.closeSync();
        }
    }
}
