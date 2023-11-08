// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedObserverContextImpl;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ExceptionClassifier;
import com.azure.cosmos.implementation.changefeed.common.StatusCodeErrorType;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionNotFoundException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * Implementation for {@link PartitionProcessor}.
 */
class PartitionProcessorImpl implements PartitionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PartitionProcessorImpl.class);

    private final ProcessorSettings settings;
    private final PartitionCheckpointer checkpointer;
    private final ChangeFeedObserver<JsonNode> observer;
    private volatile CosmosChangeFeedRequestOptions options;
    private final ChangeFeedContextClient documentClient;
    private final Lease lease;
    private volatile RuntimeException resultException;

    private volatile String lastServerContinuationToken;
    private volatile boolean hasMoreResults;

    public PartitionProcessorImpl(ChangeFeedObserver<JsonNode> observer,
                                  ChangeFeedContextClient documentClient,
                                  ProcessorSettings settings,
                                  PartitionCheckpointer checkpointer,
                                  Lease lease) {
        this.observer = observer;
        this.documentClient = documentClient;
        this.settings = settings;
        this.checkpointer = checkpointer;
        this.lease = lease;

        ChangeFeedState state = settings.getStartState();
        this.options = ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(state);
        this.options.setMaxItemCount(settings.getMaxItemCount());
        // For pk version, merge is not support, exclude it from the capabilities header
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.getCosmosChangeFeedRequestOptionsAccessor()
            .setHeader(
                this.options,
                HttpConstants.HttpHeaders.SDK_SUPPORTED_CAPABILITIES,
                String.valueOf(HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES_NONE));
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        logger.info("Partition {}: processing task started with owner {}.", this.lease.getLeaseToken(), this.lease.getOwner());
        this.hasMoreResults = true;
        this.checkpointer.setCancellationToken(cancellationToken);

        return Flux.just(this)
            .flatMap( value -> {
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
            .flatMap(value -> this.documentClient.createDocumentChangeFeedQuery(this.settings.getCollectionSelfLink(),
                                                                                this.options, JsonNode.class)
                .limitRequest(1)
            )
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
                this.lastServerContinuationToken = continuationState
                    .getContinuation()
                    .getCurrentContinuationToken()
                    .getToken();

                this.hasMoreResults = !ModelBridgeInternal.noChanges(documentFeedResponse);
                if (documentFeedResponse.getResults() != null && documentFeedResponse.getResults().size() > 0) {
                    logger.info("Partition {}: processing {} feeds with owner {}.", this.lease.getLeaseToken(), documentFeedResponse.getResults().size(), this.lease.getOwner());
                    return this.dispatchChanges(documentFeedResponse, continuationState)
                        .doOnError(throwable -> logger.debug(
                            "Exception was thrown from thread {}",
                            Thread.currentThread().getId(), throwable))
                        .doOnSuccess((Void) -> {
                            this.options =
                                CosmosChangeFeedRequestOptions
                                    .createForProcessingFromContinuation(continuationToken);

                            if (cancellationToken.isCancellationRequested()) throw new TaskCancelledException();
                        });
                }
                this.options =
                    CosmosChangeFeedRequestOptions
                        .createForProcessingFromContinuation(continuationToken);

                if (cancellationToken.isCancellationRequested()) {
                    return Flux.error(new TaskCancelledException());
                }

                return Flux.empty();
            })
            .doOnComplete(() -> {
                if (this.options.getMaxItemCount() != this.settings.getMaxItemCount()) {
                    this.options.setMaxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosException) {
                    // NOTE - the reason why it is safe to access the this.lastServerContinuationToken
                    // below in a tread-safe manner is because the CosmosException would never be thrown
                    // form the flatMap-section above (but only from the "source" (the flatMap-section
                    // calling createDocumentChangeFeedQuery - so if we ever land in this if-block
                    // we know it is a terminal event.

                    CosmosException clientException = (CosmosException) throwable;
                    logger.warn("CosmosException: Partition {} from thread {} with owner {}",
                        this.lease.getLeaseToken(), Thread.currentThread().getId(), this.lease.getOwner(), clientException);
                    StatusCodeErrorType docDbError = ExceptionClassifier.classifyClientException(clientException);

                    switch (docDbError) {
                        case PARTITION_NOT_FOUND: {
                            this.resultException = new PartitionNotFoundException(
                                "Partition not found.",
                                this.lastServerContinuationToken);
                        }
                        break;
                        case PARTITION_SPLIT_OR_MERGE: {
                            this.resultException = new FeedRangeGoneException(
                                "Partition split.",
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
                                    "Cannot reduce maxItemCount further as it's already at {}",
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
                        }
                        break;
                        default: {
                            logger.error("Unrecognized Cosmos exception returned error code {}", docDbError, clientException);
                            this.resultException = new RuntimeException(clientException);
                        }
                    }
                } else if (throwable instanceof LeaseLostException) {
                    logger.info("LeaseLoseException with Partition {} from thread {} with owner {}",
                        this.lease.getLeaseToken(), Thread.currentThread().getId(), this.lease.getOwner());
                    this.resultException = (LeaseLostException) throwable;
                } else if (throwable instanceof TaskCancelledException) {
                    logger.debug("Task cancelled exception: Partition {} from thread {} with owner {}",
                        this.lease.getLeaseToken(), Thread.currentThread().getId(), this.lease.getOwner(), throwable);
                    this.resultException = (TaskCancelledException) throwable;
                } else {
                    logger.warn("Unexpected exception: Partition {} from thread {} with owner {}",
                        this.lease.getLeaseToken(), Thread.currentThread().getId(), this.lease.getOwner(), throwable);
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
                logger.info("Partition {}: processing task exited with owner {}.", this.lease.getLeaseToken(), this.lease.getOwner());
            });
    }

    private FeedRangePartitionKeyRangeImpl getPkRangeFeedRangeFromStartState() {
        final FeedRangeInternal feedRange = this.settings.getStartState().getFeedRange();
        checkNotNull(feedRange, "FeedRange must not be null here.");

        // TODO fabianm - move observer to FeedRange and remove this constraint for merge support
        checkArgument(
            feedRange instanceof FeedRangePartitionKeyRangeImpl,
            "FeedRange must be a PkRangeId FeedRange when using Lease V1 contract.");

        return (FeedRangePartitionKeyRangeImpl)feedRange;
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private Mono<Void> dispatchChanges(
        FeedResponse<JsonNode> response,
        ChangeFeedState continuationState) {

        ChangeFeedObserverContext<JsonNode> context = new ChangeFeedObserverContextImpl<>(
            this.getPkRangeFeedRangeFromStartState().getPartitionKeyRangeId(),
            response,
            continuationState,
            this.checkpointer);

        return this.observer.processChanges(context, response.getResults());
    }
}
