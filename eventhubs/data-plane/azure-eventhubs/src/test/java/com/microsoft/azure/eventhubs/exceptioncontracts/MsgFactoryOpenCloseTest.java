// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.FaultInjectingReactorFactory;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MsgFactoryOpenCloseTest extends ApiTestBase {

    private static final String PARTITION_ID = "0";
    private static ConnectionStringBuilder connStr;

    @BeforeClass
    public static void initialize() {
        connStr = TestContext.getConnectionString();
    }

    @Test()
    public void verifyTaskQueueEmptyOnMsgFactoryGracefulClose() throws Exception {

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
    public void verifyTaskQueueEmptyOnMsgFactoryWithPumpGracefulClose() throws Exception {

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

    @Ignore("TODO: Investigate testcase failure.")
    @Test()
    public void verifyThreadReleaseOnMsgFactoryOpenError() throws Exception {

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
    public void supplyClosedExecutorServiceToEventHubClient() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);
        testClosed.shutdown();

        EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);
    }

    @Test(expected = RejectedExecutionException.class)
    public void supplyClosedExecutorServiceToSendOperation() throws Exception {
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
    public void supplyClosedExecutorServiceToReceiveOperation() throws Exception {
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
    public void supplyClosedExecutorServiceToCreateLinkOperation() throws Exception {
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
    public void supplyClosedExecutorServiceToCreateSenderOperation() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.createPartitionSenderSync(PARTITION_ID);
    }

    @Test(expected = RejectedExecutionException.class)
    public void supplyClosedExecutorServiceToCreateReceiverOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());
    }

    @Test(expected = RejectedExecutionException.class)
    public void supplyClosedExecutorServiceThenMgmtOperation() throws Throwable {
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
    public void supplyClosedExecutorServiceThenFactoryCloseOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.closeSync();
    }

    @Test(expected = RejectedExecutionException.class)
    public void supplyClosedExecutorServiceThenSenderCloseOperation() throws Exception {
        final ScheduledThreadPoolExecutor testClosed = new ScheduledThreadPoolExecutor(1);

        final PartitionSender temp = EventHubClient.createSync(
                TestContext.getConnectionString().toString(),
                testClosed).createPartitionSenderSync(PARTITION_ID);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        temp.closeSync();
    }

    @Test(expected = RejectedExecutionException.class)
    public void supplyClosedExecutorServiceThenReceiverCloseOperation() throws Exception {
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
