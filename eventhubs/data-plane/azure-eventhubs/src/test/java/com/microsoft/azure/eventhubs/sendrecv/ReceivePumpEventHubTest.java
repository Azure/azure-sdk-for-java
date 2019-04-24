// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReceivePumpEventHubTest extends ApiTestBase {
    private static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();
    private static final String PARTITION_ID = "0";

    static EventHubClient ehClient;

    PartitionReceiver receiver;

    @BeforeClass
    public static void initializeEventHub() throws EventHubException, IOException {
        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }

    @Before
    public void initializeTest() throws EventHubException {
        receiver = ehClient.createReceiverSync(CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
    }

    @Test(expected = TimeoutException.class)
    public void testInvokeOnTimeoutKnobDefault() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal));
        invokeSignal.get(3, TimeUnit.SECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void testInvokeOnTimeoutKnobFalse() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), false);
        invokeSignal.get(3, TimeUnit.SECONDS);
    }

    @Test()
    public void testInvokeOnTimeoutKnobTrue() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
        invokeSignal.get(3, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvokeWithInvalidArgs() throws Throwable {
        final CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal, PartitionReceiver.DEFAULT_PREFETCH_COUNT + 1), true);
        try {
            invokeSignal.get(3, TimeUnit.SECONDS);
        } catch (ExecutionException executionException) {
            throw executionException.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetReceiveHandlerMultipleTimes() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);

        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
    }

    @Test()
    public void testGraceFullCloseReceivePump() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveTimeout(Duration.ofSeconds(1));
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);

        receiver.setReceiveHandler(null).get();

        invokeSignal = new CompletableFuture<Void>();
        receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
        invokeSignal.get(3, TimeUnit.SECONDS);
    }

    @After
    public void cleanupTest() throws EventHubException {
        if (receiver != null) {
            receiver.closeSync();
        }
    }

    public static final class InvokeOnReceiveEventValidator implements PartitionReceiveHandler {
        final CompletableFuture<Void> signalInvoked;
        final int maxEventCount;

        public InvokeOnReceiveEventValidator(final CompletableFuture<Void> signalInvoked) {
            this(signalInvoked, 50);
        }

        public InvokeOnReceiveEventValidator(final CompletableFuture<Void> signalInvoked, final int maxEventCount) {
            this.signalInvoked = signalInvoked;
            this.maxEventCount = maxEventCount;
        }

        @Override
        public int getMaxEventCount() {
            return this.maxEventCount;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
            this.signalInvoked.complete(null);
        }

        @Override
        public void onError(Throwable error) {
            this.signalInvoked.completeExceptionally(error);
        }
    }
}
