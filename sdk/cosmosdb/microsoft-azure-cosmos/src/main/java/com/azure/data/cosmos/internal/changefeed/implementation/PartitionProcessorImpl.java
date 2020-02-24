// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.changefeed.CancellationToken;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import com.azure.data.cosmos.internal.changefeed.PartitionCheckpointer;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.data.cosmos.internal.changefeed.ProcessorSettings;
import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import com.azure.data.cosmos.internal.changefeed.exceptions.PartitionNotFoundException;
import com.azure.data.cosmos.internal.changefeed.exceptions.PartitionSplitException;
import com.azure.data.cosmos.internal.changefeed.exceptions.TaskCancelledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

import static com.azure.data.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;
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
        this.options.maxItemCount(settings.getMaxItemCount());
        partitionKeyRangeIdInternal(this.options, settings.getPartitionKeyRangeId());
        // this.options.sessionToken(properties.sessionToken());
        this.options.startFromBeginning(settings.isStartFromBeginning());
        this.options.requestContinuation(settings.getStartContinuation());
        this.options.startDateTime(settings.getStartTime());
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        this.lastContinuation = this.settings.getStartContinuation();
        this.isFirstQueryForChangeFeeds = true;

        this.options.requestContinuation(this.lastContinuation);

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

                this.lastContinuation = documentFeedResponse.continuationToken();
                if (documentFeedResponse.results() != null && documentFeedResponse.results().size() > 0) {
                    return this.dispatchChanges(documentFeedResponse)
                        .doOnError(throwable -> {
                            logger.debug("Exception was thrown from thread {}", Thread.currentThread().getId(), throwable);
                        })
                        .doOnSuccess((Void) -> {
                            this.options.requestContinuation(this.lastContinuation);

                            if (cancellationToken.isCancellationRequested()) throw new TaskCancelledException();
                        });
                }
                this.options.requestContinuation(this.lastContinuation);

                if (cancellationToken.isCancellationRequested()) {
                    return Flux.error(new TaskCancelledException());
                }

                return Flux.empty();
            })
            .doOnComplete(() -> {
                if (this.options.maxItemCount().compareTo(this.settings.getMaxItemCount()) != 0) {
                    this.options.maxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof CosmosClientException) {

                    CosmosClientException clientException = (CosmosClientException) throwable;
                    logger.warn("CosmosClientException: partition {} from thread {}",
                        this.options.partitionKey().getInternalPartitionKey(), Thread.currentThread().getId(), clientException);
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
                            if (this.options.maxItemCount() == null) {
                                this.options.maxItemCount(DefaultMaxItemCount);
                            } else if (this.options.maxItemCount() <= 1) {
                                logger.error("Cannot reduce maxItemCount further as it's already at {}", this.options.maxItemCount(), clientException);
                                this.resultException = new RuntimeException(clientException);
                            }

                            this.options.maxItemCount(this.options.maxItemCount() / 2);
                            logger.warn("Reducing maxItemCount, new value: {}", this.options.maxItemCount());
                            return Flux.empty();
                        }
                        case TRANSIENT_ERROR: {
                            // Retry on transient (429) errors
                            if (clientException.retryAfterInMilliseconds() > 0) {
                                ZonedDateTime stopTimer = ZonedDateTime.now().plus(clientException.retryAfterInMilliseconds(), MILLIS);
                                return Mono.just(clientException.retryAfterInMilliseconds()) // set some seed value to be able to run the repeat loop
                                    .delayElement(Duration.ofMillis(100))
                                    .repeat( () -> {
                                        ZonedDateTime currentTime = ZonedDateTime.now();
                                        return !cancellationToken.isCancellationRequested() && currentTime.isBefore(stopTimer);
                                    }).flatMap( values -> Flux.empty());
                            }
                        }
                        default: {
                            logger.error("Unrecognized Cosmos exception returned error code {}", docDbError, clientException);
                            this.resultException = new RuntimeException(clientException);
                        }
                    }
                } else if (throwable instanceof LeaseLostException) {
                    logger.info("LeaseLoseException with partition {} from thread {}",
                        this.options.partitionKey().getInternalPartitionKey(), Thread.currentThread().getId());
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

        return this.observer.processChanges(context, response.results());
    }
}
