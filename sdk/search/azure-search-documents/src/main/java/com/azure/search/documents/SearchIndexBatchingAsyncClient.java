// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexingResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 *
 */
public class SearchIndexBatchingAsyncClient {
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final SearchAsyncClient client;
    private final boolean isAutoFlushEnabled;
    private final long flushWindowMillis;
    private final int batchSize;
    private final DocumentPersister documentPersister;
    private final Timer autoFlushTimer;

    private final Object actionsMutex = 0;
    private List<IndexAction<?>> actions = new ArrayList<>();

    private final Object successfulActionsMutex = 0;
    private List<IndexAction<?>> successfulActions = new ArrayList<>();

    private final Object failedActionsMutex = 0;
    private List<IndexAction<?>> failedActions = new ArrayList<>();

    private TimerTask flushTask;

    SearchIndexBatchingAsyncClient(SearchAsyncClient client, Integer flushWindow, Integer batchSize,
        DocumentPersister documentPersister) {
        ClientLogger logger = new ClientLogger(SearchIndexBatchingAsyncClient.class);
        if (flushWindow != null && flushWindow < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'flushWindow' cannot be less than zero."));
        }

        if (batchSize != null && batchSize < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }

        this.client = client;

        if (flushWindow != null) {
            this.isAutoFlushEnabled = flushWindow > 0;
            this.flushWindowMillis = TimeUnit.SECONDS.toMillis(flushWindow);
        } else {
            this.isAutoFlushEnabled = false;
            this.flushWindowMillis = 0;
        }

        this.batchSize = (batchSize == null) ? DEFAULT_BATCH_SIZE : batchSize;
        this.documentPersister = documentPersister;

        this.autoFlushTimer = (this.isAutoFlushEnabled) ? new Timer(true) : null;
    }

    /**
     * Gets the {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<?>> getActions() {
        return actions;
    }

    /**
     * Gets the {@link IndexAction IndexActions} that have been successfully indexed.
     * <p>
     * This returns the actions that have succeeded since the last time this method was called. Every time this is
     * called the queue of succeeded actions is drained.
     *
     * @return The {@link IndexAction IndexActions} in the batch that have been successfully indexed.
     */
    public Collection<IndexAction<?>> getSucceededActions() {
        synchronized (successfulActionsMutex) {
            List<IndexAction<?>> actions = successfulActions;
            successfulActions = new ArrayList<>();
            return actions;
        }
    }

    /**
     * Gets the {@link IndexAction IndexActions} that have failed indexing and aren't able to be retried.
     * <p>
     * This returns the actions that have failed since the last time this method was called. Every time this is called
     * the queue of failed actions is drained.
     *
     * @return The {@link IndexAction IndexActions} that have failed indexing and aren't able to be retried.
     */
    public Collection<IndexAction<?>> getFailedActions() {
        synchronized (failedActionsMutex) {
            List<IndexAction<?>> actions = failedActions;
            failedActions = new ArrayList<>();
            return actions;
        }
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
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.UPLOAD);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
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
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.DELETE);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
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
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.MERGE);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
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
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.MERGE_OR_UPLOAD);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
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
        this.actions.addAll(actions);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(actions);
        }

        return flushIfNeeded();
    }

    private Collection<IndexAction<?>> createDocumentActions(Collection<?> documents, IndexActionType actionType) {
        return documents.stream().map(document -> new IndexAction<>()
            .setActionType(actionType)
            .setDocument(document))
            .collect(Collectors.toList());
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param throwOnAnyFailure Flag indicating if the batch should raise an error if any documents in the batch fail to
     * index.
     * @return A reactive response that indicates if the flush operation has completed.
     */
    public Mono<Void> flush(boolean throwOnAnyFailure) {
        List<IndexAction<?>> actions;
        synchronized (actionsMutex) {
            actions = this.actions;
            this.actions = new ArrayList<>();
        }

        List<com.azure.search.documents.implementation.models.IndexAction> convertedActions = actions.stream()
            .map(action -> IndexActionConverter.map(action, client.serializer))
            .collect(Collectors.toList());

        AtomicBoolean hasError = new AtomicBoolean(false);
        return withContext(context -> flushInternal(convertedActions, 0, throwOnAnyFailure, context)
            .map(response -> {
                handleResponse(actions, response.getResults(), response.getOffset());
                if (response.isError()) {
                    hasError.set(true);
                }

                return response;
            }).then(Mono.fromCallable(() -> hasError.get()
                ? Mono.error(new RuntimeException("Batching has encountered errors"))
                : Mono.empty())));
    }

    /*
     * This may result in more than one service call in the case where the index batch is too large and we attempt to
     * split it.
     */
    private Flux<IndexBatchResponse> flushInternal(
        List<com.azure.search.documents.implementation.models.IndexAction> actions, int actionsOffset,
        boolean throwOnAnyError, Context context) {
        return client.indexDocumentsWithResponse(actions, throwOnAnyError, context)
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
                        flushInternal(actions.subList(0, splitOffset), 0, throwOnAnyError, context),
                        flushInternal(actions.subList(splitOffset, actionCount), splitOffset, throwOnAnyError, context)
                    );
                }

                return Flux.just(new IndexBatchResponse(null, actionsOffset, actions.size(), true));
            });
    }

    private void handleResponse(List<IndexAction<?>> actions, List<IndexingResult> results, int offset) {
        for (int i = 0; i < results.size(); i++) {
            IndexingResult result = results.get(i);
            IndexAction<?> action = actions.get(offset + i);

            if (result.getStatusCode() == 200 || result.getStatusCode() == 201) {
                if (documentPersister != null) {
                    documentPersister.removeQueuedAction(action);
                    documentPersister.addSucceededAction(action);
                }

                successfulActions.add(action);
            } else if (result.getStatusCode() == 409
                || result.getStatusCode() == 422
                || result.getStatusCode() == 503) {
                this.actions.add(action);
            } else {
                if (documentPersister != null) {
                    documentPersister.removeQueuedAction(action);
                    documentPersister.addFailedAction(action);
                }

                failedActions.add(action);
            }
        }
    }

    private void rescheduleFlushTask() {
        /*
         * If there is a current flush task cancel and nullify it. This will allow for the auto-flush timer to garbage
         * collect it. If the task has already executed cancel won't do anything.
         */
        if (this.flushTask != null) {
            this.flushTask.cancel();
            this.flushTask = null;
        }

        this.flushTask = new TimerTask() {
            @Override
            public void run() {
                flush(false);
            }
        };

        this.autoFlushTimer.schedule(flushTask, flushWindowMillis);
    }

    private Mono<Void> flushIfNeeded() {
        if (!this.isAutoFlushEnabled) {
            return Mono.empty();
        }

        rescheduleFlushTask();

        if (actions.size() < batchSize) {
            return Mono.empty();
        }

        return flush(false);
    }

    /**
     * Closes the batch, any documents remaining in the batch will be sent to the Search index for indexing.
     *
     * @return A reactive response indicating that the batch has been closed.
     */
    public Mono<Void> close() {
        if (isAutoFlushEnabled) {
            flushTask.cancel();
            flushTask = null;

            autoFlushTimer.cancel();
        }

        return flush(false);
    }

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
