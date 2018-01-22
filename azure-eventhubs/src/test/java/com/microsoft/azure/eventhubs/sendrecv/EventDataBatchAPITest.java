/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.microsoft.azure.eventhubs.*;
import junit.framework.AssertionFailedError;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;

public class EventDataBatchAPITest extends ApiTestBase {

    private static final String cgName = TestContext.getConsumerGroupName();
    private static final String partitionId = "0";
    private static EventHubClient ehClient;
    private static PartitionSender sender = null;

    @BeforeClass
    public static void initializeEventHub() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        sender = ehClient.createPartitionSenderSync(partitionId);
    }

    @Test
    public void sendSmallEventsFullBatchTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final EventDataBatch batchEvents = sender.createBatch();

        while (batchEvents.tryAdd(new EventData("a".getBytes())));

        sender = ehClient.createPartitionSenderSync(partitionId);
        sender.sendSync(batchEvents);
    }

    @Test
    public void sendSmallEventsFullBatchPartitionKeyTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final BatchOptions options = new BatchOptions()
                .with(o -> o.partitionKey = UUID.randomUUID().toString());
        final EventDataBatch batchEvents = ehClient.createBatch(options);

        while (batchEvents.tryAdd(new EventData("a".getBytes())));

        ehClient.sendSync(batchEvents);
    }

    @Test
    public void sendBatchPartitionKeyValidateTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final String partitionKey = UUID.randomUUID().toString();

        final BatchOptions options = new BatchOptions().with( o -> o.partitionKey = partitionKey );
        final EventDataBatch batchEvents = ehClient.createBatch(options);

        int count = 0;
        while (batchEvents.tryAdd(new EventData("a".getBytes())) && count++ < 10);

        final int sentCount = count;
        final CompletableFuture<Void> testResult = new CompletableFuture<>();
        final PartitionReceiveHandler validator = new PartitionReceiveHandler(100) {
            final AtomicInteger netCount = new AtomicInteger(0);

            @Override
            public void onReceive(Iterable<EventData> events) {
                if (events != null) {
                    final Iterator<EventData> eterator = events.iterator();
                    while (eterator.hasNext()) {
                        final EventData currentData = eterator.next();
                        final String currentPartitionKey = currentData.getSystemProperties().getPartitionKey();
                        if (!currentPartitionKey.equalsIgnoreCase(partitionKey))
                            testResult.completeExceptionally(new AssertionFailedError());

                        final int countSoFar = netCount.incrementAndGet();
                        if (countSoFar >= sentCount)
                            testResult.complete(null);
                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                testResult.completeExceptionally(error);
            }
        };

        final LinkedList<PartitionReceiver> receivers = new LinkedList<>();
        try {
            final String[] partitionIds = ehClient.getRuntimeInformation().get().getPartitionIds();
            for (int index = 0; index < partitionIds.length; index++) {
                final PartitionReceiver receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), partitionIds[index], EventPosition.fromEndOfStream());
                receiver.setReceiveTimeout(Duration.ofSeconds(5));
                receiver.setReceiveHandler(validator);
                receivers.add(receiver);
            }

            ehClient.sendSync(batchEvents);
            testResult.get();
        }
        finally {
            if (receivers.size() > 0)
                receivers.forEach(new Consumer<PartitionReceiver>() {
                    @Override
                    public void accept(PartitionReceiver partitionReceiver) {
                        try {
                            partitionReceiver.closeSync();
                        } catch (EventHubException ignore) {
                        }
                    }
                });
        }
    }

    @Test
    public void sendEventsFullBatchWithAppPropsTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Void> validator = new CompletableFuture<>();
        final PartitionReceiver receiver = ehClient.createReceiverSync(cgName, partitionId, EventPosition.fromEndOfStream());
        receiver.setReceiveTimeout(Duration.ofSeconds(5));

        try {
            final EventDataBatch batchEvents = sender.createBatch();

            int count = 0;
            while (true) {
                final EventData eventData = new EventData(new String(new char[new Random().nextInt(50000)]).replace("\0", "a").getBytes());
                for (int i = 0; i < new Random().nextInt(20); i++)
                    eventData.getProperties().put("somekey" + i, "somevalue");

                if (batchEvents.tryAdd(eventData))
                    count++;
                else
                    break;
            }

            Assert.assertEquals(count, batchEvents.getSize());
            receiver.setReceiveHandler(new CountValidator(validator, count));

            sender.sendSync(batchEvents);

            validator.get(100, TimeUnit.SECONDS);

            receiver.setReceiveHandler(null);
        }finally {
            receiver.closeSync();
        }
    }

    @Test
    public void sendEventsFullBatchWithPartitionKeyTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {

        final String partitionKey = UUID.randomUUID().toString();
        final BatchOptions options = new BatchOptions().with( o -> o.partitionKey = partitionKey);
        final EventDataBatch batchEvents = ehClient.createBatch(options);

        int count = 0;
        while (true) {
            final EventData eventData = new EventData(new String("a").getBytes());
            for (int i=0;i<new Random().nextInt(20);i++)
                eventData.getProperties().put("somekey" + i, "somevalue");

            if (batchEvents.tryAdd(eventData))
                count++;
            else
                break;
        }

        Assert.assertEquals(count, batchEvents.getSize());
        ehClient.sendSync(batchEvents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendBatchWithPartitionKeyOnPartitionSenderTest()
            throws EventHubException, InterruptedException, ExecutionException, TimeoutException {


        final BatchOptions options = new BatchOptions().with( o -> o.partitionKey = UUID.randomUUID().toString() );
        final EventDataBatch batchEvents = sender.createBatch(options);

        int count = 0;
        while (true) {
            final EventData eventData = new EventData(new String("a").getBytes());
            for (int i=0;i<new Random().nextInt(20);i++)
                eventData.getProperties().put("somekey" + i, "somevalue");

            if (batchEvents.tryAdd(eventData))
                count++;
            else
                break;
        }

        Assert.assertEquals(count, batchEvents.getSize());

        // the CreateBatch was created without taking PartitionKey size into account
        // so this call should fail with payload size exceeded
        sender.sendSync(batchEvents);
    }

    @AfterClass
    public static void cleanupClient() throws EventHubException
    {
        sender.closeSync();
        ehClient.closeSync();
    }

    public static class CountValidator extends PartitionReceiveHandler {
        final CompletableFuture<Void> validateSignal;
        final int netEventCount;

        int currentCount = 0;

        public CountValidator(final CompletableFuture<Void> validateSignal, final int netEventCount) {
            super(999);
            this.validateSignal = validateSignal;
            this.netEventCount = netEventCount;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
            if (events != null)
                for (EventData event : events) {
                    currentCount++;
                }

            if (currentCount >= netEventCount)
                this.validateSignal.complete(null);

            try {
                Thread.sleep(100); // wait for events to accumulate in the receive pump
            } catch (InterruptedException ignore) {
            }
        }

        @Override
        public void onError(Throwable error) {
            this.validateSignal.completeExceptionally(error);
        }
    }
}
