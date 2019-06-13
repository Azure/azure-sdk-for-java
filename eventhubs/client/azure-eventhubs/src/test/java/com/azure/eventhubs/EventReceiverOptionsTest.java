// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ApiTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;

public class EventReceiverOptionsTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final int DEFAULT_PREFETCH_COUNT = 500; // will delete it if EventReceiver make it public

    // since we cannot test receiving very large prefetch like 10000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    private static final int EVENT_COUNT = DEFAULT_PREFETCH_COUNT * 3;
    private static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    private static EventHubClient ehClient;
    private static EventReceiver receiver;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        ehClient = ApiTestBase.getEventHubClientBuilder().build();
    }

    @AfterClass
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
        }
    }

    @Test
    public void setLargePrefetchCount() {
        receiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .consumerGroup(ApiTestBase.getConsumerGroupName())
                .beginReceivingAt(EventPosition.newEventsOnly())
                .prefetchCount(2000));
        // TODO: receive time out missing?

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    @Test
    public void setSmallFrefetchCount() {
        receiver = ehClient.createReceiver(PARTITION_ID,
            new EventReceiverOptions()
                .consumerGroup(ApiTestBase.getConsumerGroupName())
                .beginReceivingAt(EventPosition.newEventsOnly())
                .prefetchCount(11));
        // TODO: receive time out missing?

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }
}
