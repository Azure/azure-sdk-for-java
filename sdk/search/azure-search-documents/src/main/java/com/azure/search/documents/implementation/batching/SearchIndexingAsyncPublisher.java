// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.BATCH_SIZE_SCALED_DOWN;
import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.calculateRetryDelay;
import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.createDocumentHitRetryLimitException;
import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.createDocumentTooLargeException;
import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.isRetryable;
import static com.azure.search.documents.implementation.batching.SearchBatchingUtils.isSuccess;

/**
 * Internal helper class that manages sending automatic document batches to Azure Search Documents.
 *
 * @param <T> Type of the document in the batch.
 */
public final class SearchIndexingAsyncPublisher<T> {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexingAsyncPublisher.class);

    private final SearchIndexClientImpl restClient;
    private final JsonSerializer serializer;

    private final boolean autoFlush;
    private int batchSize;
    private final int maxRetries;
    private final long throttlingDelayNanos;
    private final long maxThrottlingDelayNanos;

    private final Consumer<OnActionAddedOptions<T>> onActionAdded;
    private final Consumer<OnActionSentOptions<T>> onActionSent;
    private final Consumer<OnActionSucceededOptions<T>> onActionSucceeded;
    private final Consumer<OnActionErrorOptions<T>> onActionError;

    private final Function<T, String> documentKeyRetriever;
    private final Function<Integer, Integer> scaleDownFunction = size -> size / 2;
    private final IndexingDocumentManager<T> documentManager;

    private final Semaphore processingSemaphore = new Semaphore(1, true);

    volatile AtomicInteger backoffCount = new AtomicInteger();
    volatile Duration currentRetryDelay = Duration.ZERO;

    public SearchIndexingAsyncPublisher(SearchIndexClientImpl restClient, JsonSerializer serializer,
        Function<T, String> documentKeyRetriever, boolean autoFlush, int initialBatchActionCount,
        int maxRetriesPerAction, Duration throttlingDelay, Duration maxThrottlingDelay,
        Consumer<OnActionAddedOptions<T>> onActionAdded,
        Consumer<OnActionSucceededOptions<T>> onActionSucceeded,
        Consumer<OnActionErrorOptions<T>> onActionError,
        Consumer<OnActionSentOptions<T>> onActionSent) {
        this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
            "'documentKeyRetriever' cannot be null");

        this.restClient = restClient;
        this.serializer = serializer;
        this.documentManager = new IndexingDocumentManager<>();

        this.autoFlush = autoFlush;
        this.batchSize = initialBatchActionCount;
        this.maxRetries = maxRetriesPerAction;
        this.throttlingDelayNanos = throttlingDelay.toNanos();
        this.maxThrottlingDelayNanos = (maxThrottlingDelay.compareTo(throttlingDelay) < 0)
            ? this.throttlingDelayNanos
            : maxThrottlingDelay.toNanos();

        this.onActionAdded = onActionAdded;
        this.onActionSent = onActionSent;
        this.onActionSucceeded = onActionSucceeded;
        this.onActionError = onActionError;
    }

    public Collection<IndexAction<T>> getActions() {
        return documentManager.getActions();
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Duration getCurrentRetryDelay() {
        return currentRetryDelay;
    }

    public Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context,
        Runnable rescheduleFlush) {
        Tuple2<Integer, Boolean> batchSizeAndAvailable
            = documentManager.addAndCheckForBatch(actions, documentKeyRetriever, onActionAdded, batchSize);

        LOGGER.verbose("Actions added, new pending queue size: {}.", batchSizeAndAvailable.getT1());

        if (autoFlush && batchSizeAndAvailable.getT2()) {
            rescheduleFlush.run();
            LOGGER.verbose("Adding documents triggered batch size limit, sending documents for indexing.");
            return flush(false, false, context);
        }

        return Mono.empty();
    }

    public Mono<Void> flush(boolean awaitLock, boolean isClose, Context context) {
        if (awaitLock) {
            try {
                processingSemaphore.acquire();
            } catch (InterruptedException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            return Mono.using(() -> processingSemaphore, ignored -> flushLoop(isClose, context), Semaphore::release);
        } else if (processingSemaphore.tryAcquire()) {
            return Mono.using(() -> processingSemaphore, ignored -> flushLoop(isClose, context), Semaphore::release);
        } else {
            LOGGER.verbose("Batch already in-flight and not waiting for completion. Performing no-op.");
            return Mono.empty();
        }
    }

    private Mono<Void> flushLoop(boolean isClosed, Context context) {
        return createAndProcessBatch(context, true)
            .expand(ignored -> Flux.defer(() -> createAndProcessBatch(context, isClosed)))
            .then();
    }

    private Mono<IndexBatchResponse> createAndProcessBatch(Context context, boolean ignoreBatchSize) {
        List<TryTrackingIndexAction<T>> batchActions = documentManager.tryCreateBatch(batchSize, ignoreBatchSize);

        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(batchActions)) {
            return Mono.empty();
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = batchActions.stream()
            .map(action -> IndexActionConverter.map(action.getAction(), serializer))
            .collect(Collectors.toList());

        return sendBatch(convertedActions, batchActions, context)
            .map(response -> {
                handleResponse(batchActions, response);

                return response;
            });
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private Mono<IndexBatchResponse> sendBatch(
        List<com.azure.search.documents.implementation.models.IndexAction> actions,
        List<TryTrackingIndexAction<T>> batchActions,
        Context context) {
        LOGGER.verbose("Sending a batch of size {}.", batchActions.size());

        if (onActionSent != null) {
            batchActions.forEach(action -> onActionSent.accept(new OnActionSentOptions<>(action.getAction())));
        }

        Mono<Response<IndexDocumentsResult>> batchCall = Utility.indexDocumentsWithResponseAsync(restClient, actions, true,
            context, LOGGER);

        if (!currentRetryDelay.isZero() && !currentRetryDelay.isNegative()) {
            batchCall = batchCall.delaySubscription(currentRetryDelay);
        }

        return batchCall.map(response -> new IndexBatchResponse(response.getStatusCode(),
            response.getValue().getResults(), actions.size(), false))
            .doOnCancel(() -> {
                LOGGER.warning("Request was cancelled before response, adding all in-flight documents back to queue.");
                documentManager.reinsertCancelledActions(batchActions);
            })
            // Handles mixed success responses.
            .onErrorResume(IndexBatchException.class, exception -> Mono.just(
                new IndexBatchResponse(207, exception.getIndexingResults(), actions.size(), true)))
            .onErrorResume(HttpResponseException.class, exception -> {
                /*
                 * If we received an error response where the payload was too large split it into two smaller payloads
                 * and attempt to index again. If the number of index actions was one raise the error as we cannot split
                 * that any further.
                 */
                int statusCode = exception.getResponse().getStatusCode();
                if (statusCode == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
                    /*
                     * Pass both the sent batch size and the configured batch size. This covers that case where the
                     * sent batch size was smaller than the configured batch size and a 413 was trigger.
                     *
                     * For example, by default the configured batch size defaults to 512 but a batch of 200 may be sent
                     * and trigger 413, if we only halved 512 we'd send the same batch again and 413 a second time.
                     * Instead in this scenario we should halve 200 to 100.
                     */
                    int previousBatchSize = Math.min(batchSize, actions.size());
                    this.batchSize = Math.max(1, scaleDownFunction.apply(previousBatchSize));

                    LOGGER.verbose(BATCH_SIZE_SCALED_DOWN, System.lineSeparator(), previousBatchSize, batchSize);

                    int actionCount = actions.size();
                    if (actionCount == 1) {
                        return Mono.just(new IndexBatchResponse(statusCode, null, actionCount, true));
                    }

                    int splitOffset = Math.min(actions.size(), batchSize);
                    List<TryTrackingIndexAction<T>> batchActionsToRemove = batchActions.subList(splitOffset,
                        batchActions.size());
                    documentManager.reinsertFailedActions(batchActionsToRemove);
                    batchActionsToRemove.clear();

                    return sendBatch(actions.subList(0, splitOffset), batchActions, context);
                }

                return Mono.just(new IndexBatchResponse(statusCode, null, actions.size(), true));
            })
            // General catch all to allow operation to continue.
            .onErrorResume(Exception.class, ignored ->
                Mono.just(new IndexBatchResponse(0, null, actions.size(), true)));
    }

    private void handleResponse(List<TryTrackingIndexAction<T>> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getStatusCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE && batchResponse.getCount() == 1) {
            IndexAction<T> action = actions.get(0).getAction();
            if (onActionError != null) {
                onActionError.accept(new OnActionErrorOptions<>(action)
                    .setThrowable(createDocumentTooLargeException()));
            }
            return;
        }

        List<TryTrackingIndexAction<T>> actionsToRetry = new ArrayList<>();
        boolean has503 = batchResponse.getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE;
        if (batchResponse.getResults() == null) {
            /*
             * Null results indicates that the entire request failed. Retry all documents.
             */
            actionsToRetry.addAll(actions);
        } else {
            /*
             * We got back a result set, correlate responses to their request document and add retryable actions back
             * into the queue.
             */
            for (IndexingResult result : batchResponse.getResults()) {
                String key = result.getKey();
                TryTrackingIndexAction<T> action = actions.stream()
                    .filter(a -> key.equals(a.getKey()))
                    .findFirst()
                    .orElse(null);

                if (action == null) {
                    LOGGER.warning("Unable to correlate result key {} to initial document.", key);
                    continue;
                }

                if (isSuccess(result.getStatusCode())) {
                    if (onActionSucceeded != null) {
                        onActionSucceeded.accept(new OnActionSucceededOptions<>(action.getAction()));
                    }
                } else if (isRetryable(result.getStatusCode())) {
                    has503 |= result.getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE;
                    if (action.getTryCount() < maxRetries) {
                        action.incrementTryCount();
                        actionsToRetry.add(action);
                    } else {
                        if (onActionError != null) {
                            onActionError.accept(new OnActionErrorOptions<>(action.getAction())
                                .setThrowable(createDocumentHitRetryLimitException())
                                .setIndexingResult(result));
                        }
                    }
                } else {
                    if (onActionError != null) {
                        onActionError.accept(new OnActionErrorOptions<>(action.getAction())
                            .setIndexingResult(result));
                    }
                }
            }
        }

        if (has503) {
            currentRetryDelay = calculateRetryDelay(backoffCount.getAndIncrement(), throttlingDelayNanos,
                maxThrottlingDelayNanos);
        } else {
            backoffCount.set(0);
            currentRetryDelay = Duration.ZERO;
        }

        if (!CoreUtils.isNullOrEmpty(actionsToRetry)) {
            documentManager.reinsertFailedActions(actionsToRetry);
        }
    }
}
