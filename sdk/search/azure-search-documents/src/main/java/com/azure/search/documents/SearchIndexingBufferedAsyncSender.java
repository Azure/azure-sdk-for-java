// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a buffered sender that contains operations for conveniently indexing documents to an Azure Search
 * index.
 */
public final class SearchIndexingBufferedAsyncSender<T> {
    private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedAsyncSender.class);

    private final SearchIndexingPublisher<T> publisher;
    private final boolean autoFlush;
    private final long flushWindowMillis;

    private Timer autoFlushTimer;
    private final AtomicReference<TimerTask> flushTask = new AtomicReference<>();

    private volatile boolean isClosed = false;

    SearchIndexingBufferedAsyncSender(SearchAsyncClient client, SearchIndexingBufferedSenderOptions<T> options) {
        this.publisher = new SearchIndexingPublisher<>(client, options);

        this.autoFlush = options.getAutoFlush();
        this.flushWindowMillis = Math.max(0, options.getAutoFlushWindow().toMillis());
        this.autoFlushTimer = (this.autoFlush && this.flushWindowMillis > 0) ? new Timer() : null;
    }

    /**
     * Gets the {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<?>> getActions() {
        return publisher.getActions();
    }

    /**
     * Gets the batch size.
     *
     * @return The batch size.
     */
    int getBatchSize() {
        return publisher.getBatchSize();
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
    public Mono<Void> addUploadActions(Collection<T> documents) {
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
    public Mono<Void> addDeleteActions(Collection<T> documents) {
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
    public Mono<Void> addMergeActions(Collection<T> documents) {
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
    public Mono<Void> addMergeOrUploadActions(Collection<T> documents) {
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
    public Mono<Void> addActions(Collection<IndexAction<T>> actions) {
        return withContext(context -> addActions(actions, context));
    }

    Mono<Void> createAndAddActions(Collection<T> documents, IndexActionType actionType, Context context) {
        return addActions(createDocumentActions(documents, actionType), context);
    }

    Mono<Void> addActions(Collection<IndexAction<T>> actions, Context context) {
        ensureOpen();

        return publisher.addActions(actions, context, this::rescheduleFlushTask);
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
        ensureOpen();

        rescheduleFlushTask();
        return publisher.flush(context, false);
    }

    private void rescheduleFlushTask() {
        if (!autoFlush) {
            return;
        }

        TimerTask newTask = new TimerTask() {
            @Override
            public void run() {
                Mono.defer(() -> publisher.flush(Context.NONE, false)).subscribe();
            }
        };

        // If the previous flush task exists cancel it. If it has already executed cancel does nothing.
        TimerTask previousTask = this.flushTask.getAndSet(newTask);
        if (previousTask != null) {
            previousTask.cancel();
        }

        this.autoFlushTimer.schedule(newTask, flushWindowMillis);
    }

    /**
     * Closes the buffered sender, any documents remaining in the batch will be sent to the Search index for indexing.
     * <p>
     * Once the buffered sender has been closed any attempts to add documents or flush it will cause an {@link
     * IllegalStateException} to be thrown.
     *
     * @return A reactive response indicating that the buffered sender has been closed.
     */
    public Mono<Void> close() {
        return withContext(this::close);
    }

    Mono<Void> close(Context context) {
        if (!isClosed) {
            synchronized (this) {
                if (!isClosed) {
                    isClosed = true;
                    if (this.autoFlush) {
                        TimerTask currentTask = flushTask.getAndSet(null);
                        if (currentTask != null) {
                            currentTask.cancel();
                        }

                        autoFlushTimer.purge();
                        autoFlushTimer.cancel();
                        autoFlushTimer = null;
                    }

                    return publisher.flush(context, true);
                }

                return Mono.empty();
            }
        }

        return Mono.empty();
    }

    private synchronized void ensureOpen() {
        if (isClosed) {
            throw logger.logExceptionAsError(new IllegalStateException("Buffered sender has been closed."));
        }
    }

    private static <T> Collection<IndexAction<T>> createDocumentActions(Collection<T> documents,
        IndexActionType actionType) {
        return documents.stream().map(document -> new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(document))
            .collect(Collectors.toList());
    }
}
