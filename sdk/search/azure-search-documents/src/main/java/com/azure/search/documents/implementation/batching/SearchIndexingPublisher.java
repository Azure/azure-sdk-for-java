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

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Internal helper class that manages sending automatic document batches to Azure Search Documents.
 *
 * @param <T> Type of the document.
 */
public final class SearchIndexingPublisher<T> {
    private static final double JITTER_FACTOR = 0.05;
    private static final String BATCH_SIZE_SCALED_DOWN =
        "Scaling down batch size due to 413 (Payload too large) response.{}Scaled down from {} to {}";

    private final ClientLogger logger = new ClientLogger(SearchIndexingPublisher.class);

    private final SearchIndexClientImpl restClient;
    private final JsonSerializer serializer;

    private final boolean autoFlush;
    private int batchActionCount;
    private final int maxRetries;
    private final Duration throttlingDelay;
    private final Duration maxThrottlingDelay;

    private final Consumer<OnActionAddedOptions<T>> onActionAddedConsumer;
    private final Consumer<OnActionSentOptions<T>> onActionSentConsumer;
    private final Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer;
    private final Consumer<OnActionErrorOptions<T>> onActionErrorConsumer;

    private final Function<T, String> documentKeyRetriever;
    private final Function<Integer, Integer> scaleDownFunction = size -> size / 2;

    private final Object actionsMutex = new Object();
    private final LinkedList<TryTrackingIndexAction<T>> actions = new LinkedList<>();

    /*
     * This queue keeps track of documents that are currently being sent to the service for indexing. This queue is
     * resilient against cases where the request timeouts or is cancelled by an external operation, preventing the
     * documents from being lost.
     */
    private final LinkedList<TryTrackingIndexAction<T>> inFlightActions = new LinkedList<>();

    private final Semaphore processingSemaphore = new Semaphore(1);

    volatile AtomicInteger backoffCount = new AtomicInteger();
    volatile Duration currentRetryDelay = Duration.ZERO;

    public SearchIndexingPublisher(SearchIndexClientImpl restClient, JsonSerializer serializer,
        Function<T, String> documentKeyRetriever, boolean autoFlush, int initialBatchActionCount,
        int maxRetriesPerAction, Duration throttlingDelay, Duration maxThrottlingDelay,
        Consumer<OnActionAddedOptions<T>> onActionAddedConsumer,
        Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer,
        Consumer<OnActionErrorOptions<T>> onActionErrorConsumer,
        Consumer<OnActionSentOptions<T>> onActionSentConsumer) {
        this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
            "'documentKeyRetriever' cannot be null");

        this.restClient = restClient;
        this.serializer = serializer;

        this.autoFlush = autoFlush;
        this.batchActionCount = initialBatchActionCount;
        this.maxRetries = maxRetriesPerAction;
        this.throttlingDelay = throttlingDelay;
        this.maxThrottlingDelay = (maxThrottlingDelay.compareTo(this.throttlingDelay) < 0)
            ? this.throttlingDelay
            : maxThrottlingDelay;

        this.onActionAddedConsumer = onActionAddedConsumer;
        this.onActionSentConsumer = onActionSentConsumer;
        this.onActionSucceededConsumer = onActionSucceededConsumer;
        this.onActionErrorConsumer = onActionErrorConsumer;
    }

    public synchronized Collection<IndexAction<T>> getActions() {
        List<IndexAction<T>> actions = new ArrayList<>();

        for (TryTrackingIndexAction<T> inFlightAction : inFlightActions) {
            actions.add(inFlightAction.getAction());
        }

        for (TryTrackingIndexAction<T> action : this.actions) {
            actions.add(action.getAction());
        }

        return actions;
    }

    public int getBatchActionCount() {
        return batchActionCount;
    }

    public synchronized Duration getCurrentRetryDelay() {
        return currentRetryDelay;
    }

    public synchronized Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context,
        Runnable rescheduleFlush) {
        actions.stream()
            .map(action -> new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())))
            .forEach(action -> {
                if (onActionAddedConsumer != null) {
                    onActionAddedConsumer.accept(new OnActionAddedOptions<>(action.getAction()));
                }
                this.actions.add(action);
            });

        logger.verbose("Actions added, new pending queue size: {}.", this.actions.size());

        if (autoFlush && batchAvailableForProcessing()) {
            rescheduleFlush.run();
            logger.verbose("Adding documents triggered batch size limit, sending documents for indexing.");
            return flush(false, false, context);
        }

        return Mono.empty();
    }

    public Mono<Void> flush(boolean awaitLock, boolean isClose, Context context) {
        if (awaitLock) {
            processingSemaphore.acquireUninterruptibly();
            return flushLoop(isClose, context)
                .doFinally(ignored -> processingSemaphore.release());
        } else if (processingSemaphore.tryAcquire()) {
            return flushLoop(isClose, context)
                .doFinally(ignored -> processingSemaphore.release());
        } else {
            logger.verbose("Batch already in-flight and not waiting for completion. Performing no-op.");
            return Mono.empty();
        }
    }

    private Mono<Void> flushLoop(boolean isClosed, Context context) {
        return createAndProcessBatch(context)
            .expand(ignored -> Flux.defer(() -> (batchAvailableForProcessing() || isClosed)
                ? createAndProcessBatch(context)
                : Flux.empty()))
            .then();
    }

    private Mono<IndexBatchResponse> createAndProcessBatch(Context context) {
        List<TryTrackingIndexAction<T>> batchActions = createBatch();

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

    private List<TryTrackingIndexAction<T>> createBatch() {
        final List<TryTrackingIndexAction<T>> batchActions;
        final Set<String> keysInBatch;
        synchronized (actionsMutex) {
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
                reinsertFailedActions(inFlightActions);
            } else {
                // Then attempt to fill the batch from documents in the actions queue.
                fillFromQueue(batchActions, actions, size - inFlightDocumentsAdded, keysInBatch);
            }
        }

        return batchActions;
    }

    private int fillFromQueue(List<TryTrackingIndexAction<T>> batch, List<TryTrackingIndexAction<T>> queue,
        int requested, Set<String> duplicateKeyTracker) {
        int offset = 0;
        int actionsAdded = 0;
        int queueSize = queue.size();

        while (actionsAdded < requested && offset < queueSize) {
            TryTrackingIndexAction<T> potentialDocumentToAdd = queue.get(offset++ - actionsAdded);

            if (duplicateKeyTracker.contains(potentialDocumentToAdd.getKey())) {
                continue;
            }

            duplicateKeyTracker.add(potentialDocumentToAdd.getKey());
            batch.add(queue.remove(offset - 1 - actionsAdded));
            actionsAdded += 1;
        }

        return actionsAdded;
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private Mono<IndexBatchResponse> sendBatch(
        List<com.azure.search.documents.implementation.models.IndexAction> actions,
        List<TryTrackingIndexAction<T>> batchActions,
        Context context) {
        logger.verbose("Sending a batch of size {}.", batchActions.size());

        if (onActionSentConsumer != null) {
            batchActions.forEach(action -> onActionSentConsumer.accept(new OnActionSentOptions<>(action.getAction())));
        }

        Mono<Response<IndexDocumentsResult>> batchCall = Utility.indexDocumentsWithResponse(restClient, actions, true,
            context, logger);

        if (!currentRetryDelay.isZero() && !currentRetryDelay.isNegative()) {
            batchCall = batchCall.delaySubscription(currentRetryDelay);
        }

        return batchCall.map(response -> new IndexBatchResponse(response.getStatusCode(),
            response.getValue().getResults(), actions.size(), false))
            .doOnCancel(() -> {
                logger.warning("Request was cancelled before response, adding all in-flight documents back to queue.");
                inFlightActions.addAll(batchActions);
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
                    int previousBatchSize = Math.min(batchActionCount, actions.size());
                    this.batchActionCount = Math.max(1, scaleDownFunction.apply(previousBatchSize));

                    logger.verbose(BATCH_SIZE_SCALED_DOWN, System.lineSeparator(), previousBatchSize, batchActionCount);

                    int actionCount = actions.size();
                    if (actionCount == 1) {
                        return Mono.just(new IndexBatchResponse(statusCode, null, actionCount, true));
                    }

                    int splitOffset = Math.min(actions.size(), batchActionCount);
                    List<TryTrackingIndexAction<T>> batchActionsToRemove = batchActions.subList(splitOffset,
                        batchActions.size());
                    reinsertFailedActions(batchActionsToRemove);
                    batchActionsToRemove.clear();

                    return sendBatch(actions.subList(0, splitOffset), batchActions, context);
                }

                return Mono.just(new IndexBatchResponse(statusCode, null, actions.size(), true));
            })
            // General catch all to allow operation to continue.
            .onErrorResume(Throwable.class, ignored ->
                Mono.just(new IndexBatchResponse(0, null, actions.size(), true)));
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
                    logger.warning("Unable to correlate result key {} to initial document.", key);
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
            currentRetryDelay = calculateRetryDelay(backoffCount.getAndIncrement());
        } else {
            backoffCount.set(0);
            currentRetryDelay = Duration.ZERO;
        }

        if (!CoreUtils.isNullOrEmpty(actionsToRetry)) {
            reinsertFailedActions(actionsToRetry);
        }
    }

    private void reinsertFailedActions(List<TryTrackingIndexAction<T>> actionsToRetry) {
        synchronized (actionsMutex) {
            // Push all actions that need to be retried back into the queue.
            for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
                this.actions.push(actionsToRetry.get(i));
            }
        }
    }

    private boolean batchAvailableForProcessing() {
        return (actions.size() + inFlightActions.size()) >= batchActionCount;
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 409 || statusCode == 422 || statusCode == 503;
    }

    private Duration calculateRetryDelay(int backoffCount) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (throttlingDelay.toNanos() * (1 - JITTER_FACTOR)),
                (long) (throttlingDelay.toNanos() * (1 + JITTER_FACTOR)));

        return Duration.ofNanos(Math.min((1L << backoffCount) * delayWithJitterInNanos, maxThrottlingDelay.toNanos()));
    }

    private static RuntimeException createDocumentTooLargeException() {
        return new RuntimeException("Document is too large to be indexed and won't be tried again.");
    }

    private static RuntimeException createDocumentHitRetryLimitException() {
        return new RuntimeException("Document has reached retry limit and won't be tried again.");
    }
}
