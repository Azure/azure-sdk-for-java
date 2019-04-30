// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class ReceivePump implements Runnable {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReceivePump.class);

    private final IPartitionReceiver receiver;
    private final PartitionReceiveHandler onReceiveHandler;
    private final boolean invokeOnTimeout;
    private final CompletableFuture<Void> stopPump;
    private final Executor executor;
    private final ProcessAndReschedule processAndReschedule;
    private final String eventHubName;
    private final String consumerGroupName;

    private AtomicBoolean stopPumpRaised;
    private volatile boolean isPumpHealthy = true;

    public ReceivePump(
            final String eventHubName,
            final String consumerGroupName,
            final IPartitionReceiver receiver,
            final PartitionReceiveHandler receiveHandler,
            final boolean invokeOnReceiveWithNoEvents,
            final Executor executor) {
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
        this.receiver = receiver;
        this.onReceiveHandler = receiveHandler;
        this.invokeOnTimeout = invokeOnReceiveWithNoEvents;
        this.stopPump = new CompletableFuture<>();
        this.executor = executor;
        this.processAndReschedule = new ProcessAndReschedule();

        this.stopPumpRaised = new AtomicBoolean(false);
    }

    // entry-point - for runnable
    public void run() {
        try {
            ReceivePump.this.receiveAndProcess();
        } catch (final Exception exception) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error(
                        String.format(Locale.US, "Receive pump for eventHub (%s), consumerGroup (%s), partition (%s) "
                                + "encountered unrecoverable error and exited with exception %s.",
                                this.eventHubName, this.consumerGroupName, this.receiver.getPartitionId(), exception.toString()));
            }

            throw exception;
        }
    }

    // receives and invokes user-callback if success or stops pump if fails
    public void receiveAndProcess() {
        if (this.shouldContinue()) {
            this.receiver.receive(this.onReceiveHandler.getMaxEventCount())
                    .handleAsync(this.processAndReschedule, this.executor);
        } else {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "Stopping receive pump for eventHub (%s), consumerGroup (%s), partition (%s) as %s",
                        this.eventHubName, this.consumerGroupName, this.receiver.getPartitionId(),
                        this.stopPumpRaised.get() ? "per the request." : "pump ran into errors."));
            }

            this.stopPump.complete(null);
        }
    }

    public CompletableFuture<Void> stop() {
        this.stopPumpRaised.set(true);
        return this.stopPump;
    }

    public boolean isRunning() {
        return !this.stopPump.isDone();
    }

    private boolean shouldContinue() {
        return this.isPumpHealthy && !this.stopPumpRaised.get();
    }

    private void handleClientExceptions(final Throwable clientException) {
        if (clientException != null) {
            this.isPumpHealthy = false;

            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn(String.format(
                        "Receive pump for eventHub (%s), consumerGroup (%s), partition (%s) exiting after receive exception %s",
                        this.eventHubName, this.consumerGroupName, this.receiver.getPartitionId(), clientException.toString()));
            }

            this.onReceiveHandler.onError(clientException);
        }
    }

    private void handleUserCodeExceptions(final Throwable userCodeException) {
        this.isPumpHealthy = false;
        if (TRACE_LOGGER.isErrorEnabled()) {
            TRACE_LOGGER.error(
                    String.format(Locale.US, "Receive pump for eventHub (%s), consumerGroup (%s), partition (%s) "
                                    + "exiting after user-code exception %s",
                            this.eventHubName, this.consumerGroupName, this.receiver.getPartitionId(), userCodeException.toString()));
        }

        this.onReceiveHandler.onError(userCodeException);

        if (userCodeException instanceof InterruptedException) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info(String.format(Locale.US, "Interrupting receive pump for eventHub (%s), consumerGroup (%s),  partition (%s)",
                        this.eventHubName, this.consumerGroupName, this.receiver.getPartitionId()));
            }

            Thread.currentThread().interrupt();
        }
    }

    private void schedulePump() {
        try {
            this.executor.execute(this);
        } catch (final RejectedExecutionException rejectedException) {
            this.isPumpHealthy = false;

            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn(String.format(
                        "Receive pump for eventHub (%s), consumerGroup (%s), partition (%s) exiting with error: %s",
                        this.eventHubName, this.consumerGroupName, ReceivePump.this.receiver.getPartitionId(), rejectedException.toString()));
            }

            this.onReceiveHandler.onError(rejectedException);
        }
    }

    // partition receiver contract against which this pump works
    public interface IPartitionReceiver {
        String getPartitionId();

        CompletableFuture<Iterable<EventData>> receive(int maxBatchSize);
    }

    private final class ProcessAndReschedule implements BiFunction<Iterable<EventData>, Throwable, Void> {

        @Override
        public Void apply(final Iterable<EventData> receivedEvents, final Throwable clientException) {

            ReceivePump.this.handleClientExceptions(clientException);

            try {
                // don't invoke user call back - if stop is already raised / pump is unhealthy
                if (ReceivePump.this.shouldContinue()
                        && (receivedEvents != null
                                || ReceivePump.this.invokeOnTimeout)) {
                    ReceivePump.this.onReceiveHandler.onReceive(receivedEvents);
                }
            } catch (final Throwable userCodeError) {
                ReceivePump.this.handleUserCodeExceptions(userCodeError);
            }

            ReceivePump.this.schedulePump();

            return null;
        }
    }
}
