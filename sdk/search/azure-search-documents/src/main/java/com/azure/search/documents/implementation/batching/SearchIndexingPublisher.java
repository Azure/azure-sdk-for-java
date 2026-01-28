// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;
import reactor.util.function.Tuple2;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
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
 * @param <T> The type of document in the batch.
 */
public final class SearchIndexingPublisher<T> {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexingPublisher.class);

    private final SearchClient searchClient;

    private final boolean autoFlush;
    private int batchSize;
    private final int maxRetries;
    private final long throttlingDelayNanos;
    private final long maxThrottlingDelayNanos;

    private final Consumer<OnActionAddedOptions> onActionAdded;
    private final Consumer<OnActionSentOptions> onActionSent;
    private final Consumer<OnActionSucceededOptions> onActionSucceeded;
    private final Consumer<OnActionErrorOptions> onActionError;

    private final Function<Map<String, Object>, String> documentKeyRetriever;
    private final Function<Integer, Integer> scaleDownFunction = size -> size / 2;
    private final IndexingDocumentManager documentManager;

    private final ReentrantLock lock = new ReentrantLock(true);

    volatile AtomicInteger backoffCount = new AtomicInteger();
    volatile Duration currentRetryDelay = Duration.ZERO;

    public SearchIndexingPublisher(SearchClient searchClient,
        Function<Map<String, Object>, String> documentKeyRetriever, boolean autoFlush, int initialBatchActionCount,
        int maxRetriesPerAction, Duration throttlingDelay, Duration maxThrottlingDelay,
        Consumer<OnActionAddedOptions> onActionAdded, Consumer<OnActionSucceededOptions> onActionSucceeded,
        Consumer<OnActionErrorOptions> onActionError, Consumer<OnActionSentOptions> onActionSent) {
        this.documentKeyRetriever
            = Objects.requireNonNull(documentKeyRetriever, "'documentKeyRetriever' cannot be null");

        this.searchClient = searchClient;
        this.documentManager = new IndexingDocumentManager();

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

    public Collection<IndexAction> getActions() {
        return documentManager.getActions();
    }

    public int getBatchSize() {
        return batchSize;
    }

    public Duration getCurrentRetryDelay() {
        return currentRetryDelay;
    }

    public void addActions(Collection<IndexAction> actions, Duration timeout, RequestOptions requestOptions,
        Runnable rescheduleFlush) {
        Tuple2<Integer, Boolean> batchSizeAndAvailable
            = documentManager.addAndCheckForBatch(actions, documentKeyRetriever, onActionAdded, batchSize);

        LOGGER.verbose("Actions added, new pending queue size: {}.", batchSizeAndAvailable.getT1());

        if (autoFlush && batchSizeAndAvailable.getT2()) {
            rescheduleFlush.run();
            LOGGER.verbose("Adding documents triggered batch size limit, sending documents for indexing.");
            flush(false, false, timeout, requestOptions);
        }
    }

    public void flush(boolean awaitLock, boolean isClose, Duration timeout, RequestOptions requestOptions) {
        if (awaitLock) {
            lock.lock();

            try {
                flushLoop(isClose, timeout, requestOptions);
            } finally {
                lock.unlock();
            }
        } else if (lock.tryLock()) {
            try {
                flushLoop(isClose, timeout, requestOptions);
            } finally {
                lock.unlock();
            }
        } else {
            LOGGER.verbose("Batch already in-flight and not waiting for completion. Performing no-op.");
        }
    }

    private void flushLoop(boolean isClosed, Duration timeout, RequestOptions requestOptions) {
        if (timeout != null && !timeout.isNegative() && !timeout.isZero()) {
            final AtomicReference<List<TryTrackingIndexAction>> batchActions = new AtomicReference<>();
            Future<?> future = SharedExecutorService.getInstance()
                .submit(() -> flushLoopHelper(isClosed, requestOptions, batchActions));

            try {
                CoreUtils.getResultWithTimeout(future, timeout);
            } catch (ExecutionException e) {
                Throwable realCause = e.getCause();
                if (realCause instanceof Error) {
                    throw (Error) realCause;
                } else if (realCause instanceof RuntimeException) {
                    throw LOGGER.logExceptionAsError((RuntimeException) realCause);
                } else {
                    throw LOGGER.logExceptionAsError(new RuntimeException(realCause));
                }
            } catch (InterruptedException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            } catch (TimeoutException e) {
                documentManager.reinsertCancelledActions(batchActions.get());

                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        } else {
            flushLoopHelper(isClosed, requestOptions, null);
        }
    }

    private void flushLoopHelper(boolean isClosed, RequestOptions requestOptions,
        AtomicReference<List<TryTrackingIndexAction>> batchActions) {
        List<TryTrackingIndexAction> batch = documentManager.tryCreateBatch(batchSize, true);
        if (batchActions != null) {
            batchActions.set(batch);
        }

        // Process the current batch.
        IndexBatchResponse response = processBatch(batch, requestOptions);

        // Then while a batch has been processed and there are still documents to index, keep processing batches.
        while (response != null && (batch = documentManager.tryCreateBatch(batchSize, isClosed)) != null) {
            if (batchActions != null) {
                batchActions.set(batch);
            }

            response = processBatch(batch, requestOptions);
        }
    }

    private IndexBatchResponse processBatch(List<TryTrackingIndexAction> batchActions, RequestOptions requestOptions) {
        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(batchActions)) {
            return null;
        }

        List<IndexAction> convertedActions
            = batchActions.stream().map(TryTrackingIndexAction::getAction).collect(Collectors.toList());

        IndexBatchResponse response = sendBatch(convertedActions, batchActions, requestOptions);
        handleResponse(batchActions, response);

        return response;
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private IndexBatchResponse sendBatch(List<IndexAction> actions, List<TryTrackingIndexAction> batchActions,
        RequestOptions requestOptions) {
        LOGGER.verbose("Sending a batch of size {}.", batchActions.size());

        if (onActionSent != null) {
            batchActions.forEach(action -> onActionSent.accept(new OnActionSentOptions(action.getAction())));
        }

        if (!currentRetryDelay.isZero() && !currentRetryDelay.isNegative()) {
            sleep(currentRetryDelay.toMillis());
        }

        try {
            Response<IndexDocumentsResult> batchCall
                = searchClient.indexDocumentsWithResponse(new IndexDocumentsBatch(actions), null, requestOptions);
            return new IndexBatchResponse(batchCall.getStatusCode(), batchCall.getValue().getResults(), actions.size(),
                false);
        } catch (IndexBatchException exception) {
            return new IndexBatchResponse(207, exception.getIndexingResults(), actions.size(), true);
        } catch (HttpResponseException exception) {
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
                    return new IndexBatchResponse(statusCode, null, actionCount, true);
                }

                int splitOffset = Math.min(actions.size(), batchSize);
                List<TryTrackingIndexAction> batchActionsToRemove
                    = batchActions.subList(splitOffset, batchActions.size());
                documentManager.reinsertFailedActions(batchActionsToRemove);
                batchActionsToRemove.clear();

                return sendBatch(actions.subList(0, splitOffset), batchActions, requestOptions);
            }

            return new IndexBatchResponse(statusCode, null, actions.size(), true);
        } catch (Exception e) {
            // General catch all to allow operation to continue.
            return new IndexBatchResponse(0, null, actions.size(), true);
        }
    }

    private void handleResponse(List<TryTrackingIndexAction> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getStatusCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE && batchResponse.getCount() == 1) {
            IndexAction action = actions.get(0).getAction();
            if (onActionError != null) {
                onActionError.accept(new OnActionErrorOptions(action).setThrowable(createDocumentTooLargeException()));
            }
            return;
        }

        LinkedList<TryTrackingIndexAction> actionsToRetry = new LinkedList<>();
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
                TryTrackingIndexAction action
                    = actions.stream().filter(a -> key.equals(a.getKey())).findFirst().orElse(null);

                if (action == null) {
                    LOGGER.warning("Unable to correlate result key {} to initial document.", key);
                    continue;
                }

                if (isSuccess(result.getStatusCode())) {
                    if (onActionSucceeded != null) {
                        onActionSucceeded.accept(new OnActionSucceededOptions(action.getAction()));
                    }
                } else if (isRetryable(result.getStatusCode())) {
                    has503 |= result.getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE;
                    if (action.getTryCount() < maxRetries) {
                        action.incrementTryCount();
                        actionsToRetry.add(action);
                    } else {
                        if (onActionError != null) {
                            onActionError.accept(new OnActionErrorOptions(action.getAction())
                                .setThrowable(createDocumentHitRetryLimitException())
                                .setIndexingResult(result));
                        }
                    }
                } else {
                    if (onActionError != null) {
                        onActionError.accept(new OnActionErrorOptions(action.getAction()).setIndexingResult(result));
                    }
                }
            }
        }

        if (has503) {
            currentRetryDelay
                = calculateRetryDelay(backoffCount.getAndIncrement(), throttlingDelayNanos, maxThrottlingDelayNanos);
        } else {
            backoffCount.set(0);
            currentRetryDelay = Duration.ZERO;
        }

        if (!CoreUtils.isNullOrEmpty(actionsToRetry)) {
            documentManager.reinsertFailedActions(actionsToRetry);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
