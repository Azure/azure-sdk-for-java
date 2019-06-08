/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.ChangeFeedObserver;
import com.microsoft.azure.cosmos.ChangeFeedObserverContext;
import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmos.changefeed.CancellationToken;
import com.microsoft.azure.cosmos.changefeed.ChangeFeedContextClient;
import com.microsoft.azure.cosmos.changefeed.PartitionCheckpointer;
import com.microsoft.azure.cosmos.changefeed.PartitionProcessor;
import com.microsoft.azure.cosmos.changefeed.ProcessorSettings;
import com.microsoft.azure.cosmos.changefeed.exceptions.PartitionNotFoundException;
import com.microsoft.azure.cosmos.changefeed.exceptions.PartitionSplitException;
import com.microsoft.azure.cosmos.changefeed.exceptions.TaskCancelledException;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Implementation for {@link PartitionProcessor}.
 */
public class PartitionProcessorImpl implements PartitionProcessor {
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
        this.options.setMaxItemCount(settings.getMaxItemCount());
        this.options.setPartitionKeyRangeId(settings.getPartitionKeyRangeId());
        // this.options.setSessionToken(settings.getSessionToken());
        this.options.setStartFromBeginning(settings.isStartFromBeginning());
        this.options.setRequestContinuation(settings.getStartContinuation());
        this.options.setStartDateTime(settings.getStartTime());

        //this.query = documentClient.createDocumentChangeFeedQuery(self.settings.getCollectionSelfLink(), this.options);
    }

    @Override
    public Mono<Void> run(CancellationToken cancellationToken) {
        PartitionProcessorImpl self = this;
        this.lastContinuation = this.settings.getStartContinuation();

        return Mono.fromRunnable( () -> {
            while (!cancellationToken.isCancellationRequested()) {
                Duration delay = self.settings.getFeedPollDelay();

                try {
                    self.options.setRequestContinuation(self.lastContinuation);
                    List<FeedResponse<CosmosItemSettings>> documentFeedResponseList = self.documentClient.createDocumentChangeFeedQuery(self.settings.getCollectionSelfLink(), self.options)
                        .collectList()
                        .block();

                    for (FeedResponse<CosmosItemSettings> documentFeedResponse : documentFeedResponseList) {
                        self.lastContinuation = documentFeedResponse.getResponseContinuation();
                        if (documentFeedResponse.getResults() != null && documentFeedResponse.getResults().size() > 0) {
                            self.dispatchChanges(documentFeedResponse);
                        }

                        self.options.setRequestContinuation(self.lastContinuation);

                        if (cancellationToken.isCancellationRequested()) {
                            // Observation was cancelled.
                            throw new TaskCancelledException();
                        }
                    }

                    if (this.options.getMaxItemCount().compareTo(this.settings.getMaxItemCount()) == 0) {
                        this.options.setMaxItemCount(this.settings.getMaxItemCount());   // Reset after successful execution.
                    }
                } catch (RuntimeException ex) {
                    if (ex.getCause() instanceof DocumentClientException) {

                        DocumentClientException clientException = (DocumentClientException) ex.getCause();
                        // this.logger.WarnException("exception: partition '{0}'", clientException, this.settings.PartitionKeyRangeId);
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
                                if (this.options.getMaxItemCount() == null) {
                                    this.options.setMaxItemCount(DefaultMaxItemCount);
                                } else if (this.options.getMaxItemCount() <= 1) {
                                    // this.logger.ErrorFormat("Cannot reduce maxItemCount further as it's already at {0}.", this.options.MaxItemCount);
                                    throw ex;
                                }

                                this.options.setMaxItemCount(this.options.getMaxItemCount() / 2);
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
                        // this.logger.WarnException("exception: partition '{0}'", canceledException, this.settings.PartitionKeyRangeId);
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

    private void dispatchChanges(FeedResponse<CosmosItemSettings> response) {
        ChangeFeedObserverContext context = new ChangeFeedObserverContextImpl(this.settings.getPartitionKeyRangeId(), response, this.checkpointer);

        this.observer.processChanges(context, response.getResults());
    }
}
