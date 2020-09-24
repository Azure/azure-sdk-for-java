// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains operations for conveniently indexing documents to an Azure Search index.
 *
 * @see SearchBatchClientBuilder
 */
public final class SearchBatchAsyncClient {
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final long DEFAULT_FLUSH_WINDOW = TimeUnit.SECONDS.toMillis(60);
    private static final int TRY_LIMIT = 10;

    private final SearchAsyncClient client;
    private final boolean autoFlush;
    private final long flushWindowMillis;
    private final int batchSize;
    private final IndexingHook indexingHook;
    private final Timer autoFlushTimer;

    private final Object actionsMutex = 0;
    private List<TryTrackingIndexAction> actions = new ArrayList<>();

    private final AtomicReference<TimerTask> flushTask = new AtomicReference<>();

    SearchBatchAsyncClient(SearchAsyncClient client, Boolean autoFlush, Duration flushWindow,
        Integer batchSize, IndexingHook indexingHook) {
        ClientLogger logger = new ClientLogger(SearchBatchAsyncClient.class);

        if (batchSize != null && batchSize < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }

        this.client = client;
        this.autoFlush = autoFlush == null || autoFlush;

        if (flushWindow == null) {
            this.flushWindowMillis = DEFAULT_FLUSH_WINDOW;
        } else if (flushWindow.isZero() || flushWindow.isNegative()) {
            this.flushWindowMillis = 0;
        } else {
            this.flushWindowMillis = flushWindow.toMillis();
        }

        this.batchSize = (batchSize == null) ? DEFAULT_BATCH_SIZE : batchSize;
        this.indexingHook = indexingHook;
        this.autoFlushTimer = (this.autoFlush) ? new Timer(true) : null;
    }

    /**
     * Gets the {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<?>> getActions() {
        return actions.stream().map(TryTrackingIndexAction::getAction).collect(Collectors.toList());
    }

    /**
     * Gets the batch size.
     *
     * @return The batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addUploadActions(Collection<?> documents) {
        return withContext(context -> createAndAddActions(documents, IndexActionType.UPLOAD, context));
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addDeleteActions(Collection<?> documents) {
        return withContext(context -> createAndAddActions(documents, IndexActionType.DELETE, context));
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addMergeActions(Collection<?> documents) {
        return withContext(context -> createAndAddActions(documents, IndexActionType.MERGE, context));
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addMergeOrUploadActions(Collection<?> documents) {
        return withContext(context -> createAndAddActions(documents, IndexActionType.MERGE_OR_UPLOAD, context));
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addActions(Collection<IndexAction<?>> actions) {
        return withContext(context -> addActions(actions, context));
    }

    Mono<Void> createAndAddActions(Collection<?> documents, IndexActionType actionType, Context context) {
        return addActions(createDocumentActions(documents, actionType), context);
    }

    Mono<Void> addActions(Collection<IndexAction<?>> actions, Context context) {
        synchronized (actionsMutex) {
            actions.stream().map(TryTrackingIndexAction::new).forEach(this.actions::add);
        }

        if (indexingHook != null) {
            actions.forEach(indexingHook::actionAdded);
        }

        return processIfNeeded(context);
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @return A reactive response that indicates if the flush operation has completed.
     */
    public Mono<Void> flush() {
        return withContext(this::flush);
    }

    Mono<Void> flush(Context context) {
        List<TryTrackingIndexAction> actions;
        synchronized (actionsMutex) {
            actions = this.actions;
            this.actions = new ArrayList<>();
        }

        // If there are no documents to in the batch to index just return.
        if (CoreUtils.isNullOrEmpty(actions)) {
            return Mono.empty();
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = actions.stream()
            .map(action -> IndexActionConverter.map(action.getAction(), client.serializer))
            .collect(Collectors.toList());

        AtomicBoolean hasError = new AtomicBoolean(false);
        return flushInternal(convertedActions, 0, context)
            .map(response -> {
                handleResponse(actions, response);
                if (response.isError()) {
                    hasError.set(true);
                }

                return response;
            })
            .thenEmpty(Mono.defer(() -> hasError.get()
                ? Mono.error(new RuntimeException("Batching has encountered errors"))
                : Mono.empty()));
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    Flux<IndexBatchResponse> flushInternal(
        List<com.azure.search.documents.implementation.models.IndexAction> actions, int actionsOffset,
        Context context) {
        return client.indexDocumentsWithResponse(actions, true, context)
            .flatMapMany(response -> Flux
                .just(new IndexBatchResponse(response.getValue().getResults(), actionsOffset, actions.size(), false)))
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
                        flushInternal(actions.subList(0, splitOffset), 0, context),
                        flushInternal(actions.subList(splitOffset, actionCount), splitOffset, context)
                    );
                }

                return Flux.just(new IndexBatchResponse(null, actionsOffset, actions.size(), true));
            });
    }

    private void handleResponse(List<TryTrackingIndexAction> actions, IndexBatchResponse batchResponse) {
        /*
         * Batch has been split until it had one document in it and it returned a 413 response.
         */
        if (batchResponse.getResults() == null && batchResponse.getCount() == 1) {
            if (indexingHook != null) {
                IndexAction<?> action = actions.get(batchResponse.getOffset()).getAction();
                indexingHook.actionError(action);
                indexingHook.actionRemoved(action);
            }

            return;
        }

        List<TryTrackingIndexAction> actionsToRetry = new ArrayList<>();
        for (int i = 0; i < batchResponse.getResults().size(); i++) {
            IndexingResult result = batchResponse.getResults().get(i);
            TryTrackingIndexAction action = actions.get(batchResponse.getOffset() + i);

            if (isSuccess(result.getStatusCode())) {
                if (indexingHook != null) {
                    indexingHook.actionSuccess(action.getAction());
                    indexingHook.actionRemoved(action.getAction());
                }
            } else if (isRetryable(result.getStatusCode()) && action.getTryCount() < TRY_LIMIT) {
                action.incrementTryCount();
                actionsToRetry.add(action);
            } else {
                if (indexingHook != null) {
                    indexingHook.actionError(action.getAction());
                    indexingHook.actionRemoved(action.getAction());
                }
            }
        }

        if (!CoreUtils.isNullOrEmpty(actionsToRetry)) {
            synchronized (actionsMutex) {
                this.actions.addAll(actionsToRetry);
            }
        }
    }

    private void rescheduleFlushTask() {

        TimerTask newTask = new TimerTask() {
            @Override
            public void run() {
                flush().subscribe();
            }
        };

        // If the previous flush task exists cancel it. If it has already executed cancel does nothing.
        TimerTask previousTask = this.flushTask.getAndSet(newTask);
        if (previousTask != null) {
            previousTask.cancel();
        }

        this.autoFlushTimer.schedule(newTask, flushWindowMillis);
    }

    private Mono<Void> processIfNeeded(Context context) {
        if (!this.autoFlush) {
            return Mono.empty();
        }

        rescheduleFlushTask();

        if (actions.size() < batchSize) {
            return Mono.empty();
        }

        return flush(context);
    }

    /**
     * Closes the batch, any documents remaining in the batch will be sent to the Search index for indexing.
     *
     * @return A reactive response indicating that the batch has been closed.
     */
    public Mono<Void> close() {
        return withContext(this::close);
    }

    Mono<Void> close(Context context) {
        if (this.autoFlush) {
            TimerTask currentTask = flushTask.get();
            if (currentTask != null) {
                currentTask.cancel();
            }

            autoFlushTimer.cancel();
        }

        return flush(context);
    }

    private static Collection<IndexAction<?>> createDocumentActions(Collection<?> documents,
        IndexActionType actionType) {
        return documents.stream().map(document -> new IndexAction<>()
            .setActionType(actionType)
            .setDocument(document))
            .collect(Collectors.toList());
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
    private static final class TryTrackingIndexAction {
        private final IndexAction<?> action;

        private int tryCount = 1;

        private TryTrackingIndexAction(IndexAction<?> action) {
            this.action = action;
        }

        public IndexAction<?> getAction() {
            return action;
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
