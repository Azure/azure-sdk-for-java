// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchIndexingBufferedSenderOptions;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexBatchException;
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

    private final ClientLogger logger = new ClientLogger(SearchIndexingPublisher.class);

    private final SearchIndexClientImpl restClient;
    private final JsonSerializer serializer;

    private final boolean autoFlush;
    private final int batchActionCount;
    private final int maxRetries;
    private final Duration retryDelay;
    private final Duration maxRetryDelay;

    private final Consumer<OnActionAddedOptions<T>> onActionAddedConsumer;
    private final Consumer<OnActionSentOptions<T>> onActionSentConsumer;
    private final Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer;
    private final Consumer<OnActionErrorOptions<T>> onActionErrorConsumer;

    private final Function<T, String> documentKeyRetriever;

    private final Object actionsMutex = new Object();
    private final LinkedList<TryTrackingIndexAction<T>> actions = new LinkedList<>();

    private final Semaphore processingSemaphore = new Semaphore(1);

    volatile AtomicInteger backoffCount = new AtomicInteger();
    volatile Duration currentRetryDelay = Duration.ZERO;

    public SearchIndexingPublisher(SearchIndexClientImpl restClient, JsonSerializer serializer,
        SearchIndexingBufferedSenderOptions<T> options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.documentKeyRetriever = Objects.requireNonNull(options.getDocumentKeyRetriever(),
            "'options.documentKeyRetriever' cannot be null");

        this.restClient = restClient;
        this.serializer = serializer;

        this.autoFlush = options.getAutoFlush();
        this.batchActionCount = options.getInitialBatchActionCount();
        this.maxRetries = options.getMaxRetriesPerAction();
        this.retryDelay = options.getThrottlingDelay();
        this.maxRetryDelay = (options.getMaxThrottlingDelay().compareTo(retryDelay) < 0)
            ? retryDelay
            : options.getMaxThrottlingDelay();

        this.onActionAddedConsumer = options.getOnActionAdded();
        this.onActionSentConsumer = options.getOnActionSent();
        this.onActionSucceededConsumer = options.getOnActionSucceeded();
        this.onActionErrorConsumer = options.getOnActionError();
    }

    public synchronized Collection<IndexAction<?>> getActions() {
        return actions.stream().map(TryTrackingIndexAction::getAction).collect(Collectors.toList());
    }

    public int getBatchActionCount() {
        return batchActionCount;
    }

    public synchronized Duration getCurrentRetryDelay() {
        return currentRetryDelay;
    }

    public synchronized Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context, Runnable rescheduleFlush) {
        actions.stream()
            .map(action -> new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())))
            .forEach(action -> {
                if (onActionAddedConsumer != null) {
                    onActionAddedConsumer.accept(new OnActionAddedOptions<>(action.getAction()));
                }
                this.actions.add(action);
            });

        if (autoFlush && batchAvailableForProcessing()) {
            rescheduleFlush.run();
            return flush(context, false);
        }

        return Mono.empty();
    }

    public Mono<Void> flush(Context context, boolean awaitLock) {
        if (awaitLock) {
            processingSemaphore.acquireUninterruptibly();
            return createAndProcessBatch(context)
                .doFinally(ignored -> processingSemaphore.release());
        } else if (processingSemaphore.tryAcquire()) {
            return createAndProcessBatch(context)
                .doFinally(ignored -> processingSemaphore.release());
        } else {
            logger.verbose("Batch already in-flight and not waiting for completion. Performing no-op.");
            return Mono.empty();
        }
    }

    private Mono<Void> createAndProcessBatch(Context context) {
        final List<TryTrackingIndexAction<T>> batchActions;
        final Set<String> keysInBatch;
        synchronized (actionsMutex) {
            int actionSize = this.actions.size();
            int size = Math.min(batchActionCount, actionSize);
            batchActions = new ArrayList<>(size);
            keysInBatch = new HashSet<>(size * 2);

            int offset = 0;
            int actionsAdded = 0;
            while (actionsAdded < size && offset < actionSize) {
                TryTrackingIndexAction<T> potentialDocumentToAdd = actions.get(offset++ - actionsAdded);

                if (keysInBatch.contains(potentialDocumentToAdd.getKey())) {
                    continue;
                }

                keysInBatch.add(potentialDocumentToAdd.getKey());
                batchActions.add(actions.remove(offset - 1 - actionsAdded));
                actionsAdded += 1;
            }
        }

        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(batchActions)) {
            return Mono.empty();
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = batchActions.stream()
            .map(action -> {
                if (onActionSentConsumer != null) {
                    onActionSentConsumer.accept(new OnActionSentOptions<>(action.getAction()));
                }

                return IndexActionConverter.map(action.getAction(), serializer);
            })
            .collect(Collectors.toList());

        return sendBatch(convertedActions, 0, context)
            .map(response -> {
                handleResponse(batchActions, response);

                return response;
            }).then(Mono.defer(() -> batchAvailableForProcessing()
                ? createAndProcessBatch(context)
                : Mono.empty()));
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private Flux<IndexBatchResponse> sendBatch(
        List<com.azure.search.documents.implementation.models.IndexAction> actions, int actionsOffset,
        Context context) {
        return Utility.indexDocumentsWithResponse(restClient, actions, true, context, logger)
            .delaySubscription(currentRetryDelay)
            .flatMapMany(response -> Flux.just(
                new IndexBatchResponse(response.getStatusCode(), response.getValue().getResults(), actionsOffset,
                    actions.size(), false)))
            .onErrorResume(IndexBatchException.class, exception -> Flux
                .just(new IndexBatchResponse(207, exception.getIndexingResults(), actionsOffset, actions.size(), true)))
            .onErrorResume(HttpResponseException.class, exception -> {
                /*
                 * If we received an error response where the payload was too large split it into two smaller payloads
                 * and attempt to index again. If the number of index actions was one raise the error as we cannot split
                 * that any further.
                 */
                int statusCode = exception.getResponse().getStatusCode();
                if (statusCode == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
                    int actionCount = actions.size();
                    if (actionCount == 1) {
                        return Flux.just(new IndexBatchResponse(statusCode, null, actionsOffset, actionCount, true));
                    }

                    int splitOffset = Math.round(actionCount / 2.0f);
                    return Flux.concat(
                        sendBatch(actions.subList(0, splitOffset), 0, context),
                        sendBatch(actions.subList(splitOffset, actionCount), splitOffset, context)
                    );
                }

                return Flux.just(new IndexBatchResponse(statusCode, null, actionsOffset, actions.size(), true));
            });
    }

    private void handleResponse(List<TryTrackingIndexAction<T>> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getStatusCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE && batchResponse.getCount() == 1) {
            IndexAction<T> action = actions.get(batchResponse.getOffset()).getAction();
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
            int offset = batchResponse.getOffset();
            actionsToRetry.addAll(actions.subList(offset, offset + batchResponse.getCount()));
        } else {
            /*
             * We got back a result set, correlate responses to their request document and add retryable actions back
             * into the queue.
             */
            for (IndexingResult result : batchResponse.getResults()) {
                String key = result.getKey();
                TryTrackingIndexAction<T> action = actions.stream().skip(batchResponse.getOffset())
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
            synchronized (actionsMutex) {
                // Push all actions that need to be retried back into the queue.
                for (int i = actionsToRetry.size() - 1; i >= 0; i--) {
                    this.actions.push(actionsToRetry.get(i));
                }
            }
        }
    }

    private boolean batchAvailableForProcessing() {
        return actions.size() >= batchActionCount;
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
            .nextLong((long) (retryDelay.toNanos() * (1 - JITTER_FACTOR)),
                (long) (retryDelay.toNanos() * (1 + JITTER_FACTOR)));

        return Duration.ofNanos(Math.min((1L << backoffCount) * delayWithJitterInNanos, maxRetryDelay.toNanos()));
    }

    private static RuntimeException createDocumentTooLargeException() {
        return new RuntimeException("Document is too large to be indexed and won't be tried again.");
    }

    private static RuntimeException createDocumentHitRetryLimitException() {
        return new RuntimeException("Document has reached retry limit and won't be tried again.");
    }
}
