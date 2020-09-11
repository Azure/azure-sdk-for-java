// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class PartitionManager extends Closable {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionManager.class);
    // Protected instead of private for testability
    protected final HostContext hostContext;
    private final Object scanFutureSynchronizer = new Object();
    private final int retryMax = 5;
    protected PumpManager pumpManager = null;
    protected volatile String[] partitionIds = null;
    private ScheduledFuture<?> scanFuture = null;

    PartitionManager(HostContext hostContext) {
        super(null);
        this.hostContext = hostContext;
    }

    CompletableFuture<Void> cachePartitionIds() {
        CompletableFuture<Void> retval = null;

        if (this.partitionIds != null) {
            retval = CompletableFuture.completedFuture(null);
        } else {
            // This try-catch is necessary because EventHubClient.create can directly throw
            // EventHubException or IOException, in addition to whatever failures may occur when the result of
            // the CompletableFuture is evaluated.
            try {
                final CompletableFuture<Void> cleanupFuture = new CompletableFuture<Void>();

                // Stage 0A: get EventHubClient for the event hub
                retval = this.hostContext.getEventHubClientFactory().createEventHubClient()
                        // Stage 0B: set up a way to close the EventHubClient when we're done
                        .thenApplyAsync((ehClient) -> {
                            final EventHubClient saveForCleanupClient = ehClient;
                            cleanupFuture.thenCompose((empty) -> saveForCleanupClient.close());
                            return ehClient;
                        }, this.hostContext.getExecutor())
                        // Stage 1: use the client to get runtime info for the event hub
                        .thenCompose((ehClient) -> ehClient.getRuntimeInformation())
                        // Stage 2: extract the partition ids from the runtime info or throw on null (timeout)
                        .thenAcceptAsync((EventHubRuntimeInformation ehInfo) -> {
                            if (ehInfo != null) {
                                this.partitionIds = ehInfo.getPartitionIds();

                                TRACE_LOGGER.info(this.hostContext.withHost("Eventhub " + this.hostContext.getEventHubPath() + " count of partitions: " + ehInfo.getPartitionCount()));
                                for (String id : this.partitionIds) {
                                    TRACE_LOGGER.info(this.hostContext.withHost("Found partition with id: " + id));
                                }
                            } else {
                                throw new CompletionException(new TimeoutException("getRuntimeInformation returned null"));
                            }
                        }, this.hostContext.getExecutor())
                        // Stage 3: RUN REGARDLESS OF EXCEPTIONS -- if there was an error, wrap it in IllegalEntityException and throw
                        .handleAsync((empty, e) -> {
                            cleanupFuture.complete(null); // trigger client cleanup
                            if (e != null) {
                                Throwable notifyWith = e;
                                if (e instanceof CompletionException) {
                                    notifyWith = e.getCause();
                                }
                                throw new CompletionException(new IllegalEntityException("Failure getting partition ids for event hub", notifyWith));
                            }
                            return null;
                        }, this.hostContext.getExecutor());
            } catch (EventHubException | IOException e) {
                retval = new CompletableFuture<Void>();
                retval.completeExceptionally(new IllegalEntityException("Failure getting partition ids for event hub", e));
            }
        }

        return retval;
    }

    // Testability hook: allows a test subclass to insert dummy pump.
    PumpManager createPumpTestHook() {
        return new PumpManager(this.hostContext, this);
    }

    // Testability hook: called after stores are initialized.
    void onInitializeCompleteTestHook() {
    }

    // Testability hook: called at the end of the main loop after all partition checks/stealing is complete.
    void onPartitionCheckCompleteTestHook() {
    }

    CompletableFuture<Void> stopPartitions() {
        setClosing();

        // If the lease scanner is between runs, cancel so it doesn't run again.
        synchronized (this.scanFutureSynchronizer) {
            if (this.scanFuture != null) {
                this.scanFuture.cancel(true);
            }
        }

        // Stop any partition pumps that are running.
        CompletableFuture<Void> stopping = CompletableFuture.completedFuture(null);

        if (this.pumpManager != null) {
            TRACE_LOGGER.info(this.hostContext.withHost("Shutting down all pumps"));
            stopping = this.pumpManager.removeAllPumps(CloseReason.Shutdown)
                    .whenCompleteAsync((empty, e) -> {
                        if (e != null) {
                            Throwable notifyWith = LoggingUtils.unwrapException(e, null);
                            TRACE_LOGGER.warn(this.hostContext.withHost("Failure during shutdown"), notifyWith);
                            if (notifyWith instanceof Exception) {
                                this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), (Exception) notifyWith,
                                        EventProcessorHostActionStrings.PARTITION_MANAGER_CLEANUP);

                            }
                        }
                    }, this.hostContext.getExecutor());
        }
        // else no pumps to shut down

        stopping = stopping.whenCompleteAsync((empty, e) -> {
            TRACE_LOGGER.info(this.hostContext.withHost("Partition manager exiting"));
            setClosed();
        }, this.hostContext.getExecutor());

        return stopping;
    }

    public CompletableFuture<Void> initialize() {
        this.pumpManager = createPumpTestHook();

        // Stage 0: get partition ids and cache
        return cachePartitionIds()
                // Stage 1: initialize stores, if stage 0 succeeded
                .thenCompose((unused) -> initializeStores())
                // Stage 2: RUN REGARDLESS OF EXCEPTIONS -- trace errors
                .whenCompleteAsync((empty, e) -> {
                    if (e != null) {
                        StringBuilder outAction = new StringBuilder();
                        Throwable notifyWith = LoggingUtils.unwrapException(e, outAction);
                        if (outAction.length() > 0) {
                            TRACE_LOGGER.error(this.hostContext.withHost(
                                    "Exception while initializing stores (" + outAction.toString() + "), not starting partition manager"), notifyWith);
                        } else {
                            TRACE_LOGGER.error(this.hostContext.withHost("Exception while initializing stores, not starting partition manager"), notifyWith);
                        }
                    }
                }, this.hostContext.getExecutor())
                // Stage 3: schedule scan, which will find partitions and start pumps, if previous stages succeeded
                .thenRunAsync(() -> {
                    // Schedule the first scan immediately.
                    synchronized (this.scanFutureSynchronizer) {
                        TRACE_LOGGER.debug(this.hostContext.withHost("Scheduling lease scanner first pass"));
                        this.scanFuture = this.hostContext.getExecutor().schedule(() -> scan(true), 0, TimeUnit.SECONDS);
                    }

                    onInitializeCompleteTestHook();
                }, this.hostContext.getExecutor());
    }

    private CompletableFuture<?> initializeStores() {
        ILeaseManager leaseManager = this.hostContext.getLeaseManager();
        ICheckpointManager checkpointManager = this.hostContext.getCheckpointManager();

        // let R = this.retryMax
        // Stages 0 to R: create lease store if it doesn't exist
        CompletableFuture<?> initializeStoresFuture = buildRetries(CompletableFuture.completedFuture(null),
            () -> leaseManager.createLeaseStoreIfNotExists(), "Failure creating lease store for this Event Hub, retrying",
            "Out of retries creating lease store for this Event Hub", EventProcessorHostActionStrings.CREATING_LEASE_STORE, this.retryMax);

        // Stages R+1 to 2R: create checkpoint store if it doesn't exist
        initializeStoresFuture = buildRetries(initializeStoresFuture, () -> checkpointManager.createCheckpointStoreIfNotExists(),
                "Failure creating checkpoint store for this Event Hub, retrying", "Out of retries creating checkpoint store for this Event Hub",
                EventProcessorHostActionStrings.CREATING_CHECKPOINT_STORE, this.retryMax);

        // Stages 2R+1 to 3R: create leases if they don't exist
        initializeStoresFuture = buildRetries(initializeStoresFuture, () -> leaseManager.createAllLeasesIfNotExists(Arrays.asList(this.partitionIds)),
                "Failure creating leases, retrying", "Out of retries creating leases", EventProcessorHostActionStrings.CREATING_LEASES, this.retryMax);

        // Stages 3R+1 to 4R: create checkpoint holders if they don't exist
        initializeStoresFuture = buildRetries(initializeStoresFuture, () -> checkpointManager.createAllCheckpointsIfNotExists(Arrays.asList(this.partitionIds)),
                "Failure creating checkpoint holders, retrying", "Out of retries creating checkpoint holders",
                EventProcessorHostActionStrings.CREATING_CHECKPOINTS, this.retryMax);

        initializeStoresFuture.whenCompleteAsync((r, e) -> {
            // If an exception has propagated this far, it should be a FinalException, which is guaranteed to contain a CompletionException.
            // Unwrap it so we don't leak a private type.
            if ((e != null) && (e instanceof FinalException)) {
                throw ((FinalException) e).getInner();
            }

            // Otherwise, allow the existing result to pass to the caller.
        }, this.hostContext.getExecutor());

        return initializeStoresFuture;
    }

    // CompletableFuture will be completed exceptionally if it runs out of retries.
    // If the lambda succeeds, then it will not be invoked again by following stages.
    private CompletableFuture<?> buildRetries(CompletableFuture<?> buildOnto, Callable<CompletableFuture<?>> lambda, String retryMessage,
                                              String finalFailureMessage, String action, int maxRetries) {
        // Stage 0: first attempt
        CompletableFuture<?> retryChain = buildOnto.thenCompose((unused) -> {
            CompletableFuture<?> newresult = CompletableFuture.completedFuture(null);
            try {
                newresult = lambda.call();
            } catch (Exception e1) {
                throw new CompletionException(e1);
            }
            return newresult;
        });

        for (int i = 1; i < maxRetries; i++) {
            retryChain = retryChain
                    // Stages 1, 3, 5, etc: trace errors but stop normal exception propagation in order to keep going.
                    // Either return null if we don't have a valid result, or pass the result along to the next stage.
                    // FinalExceptions are passed along also so that fatal error earlier in the chain aren't lost.
                    .handleAsync((r, e) -> {
                        Object effectiveResult = r;
                        if (e != null) {
                            if (e instanceof FinalException) {
                                // Propagate FinalException up to the end
                                throw (FinalException) e;
                            } else {
                                TRACE_LOGGER.warn(this.hostContext.withHost(retryMessage), LoggingUtils.unwrapException(e, null));
                            }
                        } else {
                            // Some lambdas return null on success. Change to TRUE to skip retrying.
                            if (r == null) {
                                effectiveResult = true;
                            }
                        }
                        return (e == null) ? effectiveResult : null; // stop propagation of other exceptions so we can retry
                    }, this.hostContext.getExecutor())
                    // Stages 2, 4, 6, etc: if we already have a valid result, pass it along. Otherwise, make another attempt.
                    // Once we have a valid result there will be no more attempts or exceptions.
                    .thenCompose((oldresult) -> {
                        CompletableFuture<?> newresult = CompletableFuture.completedFuture(oldresult);
                        if (oldresult == null) {
                            try {
                                newresult = lambda.call();
                            } catch (Exception e1) {
                                throw new CompletionException(e1);
                            }
                        }
                        return newresult;
                    });
        }
        // Stage final: trace the exception with the final message, or pass along the valid result.
        retryChain = retryChain.handleAsync((r, e) -> {
            if (e != null) {
                if (e instanceof FinalException) {
                    throw (FinalException) e;
                } else {
                    TRACE_LOGGER.warn(this.hostContext.withHost(finalFailureMessage));
                    throw new FinalException(LoggingUtils.wrapExceptionWithMessage(LoggingUtils.unwrapException(e, null), finalFailureMessage, action));
                }
            }
            return r;
        }, this.hostContext.getExecutor());

        return retryChain;
    }

    // Return Void so it can be called from a lambda.
    // throwOnFailure is true
    private Void scan(boolean isFirst) {
        TRACE_LOGGER.debug(this.hostContext.withHost("Starting lease scan"));
        long start = System.currentTimeMillis();

        try {
            (new PartitionScanner(this.hostContext, (lease) -> this.pumpManager.addPump(lease), this)).scan(isFirst)
                .whenCompleteAsync((didSteal, e) -> {
                    TRACE_LOGGER.debug(this.hostContext.withHost("Scanning took " + (System.currentTimeMillis() - start)));

                    if ((e != null) && !(e instanceof ClosingException)) {
                        TRACE_LOGGER.warn(this.hostContext.withHost("Lease scanner got exception"), e);
                    }

                    onPartitionCheckCompleteTestHook();

                    // Schedule the next scan unless we are shutting down.
                    if (!this.getIsClosingOrClosed()) {
                        int seconds = didSteal ? this.hostContext.getPartitionManagerOptions().getFastScanIntervalInSeconds()
                            : this.hostContext.getPartitionManagerOptions().getSlowScanIntervalInSeconds();
                        if (isFirst) {
                            seconds = this.hostContext.getPartitionManagerOptions().getStartupScanDelayInSeconds();
                        }
                        synchronized (this.scanFutureSynchronizer) {
                            this.scanFuture = this.hostContext.getExecutor().schedule(() -> scan(false), seconds, TimeUnit.SECONDS);
                        }
                        TRACE_LOGGER.debug(this.hostContext.withHost("Scheduling lease scanner in " + seconds));
                    } else {
                        TRACE_LOGGER.warn(this.hostContext.withHost("Not scheduling lease scanner due to shutdown"));
                    }
                }, this.hostContext.getExecutor());
        } catch (Exception e) {
            TRACE_LOGGER.error(this.hostContext.withHost("Lease scanner threw directly"), e);
            if (!this.getIsClosingOrClosed()) {
                int seconds = this.hostContext.getPartitionManagerOptions().getSlowScanIntervalInSeconds();
                synchronized (this.scanFutureSynchronizer) {
                    this.scanFuture = this.hostContext.getExecutor().schedule(() -> scan(false), seconds, TimeUnit.SECONDS);
                }
                TRACE_LOGGER.debug(this.hostContext.withHost("Forced schedule of lease scanner in " + seconds));
            }
        }

        return null;
    }

    // Exception wrapper that buildRetries() uses to indicate that a fatal error has occurred. The chain
    // built by buildRetries() normally swallows exceptions via odd-numbered stages so that the retries in
    // even-numbered stages will execute. If multiple chains are concatenated, FinalException short-circuits
    // the exceptional swallowing and allows fatal errors in earlier chains to be propagated all the way to the end.
    static class FinalException extends CompletionException {
        private static final long serialVersionUID = -4600271981700687166L;

        FinalException(CompletionException e) {
            super(e);
        }

        CompletionException getInner() {
            return (CompletionException) this.getCause();
        }
    }
}
