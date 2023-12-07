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

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
<<<<<<< HEAD
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
=======
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    private static final ExecutorService EXECUTOR =  getThreadPoolWithShutdownHook();

    private final SearchIndexClientImpl restClient;
    private final JsonSerializer serializer;

    private final boolean autoFlush;
    private int batchActionCount;
    private final int maxRetries;
    private final long throttlingDelayNanos;
    private final long maxThrottlingDelayNanos;

    private final Consumer<OnActionAddedOptions<T>> onActionAddedConsumer;
    private final Consumer<OnActionSentOptions<T>> onActionSentConsumer;
    private final Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer;
    private final Consumer<OnActionErrorOptions<T>> onActionErrorConsumer;

    private final Function<T, String> documentKeyRetriever;
    private final Function<Integer, Integer> scaleDownFunction = size -> size / 2;
<<<<<<< HEAD
    private final IndexingDocumentManager<T> documentManager;
=======

    private final Deque<TryTrackingIndexAction<T>> actions = new ConcurrentLinkedDeque<>();

    /*
     * This queue keeps track of documents that are currently being sent to the service for indexing. This queue is
     * resilient against cases where the request timeouts or is cancelled by an external operation, preventing the
     * documents from being lost.
     */
    private final Deque<TryTrackingIndexAction<T>> inFlightActions = new ConcurrentLinkedDeque<>();
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5

    private final Semaphore actionsSemaphore = new Semaphore(1);
    private final Semaphore processingSemaphore = new Semaphore(1, true);

    volatile AtomicInteger backoffCount = new AtomicInteger();
    volatile Duration currentRetryDelay = Duration.ZERO;

    public SearchIndexingPublisher(SearchIndexClientImpl restClient, JsonSerializer serializer,
                                       Function<T, String> documentKeyRetriever, boolean autoFlush,
                                       int initialBatchActionCount, int maxRetriesPerAction, Duration throttlingDelay,
                                       Duration maxThrottlingDelay,
                                       Consumer<OnActionAddedOptions<T>> onActionAddedConsumer,
                                       Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer,
                                       Consumer<OnActionErrorOptions<T>> onActionErrorConsumer,
                                       Consumer<OnActionSentOptions<T>> onActionSentConsumer) {
        this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
            "'documentKeyRetriever' cannot be null");

        this.restClient = restClient;
        this.serializer = serializer;
        this.documentManager = new IndexingDocumentManager<>();

        this.autoFlush = autoFlush;
        this.batchActionCount = initialBatchActionCount;
        this.maxRetries = maxRetriesPerAction;
        this.throttlingDelayNanos = throttlingDelay.toNanos();
        this.maxThrottlingDelayNanos = (maxThrottlingDelay.compareTo(throttlingDelay) < 0)
            ? this.throttlingDelayNanos
            : maxThrottlingDelay.toNanos();

        this.onActionAddedConsumer = onActionAddedConsumer;
        this.onActionSentConsumer = onActionSentConsumer;
        this.onActionSucceededConsumer = onActionSucceededConsumer;
        this.onActionErrorConsumer = onActionErrorConsumer;
    }

    public Collection<IndexAction<T>> getActions() {
<<<<<<< HEAD
        return documentManager.getActions();
=======
        acquireActionsSemaphore();
        try {
            List<IndexAction<T>> actions = new ArrayList<>();

            for (TryTrackingIndexAction<T> inFlightAction : inFlightActions) {
                actions.add(inFlightAction.getAction());
            }

            for (TryTrackingIndexAction<T> action : this.actions) {
                actions.add(action.getAction());
            }

            return actions;
        } finally {
            actionsSemaphore.release();
        }
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5
    }

    public int getBatchActionCount() {
        return batchActionCount;
    }

    public Duration getCurrentRetryDelay() {
        return currentRetryDelay;
    }

<<<<<<< HEAD
    public void addActions(Collection<IndexAction<T>> actions, Duration timeout, Context context,
        Runnable rescheduleFlush) {
        int actionCount = documentManager.add(actions, documentKeyRetriever, onActionAddedConsumer);
=======
    public Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context,
        Runnable rescheduleFlush) {
        try {
            actionsSemaphore.acquire();
        } catch (InterruptedException ex) {
            return Mono.error(ex);
        }

        try {
            actions
                .stream()
                .map(action -> new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())))
                .forEach(action -> {
                    if (onActionAddedConsumer != null) {
                        onActionAddedConsumer.accept(new OnActionAddedOptions<>(action.getAction()));
                    }
                    this.actions.add(action);
                });
        } finally {
            actionsSemaphore.release();
        }
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5

        LOGGER.verbose("Actions added, new pending queue size: {}.", actionCount);

        if (autoFlush && documentManager.batchAvailableForProcessing(batchActionCount)) {
            rescheduleFlush.run();
            LOGGER.verbose("Adding documents triggered batch size limit, sending documents for indexing.");
            flush(false, false, timeout, context);
        }
    }

    public void flush(boolean awaitLock, boolean isClose, Duration timeout, Context context) {
        if (awaitLock) {
<<<<<<< HEAD
            processingSemaphore.acquireUninterruptibly();
            try {
                flushLoop(isClose, timeout, context);
            } finally {
                processingSemaphore.release();
            }
        } else if (processingSemaphore.tryAcquire()) {
            try {
                flushLoop(isClose, timeout, context);
            } finally {
                processingSemaphore.release();
            }
=======
            try {
                processingSemaphore.acquire();
            } catch (InterruptedException ex) {
                return Mono.error(ex);
            }

            return Mono.using(() -> processingSemaphore, ignored -> {
                try {
                    return flushLoop(isClose, context);
                } catch (RuntimeException ex) {
                    return Mono.error(ex);
                }
            }, Semaphore::release);
        } else if (processingSemaphore.tryAcquire()) {
            return Mono.using(() -> processingSemaphore, ignored -> {
                try {
                    return flushLoop(isClose, context);
                } catch (RuntimeException ex) {
                    return Mono.error(ex);
                }
            }, Semaphore::release);
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5
        } else {
            LOGGER.verbose("Batch already in-flight and not waiting for completion. Performing no-op.");
        }
    }

    private void flushLoop(boolean isClosed, Duration timeout, Context context) {
        if (timeout != null && !timeout.isNegative() && !timeout.isZero()) {
            final AtomicReference<List<TryTrackingIndexAction<T>>> batchActions = new AtomicReference<>();
            Future<?> future = EXECUTOR.submit(() -> flushLoopHelper(isClosed, context, batchActions));

            try {
                future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                Throwable realCause = e.getCause();
                if (realCause instanceof Error) {
                    throw (Error) realCause;
                } else if (realCause instanceof RuntimeException) {
                    throw LOGGER.logExceptionAsError((RuntimeException) realCause);
                } else {
                    throw LOGGER.logExceptionAsError(new RuntimeException(realCause));
                }
            } catch (InterruptedException | TimeoutException e) {
                if (e instanceof TimeoutException) {
                    future.cancel(true);
                    documentManager.reinsertCancelledActions(batchActions.get());
                }

                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        } else {
            flushLoopHelper(isClosed, context, null);
        }
    }

    private void flushLoopHelper(boolean isClosed, Context context,
        AtomicReference<List<TryTrackingIndexAction<T>>> batchActions) {
        List<TryTrackingIndexAction<T>> batch = documentManager.createBatch(batchActionCount);
        if (batchActions != null) {
            batchActions.set(batch);
        }

        // Process the current batch.
        IndexBatchResponse response = processBatch(batch, context);

        // Then while a batch has been processed and there are still documents to index, keep processing batches.
        while (response != null && (documentManager.batchAvailableForProcessing(batchActionCount) || isClosed)) {
            batch = documentManager.createBatch(batchActionCount);
            if (batchActions != null) {
                batchActions.set(batch);
            }

            response = processBatch(batch, context);
        }
    }

    private IndexBatchResponse processBatch(List<TryTrackingIndexAction<T>> batchActions, Context context) {
        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(batchActions)) {
            return null;
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = batchActions.stream()
            .map(action -> IndexActionConverter.map(action.getAction(), serializer))
            .collect(Collectors.toList());

        IndexBatchResponse response = sendBatch(convertedActions, batchActions, context);
        handleResponse(batchActions, response);

<<<<<<< HEAD
        return response;
=======
                return response;
            });
    }

    private List<TryTrackingIndexAction<T>> createBatch() {
        final List<TryTrackingIndexAction<T>> batchActions;
        final Set<String> keysInBatch;

        acquireActionsSemaphore();
        try {
            int actionSize = this.actions.size();
            int inFlightActionSize = this.inFlightActions.size();
            int size = Math.min(batchActionCount, actionSize + inFlightActionSize);
            batchActions = new ArrayList<>(size);

            // Make the set size larger than the expected batch size to prevent a resizing scenario. Don't use a load
            // factor of 1 as that would potentially cause collisions.
            keysInBatch = new HashSet<>(size * 2);

            // First attempt to fill the batch from documents that were lost in-flight.
            int inFlightDocumentsAdded = fillFromQueue(batchActions, inFlightActions, size, keysInBatch);

            // If the batch is filled using documents lost in-flight add the remaining back to the queue.
            if (inFlightDocumentsAdded == size) {
                reinsertFailedActions(inFlightActions, false);
            } else {
                // Then attempt to fill the batch from documents in the actions queue.
                fillFromQueue(batchActions, actions, size - inFlightDocumentsAdded, keysInBatch);
            }

            return batchActions;
        } finally {
            actionsSemaphore.release();
        }
    }

    private static <T> int fillFromQueue(List<TryTrackingIndexAction<T>> batch, Deque<TryTrackingIndexAction<T>> queue,
        int requested, Set<String> duplicateKeyTracker) {
        int actionsAdded = 0;

        Iterator<TryTrackingIndexAction<T>> iterator = queue.iterator();
        while (actionsAdded < requested && iterator.hasNext()) {
            TryTrackingIndexAction<T> potentialDocumentToAdd = iterator.next();

            if (duplicateKeyTracker.contains(potentialDocumentToAdd.getKey())) {
                continue;
            }

            duplicateKeyTracker.add(potentialDocumentToAdd.getKey());
            batch.add(potentialDocumentToAdd);
            iterator.remove();
            actionsAdded += 1;
        }

        return actionsAdded;
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private IndexBatchResponse sendBatch(List<com.azure.search.documents.implementation.models.IndexAction> actions,
                                         List<TryTrackingIndexAction<T>> batchActions, Context context) {
        LOGGER.verbose("Sending a batch of size {}.", batchActions.size());

        if (onActionSentConsumer != null) {
            batchActions.forEach(action -> onActionSentConsumer.accept(new OnActionSentOptions<>(action.getAction())));
        }

<<<<<<< HEAD
        if (!currentRetryDelay.isZero() && !currentRetryDelay.isNegative()) {
            sleep(currentRetryDelay.toMillis());
=======
        Mono<Response<IndexDocumentsResult>> batchCall = Utility.indexDocumentsWithResponseAsync(restClient, actions, true,
            context, LOGGER);

        Duration delay = currentRetryDelay;
        if (!delay.isZero() && !delay.isNegative()) {
            batchCall = batchCall.delaySubscription(delay);
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5
        }

        try {
            Response<IndexDocumentsResult> batchCall = Utility.indexDocumentsWithResponse(restClient, actions, true,
                context, LOGGER);
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
                int previousBatchSize = Math.min(batchActionCount, actions.size());
                this.batchActionCount = Math.max(1, scaleDownFunction.apply(previousBatchSize));

                LOGGER.verbose(BATCH_SIZE_SCALED_DOWN, System.lineSeparator(), previousBatchSize, batchActionCount);

                int actionCount = actions.size();
                if (actionCount == 1) {
                    return new IndexBatchResponse(statusCode, null, actionCount, true);
                }

                int splitOffset = Math.min(actions.size(), batchActionCount);
                List<TryTrackingIndexAction<T>> batchActionsToRemove = batchActions.subList(splitOffset,
                    batchActions.size());
                documentManager.reinsertFailedActions(batchActionsToRemove);
                batchActionsToRemove.clear();

                return sendBatch(actions.subList(0, splitOffset), batchActions, context);
            }

            return new IndexBatchResponse(statusCode, null, actions.size(), true);
        } catch (Exception e) {
            // General catch all to allow operation to continue.
            return new IndexBatchResponse(0, null, actions.size(), true);
        }
    }

    private void handleResponse(List<TryTrackingIndexAction<T>> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getStatusCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE && batchResponse.getCount() == 1) {
            IndexAction<T> action = actions.get(0).getAction();
            if (onActionErrorConsumer != null) {
                onActionErrorConsumer.accept(new OnActionErrorOptions<>(action)
                    .setThrowable(createDocumentTooLargeException()));
            }
            return;
        }

        Deque<TryTrackingIndexAction<T>> actionsToRetry = new LinkedList<>();
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
                    if (onActionSucceededConsumer != null) {
                        onActionSucceededConsumer.accept(new OnActionSucceededOptions<>(action.getAction()));
                    }
                } else if (isRetryable(result.getStatusCode())) {
                    has503 |= result.getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE;
                    if (action.getTryCount() < maxRetries) {
                        action.incrementTryCount();
                        actionsToRetry.add(action);
                    } else {
                        if (onActionErrorConsumer != null) {
                            onActionErrorConsumer.accept(new OnActionErrorOptions<>(action.getAction())
                                .setThrowable(createDocumentHitRetryLimitException())
                                .setIndexingResult(result));
                        }
                    }
                } else {
                    if (onActionErrorConsumer != null) {
                        onActionErrorConsumer.accept(new OnActionErrorOptions<>(action.getAction())
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
<<<<<<< HEAD
            documentManager.reinsertFailedActions(actionsToRetry);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static ExecutorService getThreadPoolWithShutdownHook() {
        ExecutorService threadPool = Executors.newCachedThreadPool();

        long halfTimeout = TimeUnit.SECONDS.toNanos(5) / 2;
        Thread hook = new Thread(() -> {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(halfTimeout, TimeUnit.NANOSECONDS)) {
                    threadPool.shutdownNow();
                    threadPool.awaitTermination(halfTimeout, TimeUnit.NANOSECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                threadPool.shutdown();
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
=======
            reinsertFailedActions(actionsToRetry, true);
        }
    }

    private void reinsertFailedActions(Deque<TryTrackingIndexAction<T>> actionsToRetry, boolean acquireSemaphore) {
        if (acquireSemaphore) {
            acquireActionsSemaphore();
            try {
                // Push all actions that need to be retried back into the queue.
                actionsToRetry.descendingIterator().forEachRemaining(actions::add);
            } finally {
                actionsSemaphore.release();
            }
        } else {
            // Push all actions that need to be retried back into the queue.
            actionsToRetry.descendingIterator().forEachRemaining(actions::add);
        }
    }

    private void reinsertFailedActions(List<TryTrackingIndexAction<T>> actionsToRetry) {
        acquireActionsSemaphore();
        try {
            // Push all actions that need to be retried back into the queue.
            for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
                this.actions.push(actionsToRetry.get(i));
            }
        } finally {
            actionsSemaphore.release();
        }
    }

    private void acquireActionsSemaphore() {
        try {
            actionsSemaphore.acquire();
        } catch (InterruptedException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }
>>>>>>> 8ff149de68e50caa6e7875c7d719b1400fb006e5

        return threadPool;
    }
}
