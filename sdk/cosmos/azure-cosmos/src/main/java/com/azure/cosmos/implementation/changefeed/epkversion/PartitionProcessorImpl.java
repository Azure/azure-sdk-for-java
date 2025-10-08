// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedObserverContextImpl;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ExceptionClassifier;
import com.azure.cosmos.implementation.changefeed.common.StatusCodeErrorType;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionNotFoundException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.CosmosChangeFeedContinuationTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * Implementation for {@link PartitionProcessor}.
 */
class PartitionProcessorImpl<T> implements PartitionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PartitionProcessorImpl.class);

    private final ProcessorSettings settings;
    private final PartitionCheckpointer checkpointer;
    private final ChangeFeedObserver<T> observer;
    private volatile CosmosChangeFeedRequestOptions options;
    private final ChangeFeedContextClient documentClient;
    private final Lease lease;
    private final Class<T> itemType;
    private final ChangeFeedMode changeFeedMode;
    private volatile RuntimeException resultException;

    private volatile String lastServerContinuationToken;
    private volatile boolean hasMoreResults;
    private volatile boolean hasServerContinuationTokenChange;
    private final int maxStreamsConstrainedRetries = 10;
    private final AtomicInteger streamsConstrainedRetries = new AtomicInteger(0);
    private final AtomicInteger unparseableDocumentRetries = new AtomicInteger(0);
    private final FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager;
    private Instant lastProcessedTime;

    public PartitionProcessorImpl(ChangeFeedObserver<T> observer,
                                  ChangeFeedContextClient documentClient,
                                  ProcessorSettings settings,
                                  PartitionCheckpointer checkpointer,
                                  Lease lease,
                                  Class<T> itemType,
                                  ChangeFeedMode changeFeedMode,
                                  FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager) {
        this.observer = observer;
        this.documentClient = documentClient;
        this.settings = settings;
        this.checkpointer = checkpointer;
        this.lease = lease;
        this.itemType = itemType;
        this.changeFeedMode = changeFeedMode;
        this.lastServerContinuationToken = this.lease.getContinuationToken();

        this.options = PartitionProcessorHelper.createChangeFeedRequestOptionsForChangeFeedState(
                settings.getStartState(),
                settings.getMaxItemCount(),
                this.changeFeedMode);
        this.options.setResponseInterceptor(settings.getResponseInterceptor());

        this.feedRangeThroughputControlConfigManager = feedRangeThroughputControlConfigManager;
        this.lastProcessedTime = Instant.now();
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        logger.info("Lease with token {}: processing task started with owner {}.",
            this.lease.getLeaseToken(), this.lease.getOwner());
        this.hasMoreResults = true;
        this.hasServerContinuationTokenChange = false;
        this.checkpointer.setCancellationToken(cancellationToken);

        return Flux.just(this)
            .flatMap(value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Flux.empty();
                }

                // If there are still changes need to be processed, fetch right away
                // If there are no changes, wait pollDelay time then try again
                if(this.hasMoreResults && this.resultException == null) {
                    return Flux.just(value);
                }

                Instant stopTimer = Instant.now().plus(this.settings.getFeedPollDelay());
                return Mono.just(value)
                    .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
                    .repeat( () -> {
                        Instant currentTime = Instant.now();
                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                    }).last();

            })
            .flatMap(value -> this.tryGetThroughputControlConfigForFeedRange(this.lease))
            .flatMap(configValueHolder -> {
                if (configValueHolder.v != null) {
                    this.options.setThroughputControlGroupName(configValueHolder.v.getGroupName());
                }

                return this.documentClient.createDocumentChangeFeedQuery(
                        this.settings.getCollectionSelfLink(),
                        this.options,
                        itemType);
            })
            .flatMap(documentFeedResponse -> {
                if (cancellationToken.isCancellationRequested()) return Flux.error(new TaskCancelledException());

                final String continuationToken = documentFeedResponse.getContinuationToken();

                final ChangeFeedState continuationState = ChangeFeedState.fromString(continuationToken);
                checkNotNull(continuationState, "Argument 'continuationState' must not be null.");
                checkArgument(
                    continuationState
                        .getContinuation()
                        .getContinuationTokenCount() == 1,
                    "For ChangeFeedProcessor the continuation state should always have one range/continuation");

                this.hasServerContinuationTokenChange =
                    !StringUtils.equals(this.lastServerContinuationToken, continuationToken)
                        && StringUtils.isNotEmpty(continuationToken);

                this.lastServerContinuationToken = continuationToken;
                this.hasMoreResults = !ModelBridgeInternal.noChanges(documentFeedResponse);

                lastProcessedTime = Instant.now();
                if (documentFeedResponse.getResults() != null && documentFeedResponse.getResults().size() > 0) {
                    logger.info("Lease with token {}: processing {} feeds with owner {}.",
                        this.lease.getLeaseToken(), documentFeedResponse.getResults().size(), this.lease.getOwner());
                    return this.dispatchChanges(documentFeedResponse, continuationState)
                        .doOnError(throwable -> logger.warn(
                            "Lease with token " + this.lease.getLeaseToken() + ": Exception was thrown from thread " +
                                Thread.currentThread().getId(),
                            throwable))
                        .doOnSuccess((Void) -> {
                            this.options = PartitionProcessorHelper.createForProcessingFromContinuation(continuationToken, this.changeFeedMode);

                            if (cancellationToken.isCancellationRequested()) throw new TaskCancelledException();
                        });
                } else {
                    // only skip checkpoint for 304 & noServerContinuationToken change
                    boolean shouldSkipCheckpoint = !hasServerContinuationTokenChange && !hasMoreResults;
                    return Mono.just(shouldSkipCheckpoint)
                        .flatMap(skipCheckpoint -> {
                            if (skipCheckpoint) {
                                return Mono.empty();
                            } else {
                                return this.checkpointer.checkpointPartition(continuationState)
                                    .doOnError(throwable -> {
                                        logger.warn(
                                            "Failed to checkpoint Lease with token " + this.lease.getLeaseToken() +
                                                " from thread " + Thread.currentThread().getId(),
                                            throwable);
                                    });
                            }
                        })
                        .doOnSuccess((Void) -> {
                            this.options = PartitionProcessorHelper.createForProcessingFromContinuation(continuationToken, this.changeFeedMode);
                            if (cancellationToken.isCancellationRequested()) throw new TaskCancelledException();
                        });
                }
            })
            .doOnComplete(() -> {
                if (this.options.getMaxItemCount() != this.settings.getMaxItemCount()) {
                    this.options.setMaxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                }

                this.options.setResponseInterceptor(settings.getResponseInterceptor());
                this.streamsConstrainedRetries.set(0);
                this.unparseableDocumentRetries.set(0);
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    // NOTE - the reason why it is safe to access the this.lastServerContinuationToken
                    // below in a tread-safe manner is because the CosmosException would never be thrown
                    // form the flatMap-section above (but only from the "source" (the flatMap-section
                    // calling createDocumentChangeFeedQuery - so if we ever land in this if-block
                    // we know it is a terminal event.

                    CosmosException clientException = (CosmosException) throwable;
                    logger.warn(
                        "Lease with token " + this.lease.getLeaseToken() + ": CosmosException was thrown from thread " +
                            Thread.currentThread().getId() + " for lease with owner " + this.lease.getOwner(),
                        clientException);
                    StatusCodeErrorType docDbError =
                        ExceptionClassifier.classifyClientException(clientException);

                    switch (docDbError) {
                        case PARTITION_NOT_FOUND: {
                            this.resultException = new PartitionNotFoundException(
                                "Partition not found.",
                                this.lastServerContinuationToken);
                        }
                        break;
                        case PARTITION_SPLIT_OR_MERGE: {
                            this.resultException = new FeedRangeGoneException(
                                "Partition split or merge.",
                                this.lastServerContinuationToken);
                        }
                        break;
                        case UNDEFINED: {
                            this.resultException = new RuntimeException(clientException);
                        }
                        break;

                        case MAX_ITEM_COUNT_TOO_LARGE: {
                            if (this.options.getMaxItemCount() <= 1) {
                                logger.error(
                                    "Cannot reduce maxItemCount further as it's already at " +
                                        this.options.getMaxItemCount(),
                                    clientException);
                                this.resultException = new RuntimeException(clientException);
                            }

                            this.options.setMaxItemCount(this.options.getMaxItemCount() / 2);
                            logger.warn("Reducing maxItemCount, new value: {}", this.options.getMaxItemCount());
                            return Flux.empty();
                        }
                        case TRANSIENT_ERROR: {
                            // Retry on transient (429) errors
                            if (clientException.getRetryAfterDuration().toMillis() > 0) {
                                Instant stopTimer = Instant.now().plus(clientException.getRetryAfterDuration().toMillis(), MILLIS);
                                return Mono.just(clientException.getRetryAfterDuration().toMillis()) // set some seed value to be able to run
                                    // the repeat loop
                                    .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
                                    .repeat(() -> {
                                        Instant currentTime = Instant.now();
                                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                                    }).flatMap(values -> Flux.empty());
                            }
                            break;
                        }
                        case JACKSON_STREAMS_CONSTRAINED: {

                            if (!Configs.isChangeFeedProcessorMalformedResponseRecoveryEnabled()) {
                                logger.error(
                                    "Lease with token : " + this.lease.getLeaseToken() + " : Streams constrained exception encountered. To enable automatic retries, please set the " + Configs.CHANGE_FEED_PROCESSOR_MALFORMED_RESPONSE_RECOVERY_ENABLED + " configuration to 'true'. Failing.",
                                    clientException);
                                this.resultException = new RuntimeException(clientException);
                                return Flux.error(throwable);
                            }

                            int retryCount = this.streamsConstrainedRetries.incrementAndGet();
                            boolean shouldRetry = retryCount <= this.maxStreamsConstrainedRetries;

                            if (!shouldRetry) {
                                logger.error(
                                    "Lease with token : " + this.lease.getLeaseToken() + ": Reached max retries for streams constrained exception with statusCode : [" + clientException.getStatusCode() + "]" + " : subStatusCode " + clientException.getSubStatusCode() + " : message " + clientException.getMessage() + ", failing.",
                                    clientException);
                                this.resultException = new RuntimeException(clientException);
                                return Flux.error(throwable);
                            }

                            logger.warn(
                                "Lease with token : " + this.lease.getLeaseToken() + " : Streams constrained exception encountered, will retry. " + "retryCount " + retryCount + " of " + this.maxStreamsConstrainedRetries + " retries.",
                                clientException);


                            if (this.options.getMaxItemCount() == -1) {
                                logger.warn(
                                    "Lease with token : " + this.lease.getLeaseToken() + " : max item count is set to -1, will retry after setting it to 100. " + "retryCount " + retryCount + " of " + this.maxStreamsConstrainedRetries + " retries.",
                                    clientException);
                                this.options.setMaxItemCount(100);
                                return Flux.empty();
                            }

                            if (this.options.getMaxItemCount() <= 1) {
                                logger.error(
                                    "Lease with token : " + this.lease.getLeaseToken() + " Cannot reduce maxItemCount further as it's already at :" + this.options.getMaxItemCount(), clientException);
                                this.resultException = new RuntimeException(clientException);
                                return Flux.error(throwable);
                            }

                            this.options.setMaxItemCount(this.options.getMaxItemCount() / 2);
                            logger.warn("Lease with token : " + this.lease.getLeaseToken() + " Reducing maxItemCount, new value: " + this.options.getMaxItemCount());
                            return Flux.empty();
                        }
                        case JSON_PARSING_ERROR:

                            if (!Configs.isChangeFeedProcessorMalformedResponseRecoveryEnabled()) {
                                logger.error(
                                    "Lease with token : " + this.lease.getLeaseToken() + ": Parsing error encountered. To enable automatic retries, please set the + " + Configs.CHANGE_FEED_PROCESSOR_MALFORMED_RESPONSE_RECOVERY_ENABLED + " configuration to 'true'. Failing.", clientException);
                                this.resultException = new RuntimeException(clientException);
                                return Flux.error(throwable);
                            }

                            if (this.unparseableDocumentRetries.compareAndSet(0, 1)) {
                                logger.warn(
                                    "Lease with token : " + this.lease.getLeaseToken() + " : Attempting a retry on parsing error.", clientException);
                                this.options.setMaxItemCount(1);
                                return Flux.empty();
                            } else {

                                logger.error("Lease with token : " + this.lease.getLeaseToken() + " : Encountered parsing error which is not recoverable, attempting to skip document", clientException);

                                String continuation = CosmosChangeFeedContinuationTokenUtils.extractContinuationTokenFromCosmosException(clientException);

                                if (Strings.isNullOrEmpty(continuation)) {
                                    logger.error(
                                        "Lease with token : " + this.lease.getLeaseToken() + ": Unable to extract continuation token post the parsing exception, failing.",
                                        clientException);
                                    this.resultException = new RuntimeException(clientException);
                                    return Flux.error(throwable);
                                }

                                ChangeFeedState continuationState = ChangeFeedState.fromString(continuation);
                                return this.checkpointer.checkpointPartition(continuationState)
                                    .doOnSuccess(lease1 -> {
                                        logger.info("Lease with token : " + this.lease.getLeaseToken() + " Successfully skipped the unparseable document.");
                                        this.options =
                                            PartitionProcessorHelper.createForProcessingFromContinuation(continuation, this.changeFeedMode);
                                    })
                                    .doOnError(t -> {
                                        logger.error(
                                            "Failed to checkpoint for lease with token :  " + this.lease.getLeaseToken() + " with continuation " + this.lease.getReadableContinuationToken() + " from thread " + Thread.currentThread().getId(), t);
                                        this.resultException = new RuntimeException(t);
                                    });
                            }
                        default: {
                            logger.error(
                                "Lease with token " + this.lease.getLeaseToken() +
                                    ": Unrecognized Cosmos exception returned error code " + docDbError,
                                clientException);
                            this.resultException = new RuntimeException(clientException);
                        }
                    }
                } else if (throwable instanceof LeaseLostException) {
                    logger.info(
                        "Lease with token {}: LeaseLoseException was thrown from thread {} for lease with owner {}",
                        this.lease.getLeaseToken(),
                        Thread.currentThread().getId(),
                        this.lease.getOwner());
                    this.resultException = (LeaseLostException) throwable;
                } else if (throwable instanceof TaskCancelledException) {
                    logger.debug(
                        "Lease with token " + this.lease.getLeaseToken() + ": Task cancelled exception was thrown from thread " +
                            Thread.currentThread().getId() + " for lease with owner " + this.lease.getOwner(),
                        throwable);
                    this.resultException = (TaskCancelledException) throwable;
                } else {
                    logger.warn(
                        "Lease with token " + this.lease.getLeaseToken() + ": Unexpected exception was thrown from thread " +
                            Thread.currentThread().getId() + " for lease with owner " + this.lease.getOwner(),
                        throwable);
                    this.resultException = new RuntimeException(throwable);
                }
                return Flux.error(throwable);
            })
            .repeat(() -> {
                if (cancellationToken.isCancellationRequested()) {
                    this.resultException = new TaskCancelledException();
                    return false;
                }

                return true;
            })
            .onErrorResume(throwable -> {
                if (this.resultException == null) {
                    this.resultException = new RuntimeException(throwable);
                }

                return Flux.empty();
            })
            .then()
            .doFinally( any -> {
                logger.info(
                    "Lease with token {}: processing task exited with owner {}.",
                    this.lease.getLeaseToken(),
                    this.lease.getOwner());
            });
    }

    private Mono<Utils.ValueHolder<ThroughputControlGroupConfig>> tryGetThroughputControlConfigForFeedRange(Lease lease) {
        if (this.feedRangeThroughputControlConfigManager == null) {
            return Mono.just(new Utils.ValueHolder<>(null));
        }

        return this.feedRangeThroughputControlConfigManager
            .getOrCreateThroughputControlConfigForFeedRange((FeedRangeEpkImpl) lease.getFeedRange())
            .map(config -> new Utils.ValueHolder<>(config));
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    @Override
    public Instant getLastProcessedTime() {
        return this.lastProcessedTime;
    }

    private Mono<Void> dispatchChanges(
        FeedResponse<T> response,
        ChangeFeedState continuationState) {

        ChangeFeedObserverContext<T> context = new ChangeFeedObserverContextImpl<>(
            lease.getLeaseToken(),
            response,
            continuationState,
            this.checkpointer);

        return this.observer.processChanges(context, response.getResults());
    }
}
