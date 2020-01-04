// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedOptions;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionProcessor;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionNotFoundException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionSplitException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

import static com.azure.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * Implementation for {@link PartitionProcessor}.
 */
class PartitionProcessorImpl implements PartitionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PartitionProcessorImpl.class);

    private static final int DefaultMaxItemCount = 100;
    private final ProcessorSettings settings;
    private final PartitionCheckpointer checkpointer;
    private final ChangeFeedObserver observer;
    private final ChangeFeedOptions options;
    private final ChangeFeedContextClient documentClient;
    private volatile RuntimeException resultException;

    private volatile String lastContinuation;
    private volatile boolean isFirstQueryForChangeFeeds;


    public PartitionProcessorImpl(ChangeFeedObserver observer, ChangeFeedContextClient documentClient, ProcessorSettings settings, PartitionCheckpointer checkpointer) {
        this.observer = observer;
        this.documentClient = documentClient;
        this.settings = settings;
        this.checkpointer = checkpointer;

        this.options = new ChangeFeedOptions();
        this.options.setMaxItemCount(settings.getMaxItemCount());
        partitionKeyRangeIdInternal(this.options, settings.getPartitionKeyRangeId());
        // this.setOptions.getSessionToken(getProperties.getSessionToken());
        this.options.setStartFromBeginning(settings.isStartFromBeginning());
        this.options.setRequestContinuation(settings.getStartContinuation());
        this.options.setStartDateTime(settings.getStartTime());
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        this.lastContinuation = this.settings.getStartContinuation();
        this.isFirstQueryForChangeFeeds = true;

        this.options.setRequestContinuation(this.lastContinuation);

        return Flux.just(this)
            .flatMap( value -> {
                if (cancellationToken.isCancellationRequested()) {
                    return Flux.empty();
                }

                if(this.isFirstQueryForChangeFeeds) {
                    this.isFirstQueryForChangeFeeds = false;
                    return Flux.just(value);
                }

                ZonedDateTime stopTimer = ZonedDateTime.now().plus(this.settings.getFeedPollDelay());
                return Mono.just(value)
                    .delayElement(Duration.ofMillis(100))
                    .repeat( () -> {
                        ZonedDateTime currentTime = ZonedDateTime.now();
                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                    }).last();

            })
            .flatMap(value -> this.documentClient.createDocumentChangeFeedQuery(this.settings.getCollectionSelfLink(), this.options)
                .limitRequest(1)
            )
            .flatMap(documentFeedResponse -> {
                if (cancellationToken.isCancellationRequested()) return Flux.error(new TaskCancelledException());

                this.lastContinuation = documentFeedResponse.getContinuationToken();
                if (documentFeedResponse.getResults() != null && documentFeedResponse.getResults().size() > 0) {
                    return this.dispatchChanges(documentFeedResponse)
                        .doOnError(throwable -> {
                            logger.debug("Exception was thrown from thread {}", Thread.currentThread().getId(), throwable);
                        })
                        .doOnSuccess((Void) -> {
                        this.options.setRequestContinuation(this.lastContinuation);

                            if (cancellationToken.isCancellationRequested()) throw new TaskCancelledException();
                        });
                }
                this.options.setRequestContinuation(this.lastContinuation);

                if (cancellationToken.isCancellationRequested()) {
                    return Flux.error(new TaskCancelledException());
                }

                return Flux.empty();
            })
            .doOnComplete(() -> {
                if (this.options.getMaxItemCount().compareTo(this.settings.getMaxItemCount()) != 0) {
                    this.options.setMaxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {

                    CosmosClientException clientException = (CosmosClientException) throwable;
                    logger.warn("CosmosClientException: partition {} from thread {}",
                        BridgeInternal.getPartitionKeyInternal(this.options.getPartitionKey()), Thread.currentThread().getId(), clientException);
                    StatusCodeErrorType docDbError = ExceptionClassifier.classifyClientException(clientException);

                    switch (docDbError) {
                        case PARTITION_NOT_FOUND: {
                            this.resultException = new PartitionNotFoundException("Partition not found.", this.lastContinuation);
                        }
                        case PARTITION_SPLIT: {
                            this.resultException = new PartitionSplitException("Partition split.", this.lastContinuation);
                        }
                        case UNDEFINED: {
                            this.resultException = new RuntimeException(clientException);
                        }
                        case MAX_ITEM_COUNT_TOO_LARGE: {
                            if (this.options.getMaxItemCount() == null) {
                                this.options.setMaxItemCount(DefaultMaxItemCount);
                            } else if (this.options.getMaxItemCount() <= 1) {
                                logger.error("Cannot reduce maxItemCount further as it's already at {}", this.options.getMaxItemCount(), clientException);
                                this.resultException = new RuntimeException(clientException);
                            }

                            this.options.setMaxItemCount(this.options.getMaxItemCount() / 2);
                            logger.warn("Reducing maxItemCount, new value: {}", this.options.getMaxItemCount());
                            return Flux.empty();
                        }
                        case TRANSIENT_ERROR: {
                            // Retry on transient (429) errors
                            if (clientException.getRetryAfterInMilliseconds() > 0) {
                                ZonedDateTime stopTimer = ZonedDateTime.now().plus(clientException.getRetryAfterInMilliseconds(), MILLIS);
                                return Mono.just(clientException.getRetryAfterInMilliseconds()) // set some seed value to be able to run
                                    // the repeat loop
                                    .delayElement(Duration.ofMillis(100))
                                    .repeat(() -> {
                                        ZonedDateTime currentTime = ZonedDateTime.now();
                                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                                    }).flatMap(values -> Flux.empty());
                            }
                        }
                        default: {
                            logger.error("Unrecognized Cosmos exception returned error code {}", docDbError, clientException);
                            this.resultException = new RuntimeException(clientException);
                        }
                    }
                } else if (throwable instanceof LeaseLostException) {
                        logger.info("LeaseLoseException with partition {} from thread {}",
                            BridgeInternal.getPartitionKeyInternal(this.options.getPartitionKey()), Thread.currentThread().getId());
                        this.resultException = (LeaseLostException) throwable;
                } else if (throwable instanceof TaskCancelledException) {
                    logger.debug("Task cancelled exception: partition {} from {}",
                        this.settings.getPartitionKeyRangeId(), Thread.currentThread().getId(), throwable);
                    this.resultException = (TaskCancelledException) throwable;
                } else {
                    logger.warn("Unexpected exception from thread {}", Thread.currentThread().getId(), throwable);
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
            }).then();
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private Mono<Void> dispatchChanges(FeedResponse<CosmosItemProperties> response) {
        ChangeFeedObserverContext context = new ChangeFeedObserverContextImpl(this.settings.getPartitionKeyRangeId(), response, this.checkpointer);

        return this.observer.processChanges(context, response.getResults());
    }
}
