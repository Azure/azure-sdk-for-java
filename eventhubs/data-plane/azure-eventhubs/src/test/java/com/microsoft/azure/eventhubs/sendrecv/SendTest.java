/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class SendTest extends ApiTestBase {
    static final String cgName = TestContext.getConsumerGroupName();
    static final String partitionId = "0";
    static final String ORDER_PROPERTY = "order";
    static EventHubClient ehClient;

    PartitionSender sender = null;
    List<PartitionReceiver> receivers = new LinkedList<>();

    @BeforeClass
    public static void initialize() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        initializeEventHub(connectionString);
    }
    public static void initializeEventHub(final ConnectionStringBuilder connectionString) throws Exception {
        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    }

    @AfterClass
    public static void cleanupClient() throws EventHubException {
        if (ehClient != null)
            ehClient.closeSync();
    }

    @Test
    public void sendBatchRetainsOrderWithinBatch() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        LinkedList<EventData> batchEvents = new LinkedList<>();
        final int batchSize = 50;
        for (int count = 0; count < batchSize; count++) {
            EventData event = EventData.create("a".getBytes());
            event.getProperties().put(ORDER_PROPERTY, count);
            batchEvents.add(event);
        }

        final CompletableFuture<Void> validator = new CompletableFuture<>();
        final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()));
        this.receivers.add(receiver);
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new OrderValidator(validator, batchSize));

        // run out of messages in that specific partition - to account for clock-skew with Instant.now() on test machine vs eventhubs service
        Iterable<EventData> clockSkewEvents;
        do {
            clockSkewEvents = receiver.receiveSync(100);
        } while (clockSkewEvents != null && clockSkewEvents.iterator().hasNext());

        sender = ehClient.createPartitionSenderSync(partitionId);
        sender.sendSync(batchEvents);

        validator.get(25, TimeUnit.SECONDS);
    }

    @Test
    public void sendResultsInSysPropertiesWithPartitionKey() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final int partitionCount = ehClient.getRuntimeInformation().get().getPartitionCount();
        final String partitionKey = UUID.randomUUID().toString();
        CompletableFuture<Void> validateSignal = new CompletableFuture<>();
        PartitionKeyValidator validator = new PartitionKeyValidator(validateSignal, partitionKey, 1);
        for (int receiversCount = 0; receiversCount < partitionCount; receiversCount++) {
            final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, Integer.toString(receiversCount), EventPosition.fromEnqueuedTime(Instant.now()));
            receivers.add(receiver);

            // run out of messages in that specific partition - to account for clock-skew with Instant.now() on test machine vs eventhubs service
            receiver.setReceiveTimeout(Duration.ofSeconds(5));
            Iterable<EventData> clockSkewEvents;
            do {
                clockSkewEvents = receiver.receiveSync(100);
            } while (clockSkewEvents != null && clockSkewEvents.iterator().hasNext());

            receiver.setReceiveHandler(validator);
        }

        ehClient.sendSync(EventData.create("TestMessage".getBytes()), partitionKey);
        validateSignal.get(partitionCount * 5, TimeUnit.SECONDS);
    }

    @Test
    public void sendBatchResultsInSysPropertiesWithPartitionKey() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final int batchSize = 20;
        final int partitionCount = ehClient.getRuntimeInformation().get().getPartitionCount();
        final String partitionKey = UUID.randomUUID().toString();
        CompletableFuture<Void> validateSignal = new CompletableFuture<>();
        PartitionKeyValidator validator = new PartitionKeyValidator(validateSignal, partitionKey, batchSize);
        for (int receiversCount = 0; receiversCount < partitionCount; receiversCount++) {
            final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, Integer.toString(receiversCount), EventPosition.fromEnqueuedTime(Instant.now()));
            receivers.add(receiver);

            // run out of messages in that specific partition - to account for clock-skew with Instant.now() on test machine vs eventhubs service
            receiver.setReceiveTimeout(Duration.ofSeconds(5));
            Iterable<EventData> clockSkewEvents;
            do {
                clockSkewEvents = receiver.receiveSync(100);
            } while (clockSkewEvents != null && clockSkewEvents.iterator().hasNext());

            receiver.setReceiveHandler(validator);
        }

        List<EventData> events = new LinkedList<>();
        for (int index = 0; index < batchSize; index++)
            events.add(EventData.create("TestMessage".getBytes()));

        ehClient.sendSync(events, partitionKey);
        validateSignal.get(partitionCount * 5, TimeUnit.SECONDS);
    }

    @After
    public void cleanup() throws EventHubException {
        if (sender != null) {
            sender.closeSync();
            sender = null;
        }

        if (receivers != null && !receivers.isEmpty()) {
            for (PartitionReceiver receiver : receivers)
                receiver.closeSync();

            receivers.clear();
        }
    }

    public static class PartitionKeyValidator implements PartitionReceiveHandler {
        final CompletableFuture<Void> validateSignal;
        final String partitionKey;
        final int eventCount;
        int currentEventCount = 0;

        protected PartitionKeyValidator(final CompletableFuture<Void> validateSignal, final String partitionKey, final int eventCount) {
            this.validateSignal = validateSignal;
            this.partitionKey = partitionKey;
            this.eventCount = eventCount;
        }

        @Override
        public int getMaxEventCount() {
            return 50;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
            if (events != null && events.iterator().hasNext()) {
                for (EventData event : events) {
                    if (!partitionKey.equals(event.getSystemProperties().getPartitionKey()))
                        this.validateSignal.completeExceptionally(
                                new AssertionFailedError(String.format("received partitionKey: %s, expected partitionKey: %s", event.getSystemProperties().getPartitionKey(), partitionKey)));

                    this.currentEventCount++;
                }

                if (this.currentEventCount == this.eventCount)
                    this.validateSignal.complete(null);
            }
        }

        @Override
        public void onError(Throwable error) {
            this.validateSignal.completeExceptionally(error);
        }
    }

    public static class OrderValidator implements PartitionReceiveHandler {
        final CompletableFuture<Void> validateSignal;
        final int netEventCount;

        int currentCount = 0;

        public OrderValidator(final CompletableFuture<Void> validateSignal, final int netEventCount) {
            this.validateSignal = validateSignal;
            this.netEventCount = netEventCount;
        }

        @Override
        public int getMaxEventCount() {
            return 100;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
            if (events != null)
                for (EventData event : events) {
                    final int currentEventOrder = (int) event.getProperties().get(ORDER_PROPERTY);
                    if (currentEventOrder != currentCount)
                        this.validateSignal.completeExceptionally(new AssertionError(String.format("expected %s, got %s", currentCount, currentEventOrder)));

                    currentCount++;
                }

            if (currentCount >= netEventCount)
                this.validateSignal.complete(null);
        }

        @Override
        public void onError(Throwable error) {
            this.validateSignal.completeExceptionally(error);
        }
    }
}
