// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.CommunicationException;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubClientOptions;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.FaultInjectingReactorFactory;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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

    @BeforeAll
    public static void initialize() {
        connStr = TestContext.getConnectionString();
    }

    @Test
    public void verifyTaskQueueEmptyOnMsgFactoryGracefulClose() throws Exception {

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(
                    TestContext.getConnectionString().toString(),
                    executor);

            final PartitionReceiver receiver = ehClient.createReceiverSync(
                    TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
            final PartitionSender sender = ehClient.createPartitionSenderSync(PARTITION_ID);
            sender.sendSync(EventData.create("test data - string".getBytes()));
            Iterable<EventData> events = receiver.receiveSync(10);

            Assertions.assertTrue(events.iterator().hasNext());
            sender.closeSync();
            receiver.closeSync();

            ehClient.closeSync();

            Assertions.assertEquals(((ScheduledThreadPoolExecutor) executor).getQueue().size(), 0);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void verifyTaskQueueEmptyOnMsgFactoryWithPumpGracefulClose() throws Exception {

        final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

        try {
            final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(
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
            Assertions.assertTrue(events.iterator().hasNext());

            receiver.setReceiveHandler(null).get();

            sender.closeSync();
            receiver.closeSync();

            ehClient.closeSync();

            Assertions.assertEquals(((ScheduledThreadPoolExecutor) executor).getQueue().size(), 0);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void verifyThreadReleaseOnMsgFactoryOpenError() throws Exception {

        final FaultInjectingReactorFactory networkOutageSimulator = new FaultInjectingReactorFactory();
        networkOutageSimulator.setFaultType(FaultInjectingReactorFactory.FaultType.NetworkOutage);

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try {
            final CompletableFuture<MessagingFactory> openFuture = MessagingFactory.createFromConnectionString(
                    connStr.toString(),
                    null,
                    executor,
                    networkOutageSimulator,
                    null,
                    EventHubClientOptions.SILENT_OFF);
            try {
                openFuture.get();
                Assertions.fail();
            } catch (ExecutionException error) {
                Assertions.assertEquals(CommunicationException.class, error.getCause().getClass());
            }

            // Waiting for reactor to transition from cleanup to complete-stop, this requires at least 60 seconds until
            // the items are emptied.
            Thread.sleep(Duration.ofSeconds(90).toMillis());

            Assertions.assertEquals(0, ((ScheduledThreadPoolExecutor) executor).getQueue().size());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void supplyClosedExecutorServiceToEventHubClient() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);
        testClosed.shutdown();

        Assertions.assertThrows(RejectedExecutionException.class,
            () -> EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(), testClosed));
    }

    @Test
    public void supplyClosedExecutorServiceToSendOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);
        temp.sendSync(EventData.create("test data - string".getBytes()));

        testClosed.shutdown();

        Assertions.assertThrows(RejectedExecutionException.class,
            () -> temp.sendSync(EventData.create("test data - string".getBytes())));
        testClosed.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void supplyClosedExecutorServiceToReceiveOperation() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);

        final PartitionReceiver temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed)
                .createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, () -> temp.receiveSync(20));
    }

    @Test
    public void supplyClosedExecutorServiceToCreateLinkOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        // first send creates send link
        Assertions.assertThrows(RejectedExecutionException.class,
            () -> temp.sendSync(EventData.create("test data - string".getBytes())));
    }

    @Test
    public void supplyClosedExecutorServiceToCreateSenderOperation() throws Exception {
        final ScheduledExecutorService testClosed = new ScheduledThreadPoolExecutor(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, () -> temp.createPartitionSenderSync(PARTITION_ID));
    }

    @Test
    public void supplyClosedExecutorServiceToCreateReceiverOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class,
            () -> temp.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream()));
    }

    @Test
    public void supplyClosedExecutorServiceThenMgmtOperation() throws Throwable {
        final ScheduledThreadPoolExecutor testClosed = new ScheduledThreadPoolExecutor(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            try {
                temp.getPartitionRuntimeInformation(PARTITION_ID).get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void supplyClosedExecutorServiceThenFactoryCloseOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, temp::closeSync);
    }

    @Test
    public void supplyClosedExecutorServiceThenSenderCloseOperation() throws Exception {
        final ScheduledThreadPoolExecutor testClosed = new ScheduledThreadPoolExecutor(1);

        final PartitionSender temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed).createPartitionSenderSync(PARTITION_ID);

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, temp::closeSync);
    }

    @Test
    public void supplyClosedExecutorServiceThenReceiverCloseOperation() throws Exception {
        final ScheduledExecutorService testClosed = Executors.newScheduledThreadPool(1);

        final PartitionReceiver temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed).createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEndOfStream());

        testClosed.shutdown();
        testClosed.awaitTermination(60, TimeUnit.SECONDS);

        Assertions.assertThrows(RejectedExecutionException.class, temp::closeSync);
    }

    @Test
    public void testEventHubClientSendAfterClose() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        eventHubClient.closeSync();
        Assertions.assertThrows(RejectedExecutionException.class,
            () -> eventHubClient.sendSync(EventData.create("test message".getBytes())));
    }

    @Test
    public void testEventHubClientSendCloseAfterSomeSends() throws Exception {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        eventHubClient.sendSync(EventData.create("test message".getBytes()));
        eventHubClient.closeSync();
        Assertions.assertThrows(IllegalStateException.class,
            () -> eventHubClient.sendSync(EventData.create("test message".getBytes())));
    }
}
