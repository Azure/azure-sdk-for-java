// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverDisconnectedException;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class PartitionPump extends Closable implements PartitionReceiveHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionPump.class);
    protected final HostContext hostContext;
    protected final CompleteLease lease; // protected for testability
    private final CompletableFuture<Void> shutdownTriggerFuture;
    private final CompletableFuture<Void> shutdownFinishedFuture;
    private final Object processingSynchronizer;
    private final Consumer<String> pumpManagerCallback;
    private EventHubClient eventHubClient = null;
    private PartitionReceiver partitionReceiver = null;
    private CloseReason shutdownReason;
    private volatile CompletableFuture<?> internalOperationFuture = null;
    private IEventProcessor processor = null;
    private PartitionContext partitionContext = null;
    private ScheduledFuture<?> leaseRenewerFuture = null;

    PartitionPump(HostContext hostContext, CompleteLease lease, Closable parent, Consumer<String> pumpManagerCallback) {
        super(parent);

        this.hostContext = hostContext;
        this.lease = lease;
        this.pumpManagerCallback = pumpManagerCallback;
        this.processingSynchronizer = new Object();

        this.partitionContext = new PartitionContext(this.hostContext, this.lease.getPartitionId());
        this.partitionContext.setLease(this.lease);

        // Set up the shutdown futures. The shutdown process can be triggered just by completing this.shutdownFuture.
        this.shutdownTriggerFuture = new CompletableFuture<Void>();
        this.shutdownFinishedFuture = this.shutdownTriggerFuture
                .handleAsync((r, e) -> {
                    this.pumpManagerCallback.accept(this.lease.getPartitionId());
                    return cancelPendingOperations();
                }, this.hostContext.getExecutor())
                .thenCompose((empty) -> cleanUpAll(this.shutdownReason))
                .thenCompose((empty) -> releaseLeaseOnShutdown())
                .whenCompleteAsync((empty, e) -> {
                    setClosed();
                }, this.hostContext.getExecutor());
    }

    // The CompletableFuture returned by startPump remains uncompleted as long as the pump is running.
    // If startup fails, or an error occurs while running, it will complete exceptionally.
    // If clean shutdown due to unregister call, it completes normally.
    CompletableFuture<Void> startPump() {
        // Do the slow startup stuff asynchronously.
        // Use whenComplete to trigger cleanup on exception.
        CompletableFuture.runAsync(() -> openProcessor(), this.hostContext.getExecutor())
                .thenCompose((empty) -> openClientsRetryWrapper())
                .thenRunAsync(() -> scheduleLeaseRenewer(), this.hostContext.getExecutor())
                .whenCompleteAsync((r, e) -> {
                    if (e != null) {
                        // If startup failed, trigger shutdown to clean up.
                        internalShutdown(CloseReason.Shutdown, e);
                    }
                }, this.hostContext.getExecutor());

        return shutdownFinishedFuture;
    }

    private void openProcessor() {
        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "Creating and opening event processor instance"));

        String action = EventProcessorHostActionStrings.CREATING_EVENT_PROCESSOR;
        try {
            this.processor = this.hostContext.getEventProcessorFactory().createEventProcessor(this.partitionContext);
            action = EventProcessorHostActionStrings.OPENING_EVENT_PROCESSOR;
            this.processor.onOpen(this.partitionContext);
        } catch (Exception e) {
            // If the processor won't create or open, only thing we can do here is pass the buck.
            // Null it out so we don't try to operate on it further.
            this.processor = null;
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext, "Failed " + action), e);
            this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), e, action, this.lease.getPartitionId());
            throw new CompletionException(e);
        }
    }

    private CompletableFuture<Void> openClientsRetryWrapper() {
        // Stage 0: first attempt
        CompletableFuture<Boolean> retryResult = openClients();

        for (int i = 1; i < 5; i++) {
            retryResult = retryResult
                    // Stages 1, 3, 5, etc: trace errors but stop exception propagation in order to keep going
                    // UNLESS it's ReceiverDisconnectedException.
                    .handleAsync((r, e) -> {
                        if (e != null) {
                            Exception notifyWith = (Exception) LoggingUtils.unwrapException(e, null);
                            if (notifyWith instanceof ReceiverDisconnectedException) {
                                // TODO: Assuming this is due to a receiver with a higher epoch.
                                // Is there a way to be sure without checking the exception text?
                                // DO NOT trace here because then we could get multiple traces for the same exception.
                                // If it's a bad epoch, then retrying isn't going to help.
                                // Rethrow to keep propagating error to the end and prevent any more attempts.
                                throw new CompletionException(notifyWith);
                            } else {
                                TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                                        "Failure creating client or receiver, retrying"), e);
                            }
                        }
                        // If we have a valid result, pass it along to prevent further attempts.
                        return (e == null) ? r : false;
                    }, this.hostContext.getExecutor())
                    // Stages 2, 4, 6, etc: make another attempt if needed.
                    .thenCompose((done) -> {
                        return done ? CompletableFuture.completedFuture(done) : openClients();
                    });
        }
        // Stage final: on success, hook up the user's event handler to start receiving events. On error,
        // trace exceptions from the final attempt, or ReceiverDisconnectedException.
        return retryResult.handleAsync((r, e) -> {
            if (e == null) {
                // IEventProcessor.onOpen is called from the base PartitionPump and must have returned in order for execution to reach here,
                // meaning it is safe to set the handler and start calling IEventProcessor.onEvents.
                this.partitionReceiver.setReceiveHandler(this, this.hostContext.getEventProcessorOptions().getInvokeProcessorAfterReceiveTimeout());
            } else {
                Exception notifyWith = (Exception) LoggingUtils.unwrapException(e, null);
                if (notifyWith instanceof ReceiverDisconnectedException) {
                    // TODO: Assuming this is due to a receiver with a higher epoch.
                    // Is there a way to be sure without checking the exception text?
                    TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                            "Receiver disconnected on create, bad epoch?"), notifyWith);
                } else {
                    TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                            "Failure creating client or receiver, out of retries"), e);
                }

                // IEventProcessor.onOpen is called from the base PartitionPump and must have returned in order for execution to reach here,
                // so we can report this error to it instead of the general error handler.
                this.processor.onError(this.partitionContext, new ExceptionWithAction(notifyWith, EventProcessorHostActionStrings.CREATING_EVENT_HUB_CLIENT));

                // Rethrow so caller will see failure
                throw LoggingUtils.wrapException(notifyWith, EventProcessorHostActionStrings.CREATING_EVENT_HUB_CLIENT);
            }
            return null;
        }, this.hostContext.getExecutor());
    }

    protected void scheduleLeaseRenewer() {
        if (!getIsClosingOrClosed()) {
            int seconds = this.hostContext.getPartitionManagerOptions().getLeaseRenewIntervalInSeconds();
            this.leaseRenewerFuture = this.hostContext.getExecutor().schedule(() -> leaseRenewer(), seconds, TimeUnit.SECONDS);
            TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(this.lease, "scheduling leaseRenewer in " + seconds));
        }
    }

    private CompletableFuture<Boolean> openClients() {
        // Create new client
        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "Opening EH client"));

        CompletableFuture<EventHubClient> startOpeningFuture = null;
        try {
            startOpeningFuture = this.hostContext.getEventHubClientFactory().createEventHubClient();
        } catch (EventHubException | IOException e2) {
            // Marking startOpeningFuture as completed exceptionally will cause all the
            // following stages to fall through except stage 1 which will report the error.
            startOpeningFuture = new CompletableFuture<EventHubClient>();
            startOpeningFuture.completeExceptionally(e2);
        }
        this.internalOperationFuture = startOpeningFuture;

        // Stage 0: get EventHubClient
        return startOpeningFuture
                // Stage 1: save EventHubClient on success, trace on error
                .whenCompleteAsync((ehclient, e) -> {
                    if ((ehclient != null) && (e == null)) {
                        this.eventHubClient = ehclient;
                    } else {
                        TRACE_LOGGER.error(this.hostContext.withHostAndPartition(this.partitionContext, "EventHubClient creation failed"), e);
                    }
                    // this.internalOperationFuture allows canceling startup if it gets stuck. Null out now that EventHubClient creation has completed.
                    this.internalOperationFuture = null;
                }, this.hostContext.getExecutor())
                // Stage 2: get initial offset for receiver
                .thenCompose((empty) -> this.partitionContext.getInitialOffset())
                // Stage 3: set up other receiver options, create receiver if initial offset is valid
                .thenCompose((startAt) -> {
                    long epoch = this.lease.getEpoch();

                    TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext,
                            "Opening EH receiver with epoch " + epoch + " at location " + startAt));

                    CompletableFuture<PartitionReceiver> receiverFuture = null;

                    try {
                        ReceiverOptions options = new ReceiverOptions();
                        options.setReceiverRuntimeMetricEnabled(this.hostContext.getEventProcessorOptions().getReceiverRuntimeMetricEnabled());
                        options.setPrefetchCount(this.hostContext.getEventProcessorOptions().getPrefetchCount());

                        receiverFuture = this.eventHubClient.createEpochReceiver(this.partitionContext.getConsumerGroupName(),
                                this.partitionContext.getPartitionId(), startAt, epoch, options);
                        this.internalOperationFuture = receiverFuture;
                    } catch (EventHubException e) {
                        TRACE_LOGGER.error(this.hostContext.withHostAndPartition(this.partitionContext, "Opening EH receiver failed with an error "), e);
                        receiverFuture = new CompletableFuture<PartitionReceiver>();
                        receiverFuture.completeExceptionally(e);
                    }

                    return receiverFuture;
                })
                // Stage 4: save PartitionReceiver on success, trace on error
                .whenCompleteAsync((receiver, e) -> {
                    if ((receiver != null) && (e == null)) {
                        this.partitionReceiver = receiver;
                    } else if (this.eventHubClient != null) {
                        if (e instanceof ReceiverDisconnectedException) {
                            TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "PartitionReceiver disconnected during startup"));
                        } else {
                            TRACE_LOGGER.error(this.hostContext.withHostAndPartition(this.partitionContext, "PartitionReceiver creation failed"), e);
                        }
                    }
                    // else if this.eventHubClient is null then we failed in stage 0 and already traced in stage 1

                    // this.internalOperationFuture allows canceling startup if it gets stuck. Null out now that PartitionReceiver creation has completed.
                    this.internalOperationFuture = null;
                }, this.hostContext.getExecutor())
                // Stage 5: on success, set up the receiver
                .thenApplyAsync((receiver) -> {
                    this.partitionReceiver.setReceiveTimeout(this.hostContext.getEventProcessorOptions().getReceiveTimeOut());

                    TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext,
                            "EH client and receiver creation finished"));

                    return true;
                }, this.hostContext.getExecutor());
    }

    private CompletableFuture<Void> cleanUpAll(CloseReason reason) { // swallows all exceptions
        return cleanUpClients()
                .thenRunAsync(() -> {
                    if (this.processor != null) {
                        try {
                            synchronized (this.processingSynchronizer) {
                                // When we take the lock, any existing onEvents call has finished.
                                // Because the client has been closed, there will not be any more
                                // calls to onEvents in the future. Therefore we can safely call onClose.
                                this.processor.onClose(this.partitionContext, reason);
                            }
                        } catch (Exception e) {
                            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                                    "Failure closing processor"), e);
                            // If closing the processor has failed, the state of the processor is suspect.
                            // Report the failure to the general error handler instead.
                            this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), e, EventProcessorHostActionStrings.CLOSING_EVENT_PROCESSOR,
                                    this.lease.getPartitionId());
                        }
                    }
                }, this.hostContext.getExecutor());
    }

    private CompletableFuture<Void> cleanUpClients() { // swallows all exceptions
        CompletableFuture<Void> cleanupFuture = null;
        if (this.partitionReceiver != null) {
            // Disconnect the processor from the receiver we're about to close.
            // Fortunately this is idempotent -- setting the handler to null when it's already been
            // nulled by code elsewhere is harmless!
            // Setting to null also waits for the in-progress calls to complete
            TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "Setting receive handler to null"));
            cleanupFuture = this.partitionReceiver.setReceiveHandler(null);
        } else {
            TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(this.partitionContext, "partitionReceiver is null in cleanup"));
            cleanupFuture = CompletableFuture.completedFuture(null);
        }
        cleanupFuture = cleanupFuture.handleAsync((empty, e) -> {
            if (e != null) {
                TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                        "Got exception when ReceiveHandler is set to null."), LoggingUtils.unwrapException(e, null));
            }
            return null; // stop propagation of exceptions
        }, this.hostContext.getExecutor())
                .thenApplyAsync((empty) -> {
                    TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "Closing EH receiver"));
                    PartitionReceiver partitionReceiverTemp = this.partitionReceiver;
                    this.partitionReceiver = null;
                    return partitionReceiverTemp;
                }, this.hostContext.getExecutor())
                .thenCompose((partitionReceiverTemp) -> {
                    return (partitionReceiverTemp != null) ? partitionReceiverTemp.close() : CompletableFuture.completedFuture(null);
                })
                .handleAsync((empty, e) -> {
                    if (e != null) {
                        TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                                "Closing EH receiver failed."), LoggingUtils.unwrapException(e, null));
                    }
                    return null; // stop propagation of exceptions
                }, this.hostContext.getExecutor())
                .thenApplyAsync((empty) -> {
                    TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext, "Closing EH client"));
                    final EventHubClient eventHubClientTemp = this.eventHubClient;
                    this.eventHubClient = null;
                    if (eventHubClientTemp == null) {
                        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(this.partitionContext,
                                "eventHubClient is null in cleanup"));
                    }
                    return eventHubClientTemp;
                }, this.hostContext.getExecutor())
                .thenCompose((eventHubClientTemp) -> {
                    return (eventHubClientTemp != null) ? eventHubClientTemp.close() : CompletableFuture.completedFuture(null);
                })
                .handleAsync((empty, e) -> {
                    if (e != null) {
                        TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext, "Closing EH client failed."),
                                LoggingUtils.unwrapException(e, null));
                    }
                    return null; // stop propagation of exceptions
                }, this.hostContext.getExecutor());

        return cleanupFuture;
    }

    protected Void cancelPendingOperations() {
        // If an open operation is stuck, this lets us shut down anyway.
        CompletableFuture<?> captured = this.internalOperationFuture;
        if (captured != null) {
            captured.cancel(true);
        }

        ScheduledFuture<?> capturedLeaseRenewer = this.leaseRenewerFuture;
        if (capturedLeaseRenewer != null) {
            capturedLeaseRenewer.cancel(true);
        }
        return null;
    }

    private CompletableFuture<Void> releaseLeaseOnShutdown() { // swallows all exceptions
        CompletableFuture<Void> result = CompletableFuture.completedFuture(null);

        if (this.shutdownReason != CloseReason.LeaseLost) {
            // Since this pump is dead, release the lease. Don't care about any errors that may occur. Worst case is
            // that the lease eventually expires, since the lease renewer has been cancelled.
            result = PartitionPump.this.hostContext.getLeaseManager().releaseLease(this.lease)
                    .handleAsync((empty, e) -> {
                        if (e != null) {
                            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                                    "Failure releasing lease on pump shutdown"), LoggingUtils.unwrapException(e, null));
                        }
                        return null; // stop propagation of exceptions
                    }, this.hostContext.getExecutor());
        }
        // else we already lost the lease, releasing is unnecessary and would fail if we try

        return result;
    }

    protected void internalShutdown(CloseReason reason, Throwable e) {
        setClosing();

        this.shutdownReason = reason;
        if (e == null) {
            this.shutdownTriggerFuture.complete(null);
        } else {
            this.shutdownTriggerFuture.completeExceptionally(e);
        }
    }

    CompletableFuture<Void> shutdown(CloseReason reason) {
        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext,
                "pump shutdown for reason " + reason.toString()));
        internalShutdown(reason, null);
        return this.shutdownFinishedFuture;
    }

    private void leaseRenewer() {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(this.lease, "leaseRenewer()"));

        // Theoretically, if the future is cancelled then this method should never fire, but
        // there's no harm in being sure.
        if (this.leaseRenewerFuture.isCancelled()) {
            return;
        }
        if (getIsClosingOrClosed()) {
            return;
        }

        // Stage 0: renew the lease
        this.hostContext.getLeaseManager().renewLease(this.lease)
                // Stage 1: check result of renewing
                .thenApplyAsync((renewed) -> {
                    Boolean scheduleNext = true;
                    if (!renewed) {
                        // False return from renewLease means that lease was lost.
                        // Start pump shutdown process and do not schedule another call to leaseRenewer.
                        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.lease, "Lease lost, shutting down pump"));
                        internalShutdown(CloseReason.LeaseLost, null);
                        scheduleNext = false;
                    }
                    return scheduleNext;
                }, this.hostContext.getExecutor())
                // Stage 2: RUN REGARDLESS OF EXCEPTIONS -- trace exceptions, schedule next iteration
                .whenCompleteAsync((scheduleNext, e) -> {
                    if (e != null) {
                        // Failure renewing lease due to storage exception or whatever.
                        // Trace error and leave scheduleNext as true to schedule another try.
                        Exception notifyWith = (Exception) LoggingUtils.unwrapException(e, null);
                        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.lease, "Transient failure renewing lease"), notifyWith);
                        // Notify the general error handler rather than calling this.processor.onError so we can provide context (RENEWING_LEASE)
                        this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), notifyWith, EventProcessorHostActionStrings.RENEWING_LEASE,
                                this.lease.getPartitionId());
                    }

                    if ((scheduleNext != null) && scheduleNext.booleanValue() && !this.leaseRenewerFuture.isCancelled() && !getIsClosingOrClosed()) {
                        scheduleLeaseRenewer();
                    }
                }, this.hostContext.getExecutor());
    }

    @Override
    public int getMaxEventCount() {
        return this.hostContext.getEventProcessorOptions().getMaxBatchSize();
    }

    @Override
    public void onReceive(Iterable<EventData> events) {
        if (this.hostContext.getEventProcessorOptions().getReceiverRuntimeMetricEnabled()) {
            this.partitionContext.setRuntimeInformation(this.partitionReceiver.getRuntimeInformation());
        }

        // This method is called on the thread that the Java EH client uses to run the pump.
        // There is one pump per EventHubClient. Since each PartitionPump creates a new EventHubClient,
        // using that thread to call onEvents does no harm. Even if onEvents is slow, the pump will
        // get control back each time onEvents returns, and be able to receive a new batch of events
        // with which to make the next onEvents call. The pump gains nothing by running faster than onEvents.

        // The underlying client returns null if there are no events, but the contract for IEventProcessor
        // is different and is expecting an empty iterable if there are no events (and invoke processor after
        // receive timeout is turned on).

        Iterable<EventData> effectiveEvents = events;
        if (effectiveEvents == null) {
            effectiveEvents = new ArrayList<EventData>();
        }

        // Update offset and sequence number in the PartitionContext to support argument-less overload of PartitionContext.checkpoint()
        Iterator<EventData> iter = effectiveEvents.iterator();
        EventData last = null;
        while (iter.hasNext()) {
            last = iter.next();
        }
        if (last != null) {
            this.partitionContext.setOffsetAndSequenceNumber(last);
        }

        try {
            // Synchronize to serialize calls to the processor.
            // The handler is not installed until after onOpen returns, so onEvents cannot overlap with onOpen.
            // onEvents and onClose are synchronized via this.processingSynchronizer to prevent calls to onClose
            // while an onEvents call is still in progress.
            synchronized (this.processingSynchronizer) {
                this.processor.onEvents(this.partitionContext, effectiveEvents);
            }
        } catch (Exception e) {
            // TODO: do we pass errors from IEventProcessor.onEvents to IEventProcessor.onError?
            // Depending on how you look at it, that's either pointless (if the user's code throws, the user's code should already know about it) or
            // a convenient way of centralizing error handling.
            // In the meantime, just trace it.
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext,
                    "Got exception from onEvents"), e);
        }
    }

    @Override
    public void onError(Throwable error) {
        if (error == null) {
            error = new Throwable("No error info supplied by EventHub client");
        }
        if (error instanceof ReceiverDisconnectedException) {
            TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionContext,
                    "EventHub client disconnected, probably another host took the partition"));
        } else {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext, "EventHub client error: " + error.toString()));
            if (error instanceof Exception) {
                TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(this.partitionContext, "EventHub client error continued"), (Exception) error);
            }
        }

        // It is vital to perform the rest of cleanup in a separate thread and not block this one. This thread is the client's
        // receive pump thread, and blocking it means that the receive pump never completes its CompletableFuture, which in turn
        // blocks other client calls that we would like to make during cleanup. Specifically, this issue was found when
        // PartitionReceiver.setReceiveHandler(null).get() was called and never returned.
        final Throwable capturedError = error;
        CompletableFuture.runAsync(() -> PartitionPump.this.processor.onError(PartitionPump.this.partitionContext, capturedError), this.hostContext.getExecutor())
                .thenRunAsync(() -> internalShutdown(CloseReason.Shutdown, capturedError), this.hostContext.getExecutor());
    }
}
