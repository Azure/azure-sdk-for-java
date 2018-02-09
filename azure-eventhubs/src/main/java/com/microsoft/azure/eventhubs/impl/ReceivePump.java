/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceivePump {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReceivePump.class);

    private final IPartitionReceiver receiver;
    private final PartitionReceiveHandler onReceiveHandler;
    private final boolean invokeOnTimeout;
    private final CompletableFuture<Void> stopPump;

    private AtomicBoolean stopPumpRaised;

    public ReceivePump(
            final IPartitionReceiver receiver,
            final PartitionReceiveHandler receiveHandler,
            final boolean invokeOnReceiveWithNoEvents) {
        this.receiver = receiver;
        this.onReceiveHandler = receiveHandler;
        this.invokeOnTimeout = invokeOnReceiveWithNoEvents;
        this.stopPump = new CompletableFuture<Void>();

        this.stopPumpRaised = new AtomicBoolean(false);
    }

    public void run() {
        boolean isPumpHealthy = true;
        while (isPumpHealthy && !this.stopPumpRaised.get()) {
            Iterable<? extends EventData> receivedEvents = null;

            try {
                receivedEvents = this.receiver.receive(this.onReceiveHandler.getMaxEventCount());
            } catch (Throwable clientException) {
                isPumpHealthy = false;
                this.onReceiveHandler.onError(clientException);

                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(String.format("Receive pump for partition (%s) exiting after receive exception %s", this.receiver.getPartitionId(), clientException.toString()));
                }
            }

            try {
                if (receivedEvents != null || (receivedEvents == null && this.invokeOnTimeout && isPumpHealthy)) {
                    this.onReceiveHandler.onReceive(receivedEvents);
                }
            } catch (Throwable userCodeError) {
                isPumpHealthy = false;
                this.onReceiveHandler.onError(userCodeError);

                if (userCodeError instanceof InterruptedException) {
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info(String.format("Interrupting receive pump for partition (%s)", this.receiver.getPartitionId()));
                    }

                    Thread.currentThread().interrupt();
                } else {
                    TRACE_LOGGER.error(
                            String.format("Receive pump for partition (%s) exiting after user exception %s", this.receiver.getPartitionId(), userCodeError.toString()));
                }
            }
        }

        this.stopPump.complete(null);
    }

    public CompletableFuture<Void> stop() {
        this.stopPumpRaised.set(true);
        return this.stopPump;
    }

    public boolean isRunning() {
        return !this.stopPump.isDone();
    }

    // partition receiver contract against which this pump works
    public static interface IPartitionReceiver {
        public String getPartitionId();

        public Iterable<? extends EventData> receive(final int maxBatchSize) throws EventHubException;
    }
}
