/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.FaultInjectingReactorFactory;
import com.microsoft.azure.eventhubs.lib.TestContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MsgFactoryOpenCloseTest extends ApiTestBase {

    static final String PARTITION_ID = "0";
    static ConnectionStringBuilder connStr;

    @BeforeClass
    public static void initialize()  throws Exception
    {
        connStr = TestContext.getConnectionString();
    }

    @Test()
    public void VerifyTaskQueueEmptyOnMsgFactoryGracefulClose() throws Exception    {

        final LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 1, TimeUnit.MINUTES, blockingQueue);
        try {
            final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(
                    TestContext.getConnectionString().toString(),
                    executor);

            final PartitionReceiver receiver = ehClient.createReceiverSync(
                    TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
            final PartitionSender sender = ehClient.createPartitionSenderSync(PARTITION_ID);
            sender.sendSync(new EventData("test data - string".getBytes()));
            Iterable<EventData> events = receiver.receiveSync(10);

            Assert.assertTrue(events.iterator().hasNext());
            sender.closeSync();
            receiver.closeSync();

            ehClient.closeSync();

            Assert.assertEquals(blockingQueue.size(), 0);
            Assert.assertEquals(executor.getTaskCount(), executor.getCompletedTaskCount());
        } finally {
            executor.shutdown();
        }
    }

    @Test()
    public void VerifyThreadReleaseOnMsgFactoryOpenError() throws Exception    {

        final FaultInjectingReactorFactory networkOutageSimulator = new FaultInjectingReactorFactory();
        networkOutageSimulator.setFaultType(FaultInjectingReactorFactory.FaultType.NetworkOutage);

        final LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 1, TimeUnit.MINUTES, blockingQueue);

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

            Assert.assertEquals(0, blockingQueue.size());
            Assert.assertEquals(executor.getTaskCount(), executor.getCompletedTaskCount());
        } finally {
            executor.shutdown();
        }
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToEventHubClient() throws Exception {
        final ExecutorService testClosed = Executors.newWorkStealingPool();
        testClosed.shutdown();

        EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);
    }

    @Test(expected = RejectedExecutionException.class)
    public void SupplyClosedExecutorServiceToSendOperation() throws Exception {
        final ExecutorService testClosed = Executors.newWorkStealingPool();

        final EventHubClient temp = EventHubClient.createFromConnectionStringSync(
                TestContext.getConnectionString().toString(),
                testClosed);
        temp.sendSync(new EventData("test data - string".getBytes()));

        testClosed.shutdown();

        temp.sendSync(new EventData("test data - string".getBytes()));
    }
}
