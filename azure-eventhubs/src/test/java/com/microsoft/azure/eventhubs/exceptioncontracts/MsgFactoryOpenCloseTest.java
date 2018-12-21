/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.FaultInjectingReactorFactory;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.*;

public class MsgFactoryOpenCloseTest extends ApiTestBase {

    static final String PARTITION_ID = "0";
    static ConnectionStringBuilder connStr;

    @BeforeClass
    public static void initialize() {
        connStr = TestContext.getConnectionString();
    }

    @Test()
    public void VerifyTaskQueueEmptyOnMsgFactoryGracefulClose() throws Exception {

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            final EventHubClient ehClient = EventHubClient.createSync(
                    TestContext.getConnectionString().toString(),
                    executor);

            final PartitionReceiver receiver = ehClient.createReceiverSync(
                    TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
            final PartitionSender sender = ehClient.createPartitionSenderSync(PARTITION_ID);
            sender.sendSync(EventData.create("test data - string".getBytes()));
            Iterable<EventData> events = receiver.receiveSync(10);

            Assert.assertTrue(events.iterator().hasNext());
            sender.closeSync();
            receiver.closeSync();

            ehClient.closeSync();

            Assert.assertEquals(((ScheduledThreadPoolExecutor) executor).getQueue().size(), 0);
        } finally {
            executor.shutdown();
        }
    }

    @Test()
    public void VerifyTaskQueueEmptyOnMsgFactoryWithPumpGracefulClose() throws Exception {

        final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

        try {
            final EventHubClient ehClient = EventHubClient.createSync(
                    TestContext.getConnectionString().toString(),
                    executor);

            final PartitionReceiver receiver = ehClient.createReceiverSync(
                    TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));

            final CompletableFuture<Iterable<EventData>> signalReceive = new CompletableFuture<>();
            receiver.setReceiveHandler(new PartitionReceiveHandler() {
                @Override
                public int getMaxEventCount() {
                    return 10;
                }

                @Override
                public void onReceive(Iterable<EventData> events) {
                    signalReceive.complete(events);
                }

                @Override
                public void onError(Throwable error) {
                }
            }, false);

            final PartitionSender sender = ehClient.createPartitionSenderSync(PARTITION_ID);
            sender.sendSync(EventData.create("test data - string".getBytes()));

            final Iterable<EventData> events = signalReceive.get();
            Assert.assertTrue(events.iterator().hasNext());

            receiver.setReceiveHandler(null).get();

            sender.closeSync();
            receiver.closeSync();

            ehClient.closeSync();

            Assert.assertEquals(((ScheduledThreadPoolExecutor) executor).getQueue().size(), 0);
        } finally {
            executor.shutdown();
        }
    }

    @Test()
    public void VerifyThreadReleaseOnMsgFactoryOpenError() throws Exception {

        final FaultInjectingReactorFactory networkOutageSimulator = new FaultInjectingReactorFactory();
        networkOutageSimulator.setFaultType(FaultInjectingReactorFactory.FaultType.NetworkOutage);

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try {
            final CompletableFuture<MessagingFactory> openFuture = MessagingFactory.createFromConnectionString(
                    connStr.toString(), null,
                    executor,
                    networkOutageSimulator);
            try {
                openFuture.get();
                Assert.assertFalse(true);
            } catch (ExecutionException error) {
                Assert.assertEquals(EventHubException.class, error.getCause().getClass());
            }

            Thread.sleep(1000); // for reactor to transition from cleanup to complete-stop

            Assert.assertEquals(((ScheduledThreadPoolExecutor) executor).getQueue().size(), 0);
        } finally {
            executor.shutdown();
        }
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToEventHubClient() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);
        testClosed.shutdown();

        EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToSendOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);
        temp.sendSync(EventData.create("test data - string".getBytes()));

        testClosed.shutdown();

        temp.sendSync(EventData.create("test data - string".getBytes()));
        testClosed.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToReceiveOperation() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);

        final PartitionReceiver temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed)
                .createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.receiveSync(20);
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToCreateLinkOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        // first send creates send link
        temp.sendSync(EventData.create("test data - string".getBytes()));
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToCreateSenderOperation() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.createPartitionSenderSync(PARTITION_ID);
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToCreateReceiverOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceThenMgmtOperation() throws Throwable {
        final ScheduledThreadPoolExecutor testClosed = new ScheduledThreadPoolExecutor(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        try {
            temp.getPartitionRuntimeInformation(PARTITION_ID).get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceThenFactoryCloseOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.closeSync();
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceThenSenderCloseOperation() throws Exception {
        final ScheduledThreadPoolExecutor testClosed = new ScheduledThreadPoolExecutor(1);

        final PartitionSender temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed).createPartitionSenderSync(PARTITION_ID);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.closeSync();
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceThenReceiverCloseOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final PartitionReceiver temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed).createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.closeSync();
    }

    @Test(expected = RejectedExecutionException.class)
    public void testEventHubClientSendAfterClose() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        final EventHubClient eventHubClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        eventHubClient.closeSync();
        eventHubClient.sendSync(EventData.create("test message".getBytes()));
    }

    @Test(expected = IllegalStateException.class)
    public void testEventHubClientSendCloseAfterSomeSends() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        final EventHubClient eventHubClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        eventHubClient.sendSync(EventData.create("test message".getBytes()));
        eventHubClient.closeSync();
        eventHubClient.sendSync(EventData.create("test message".getBytes()));
    }
}
