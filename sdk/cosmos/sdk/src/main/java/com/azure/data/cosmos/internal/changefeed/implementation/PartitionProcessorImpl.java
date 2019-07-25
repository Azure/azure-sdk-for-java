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
import com.azure.data.cosmos.internal.changefeed.exceptions.PartitionNotFoundException;
import com.azure.data.cosmos.internal.changefeed.exceptions.PartitionSplitException;
import com.azure.data.cosmos.internal.changefeed.exceptions.TaskCancelledException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.azure.data.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;

/**
 * Implementation for {@link PartitionProcessor}.
 */
class PartitionProcessorImpl implements PartitionProcessor {
    private static final int DefaultMaxItemCount = 100;
    // private final Observable<FeedResponse<Document>> query;
    private final ProcessorSettings settings;
    private final PartitionCheckpointer checkpointer;
    private final ChangeFeedObserver observer;
    private final ChangeFeedOptions options;
    private final ChangeFeedContextClient documentClient;
    private RuntimeException resultException;

    private String lastContinuation;

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

        //this.query = documentClient.createDocumentChangeFeedQuery(self.properties.getCollectionSelfLink(), this.options);
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        PartitionProcessorImpl self = this;
        this.lastContinuation = this.settings.getStartContinuation();

        return Mono.fromRunnable( () -> {
            while (!cancellationToken.isCancellationRequested()) {
                Duration delay = self.settings.getFeedPollDelay();

                try {
                    self.options.requestContinuation(self.lastContinuation);
                    List<FeedResponse<CosmosItemProperties>> documentFeedResponseList = self.documentClient.createDocumentChangeFeedQuery(self.settings.getCollectionSelfLink(), self.options)
                        .collectList()
                        .block();

                    for (FeedResponse<CosmosItemProperties> documentFeedResponse : documentFeedResponseList) {
                        self.lastContinuation = documentFeedResponse.continuationToken();
                        if (documentFeedResponse.results() != null && documentFeedResponse.results().size() > 0) {
                            self.dispatchChanges(documentFeedResponse);
                        }

                        self.options.requestContinuation(self.lastContinuation);

                        if (cancellationToken.isCancellationRequested()) {
                            // Observation was cancelled.
                            throw new TaskCancelledException();
                        }
                    }

                    if (this.options.maxItemCount().compareTo(this.settings.getMaxItemCount()) == 0) {
                        this.options.maxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                    }
                } catch (RuntimeException ex) {
                    if (ex.getCause() instanceof CosmosClientException) {

                        CosmosClientException clientException = (CosmosClientException) ex.getCause();
                        // this.logger.WarnException("exception: partition '{0}'", clientException, this.properties.PartitionKeyRangeId);
                        StatusCodeErrorType docDbError = ExceptionClassifier.classifyClientException(clientException);

                        switch (docDbError) {
                            case PARTITION_NOT_FOUND: {
                                self.resultException = new PartitionNotFoundException("Partition not found.", self.lastContinuation);
                            }
                            case PARTITION_SPLIT: {
                                self.resultException = new PartitionSplitException("Partition split.", self.lastContinuation);
                            }
                            case UNDEFINED: {
                                self.resultException = ex;
                            }
                            case MAX_ITEM_COUNT_TOO_LARGE: {
                                if (this.options.maxItemCount() == null) {
                                    this.options.maxItemCount(DefaultMaxItemCount);
                                } else if (this.options.maxItemCount() <= 1) {
                                    // this.logger.ErrorFormat("Cannot reduce maxItemCount further as it's already at {0}.", this.options.MaxItemCount);
                                    throw ex;
                                }

                                this.options.maxItemCount(this.options.maxItemCount() / 2);
                                // this.logger.WarnFormat("Reducing maxItemCount, new value: {0}.", this.options.MaxItemCount);
                                break;
                            }
                            default: {
                                // this.logger.Fatal($"Unrecognized DocDbError enum value {docDbError}");
                                // Debug.Fail($"Unrecognized DocDbError enum value {docDbError}");
                                self.resultException = ex;
                            }
                        }
                    } else if (ex instanceof TaskCancelledException) {
                        // this.logger.WarnException("exception: partition '{0}'", canceledException, this.properties.PartitionKeyRangeId);
                        self.resultException = ex;
                    }
                }

                long remainingWork = delay.toMillis();

                try {
                    while (!cancellationToken.isCancellationRequested() && remainingWork > 0) {
                        Thread.sleep(100);
                        remainingWork -= 100;
                    }
                } catch (InterruptedException iex) {
                    // exception caught
                }
            }
        });
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    private void dispatchChanges(FeedResponse<CosmosItemProperties> response) {
        ChangeFeedObserverContext context = new ChangeFeedObserverContextImpl(this.settings.getPartitionKeyRangeId(), response, this.checkpointer);

        this.observer.processChanges(context, response.results());
    }
}
