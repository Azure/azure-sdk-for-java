// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class SearchIndexDocumentBatchingAsyncClient {
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final ClientLogger logger = new ClientLogger(SearchIndexDocumentBatchingAsyncClient.class);

    private final SearchAsyncClient client;
    private final boolean isAutoFlushEnabled;
    private final long flushWindowMillis;
    private final int batchSize;
    private final DocumentPersister documentPersister;
    private final Timer autoFlushTimer;

    private TimerTask flushTask;
    private Queue<IndexAction<?>> actions = new ConcurrentLinkedQueue<>();

    SearchIndexDocumentBatchingAsyncClient(SearchAsyncClient client, Integer flushWindow, Integer batchSize,
        DocumentPersister documentPersister) {
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
     * Gets the list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<?>> getActions() {
        return actions;
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that have been successfully indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that have been successfully indexed.
     */
    public Collection<IndexAction<?>> getSucceededActions() {
        return null;
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that have failed indexing and aren't able to be
     * retried.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that have failed indexing and aren't able to be
     * retried.
     */
    public Collection<IndexAction<?>> getFailedActions() {
        return null;
    }

    /**
     * Gets the batch size.
     *
     * @return The batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }

    public Mono<Void> addUploadActions(Collection<?> documents) {
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.UPLOAD);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
    }

    public Mono<Void> addDeleteActions(Collection<?> documents) {
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.DELETE);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
    }

    public Mono<Void> addMergeActions(Collection<?> documents) {
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.MERGE);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
    }

    public Mono<Void> addMergeOrUploadActions(Collection<?> documents) {
        Collection<IndexAction<?>> uploadActions = createDocumentActions(documents, IndexActionType.MERGE_OR_UPLOAD);
        if (documentPersister != null) {
            documentPersister.addQueuedActions(uploadActions);
        }

        actions.addAll(uploadActions);
        return flushIfNeeded();
    }

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
     * @param raiseError Flag indicating if the batch should raise an error if any documents in the batch fail to index.
     * @return A reactive response that indicates if the flush operation has completed.
     */
    public Mono<Void> flush(boolean raiseError) {
        return Mono.empty();
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

        this.flushTask = createFlushTask();
        this.autoFlushTimer.schedule(flushTask, flushWindowMillis);
    }

    private TimerTask createFlushTask() {
        return new TimerTask() {
            @Override
            public void run() {
                flush(false);
            }
        };
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
}
