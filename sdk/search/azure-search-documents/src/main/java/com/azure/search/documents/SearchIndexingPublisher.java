// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Internal helper class that manages sending automatic document batches to Azure Search Documents.
 *
 * @param <T> Type of the document.
 */
final class SearchIndexingPublisher<T> {
    private final SearchAsyncClient client;
    private final boolean autoFlush;
    private final int batchSize;
    private final int documentTryLimit;

    private final Consumer<IndexAction<T>> onActionAddedConsumer;
    private final Consumer<IndexAction<T>> onActionSentConsumer;
    private final Consumer<IndexAction<T>> onActionSucceededConsumer;
    private final BiConsumer<IndexAction<T>, Throwable> onActionErrorBiConsumer;

    private final Function<T, String> documentKeyRetriever;

    private final Object actionsMutex = new Object();
    private final Deque<TryTrackingIndexAction<T>> actions = new LinkedList<>();

    private final Semaphore processingSemaphore = new Semaphore(1);

    SearchIndexingPublisher(SearchAsyncClient client, SearchIndexingBufferedSenderOptions<T> options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.documentKeyRetriever = Objects.requireNonNull(options.getDocumentKeyRetriever(),
            "'options.documentKeyRetriever' cannot be null");

        this.client = client;
        this.autoFlush = options.getAutoFlush();
        this.batchSize = options.getBatchSize();
        this.documentTryLimit = options.getDocumentTryLimit();

        this.onActionAddedConsumer = (action) -> {
            if (options.getOnActionAdded() != null) {
                options.getOnActionAdded().accept(action);
            }
        };

        this.onActionSentConsumer = (action) -> {
            if (options.getOnActionSent() != null) {
                options.getOnActionSent().accept(action);
            }
        };

        this.onActionSucceededConsumer = (action) -> {
            if (options.getOnActionSucceeded() != null) {
                options.getOnActionSucceeded().accept(action);
            }
        };

        this.onActionErrorBiConsumer = (action, throwable) -> {
            if (options.getOnActionError() != null) {
                options.getOnActionError().accept(action, throwable);
            }
        };
    }

    synchronized Collection<IndexAction<?>> getActions() {
        return actions.stream().map(TryTrackingIndexAction::getAction).collect(Collectors.toList());
    }

    int getBatchSize() {
        return batchSize;
    }

    synchronized Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context, Runnable rescheduleFlush) {
        actions.stream()
            .map(action -> new TryTrackingIndexAction<>(action, documentKeyRetriever.apply(action.getDocument())))
            .forEach(action -> {
                onActionAddedConsumer.accept(action.getAction());
                this.actions.add(action);
            });

        if (autoFlush && batchAvailableForProcessing()) {
            rescheduleFlush.run();
            return flush(context, false);
        }

        return Mono.empty();
    }

    Mono<Void> flush(Context context, boolean awaitLock) {
        if (awaitLock) {
            processingSemaphore.acquireUninterruptibly();
            return createAndProcessBatch(context)
                .doFinally(ignored -> processingSemaphore.release());
        } else if (processingSemaphore.tryAcquire()) {
            return createAndProcessBatch(context)
                .doFinally(ignored -> processingSemaphore.release());
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> createAndProcessBatch(Context context) {
        final List<TryTrackingIndexAction<T>> batchActions = new ArrayList<>(batchSize);
        synchronized (actionsMutex) {
            int size = Math.min(batchSize, this.actions.size());
            for (int i = 0; i < size; i++) {
                TryTrackingIndexAction<T> action = actions.pop();
                onActionSentConsumer.accept(action.getAction());
                batchActions.add(action);
            }
        }

        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(batchActions)) {
            return Mono.empty();
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = batchActions.stream()
            .map(action -> IndexActionConverter.map(action.getAction(), client.serializer))
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
        return client.indexDocumentsWithResponse(actions, true, context)
            .flatMapMany(response -> Flux.just(
                new IndexBatchResponse(response.getValue().getResults(), actionsOffset, actions.size(), false)))
            .onErrorResume(IndexBatchException.class, exception -> Flux
                .just(new IndexBatchResponse(exception.getIndexingResults(), actionsOffset, actions.size(), true)))
            .onErrorResume(HttpResponseException.class, exception -> {
                /*
                 * If we received an error response where the payload was too large split it into two smaller payloads
                 * and attempt to index again. If the number of index actions was one raise the error as we cannot split
                 * that any further.
                 */
                if (exception.getResponse().getStatusCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
                    int actionCount = actions.size();
                    if (actionCount == 1) {
                        return Flux.just(new IndexBatchResponse(null, actionsOffset, actionCount, true));
                    }

                    int splitOffset = Math.round(actionCount / 2.0f);
                    return Flux.concat(
                        sendBatch(actions.subList(0, splitOffset), 0, context),
                        sendBatch(actions.subList(splitOffset, actionCount), splitOffset, context)
                    );
                }

                return Flux.just(new IndexBatchResponse(null, actionsOffset, actions.size(), true));
            });
    }

    private void handleResponse(List<TryTrackingIndexAction<T>> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getResults() == null && batchResponse.getCount() == 1) {
            IndexAction<T> action = actions.get(batchResponse.getOffset()).getAction();
            onActionErrorBiConsumer.accept(action,
                new RuntimeException("Document is too large to be indexed and won't be tried again."));
            return;
        }

        if (batchResponse.getResults() == null) {
            return;
        }

        List<TryTrackingIndexAction<T>> actionsToRetry = new ArrayList<>();
        for (IndexingResult result : batchResponse.getResults()) {
            String key = result.getKey();
            TryTrackingIndexAction<T> action = actions.stream().skip(batchResponse.getOffset())
                .filter(a -> key.equals(a.getKey()))
                .findFirst()
                .get();

            if (isSuccess(result.getStatusCode())) {
                onActionSucceededConsumer.accept(action.getAction());
            } else if (isRetryable(result.getStatusCode())) {
                if (action.getTryCount() < documentTryLimit) {
                    action.incrementTryCount();
                    actionsToRetry.add(action);
                } else {
                    onActionErrorBiConsumer.accept(action.getAction(),
                        new RuntimeException("Document has reached retry limit and won't be tried again."));
                }
            } else {
                onActionErrorBiConsumer.accept(action.getAction(), new RuntimeException(result.getErrorMessage()));
            }
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
        return actions.size() >= batchSize;
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 409 || statusCode == 422 || statusCode == 503;
    }

    /*
     * Helper class which contains the IndexAction and the number of times it has tried to be indexed.
     */
    private static final class TryTrackingIndexAction<T> {
        private final IndexAction<T> action;
        private final String key;

        private int tryCount = 1;

        private TryTrackingIndexAction(IndexAction<T> action, String key) {
            this.action = action;
            this.key = key;
        }

        public IndexAction<T> getAction() {
            return action;
        }

        public String getKey() {
            return key;
        }

        public int getTryCount() {
            return tryCount;
        }

        public void incrementTryCount() {
            tryCount++;
        }
    }

    /*
     * Helper class which keeps track of the service results, the offset from the initial request set if it was split,
     * and whether the response is an error status.
     */
    private static final class IndexBatchResponse {
        private final List<IndexingResult> results;
        private final int offset;
        private final int count;
        private final boolean isError;

        private IndexBatchResponse(List<IndexingResult> results, int offset, int count, boolean isError) {
            this.results = results;
            this.offset = offset;
            this.count = count;
            this.isError = isError;
        }

        public List<IndexingResult> getResults() {
            return results;
        }

        public int getOffset() {
            return offset;
        }

        public int getCount() {
            return count;
        }

        public boolean isError() {
            return isError;
        }
    }
}
