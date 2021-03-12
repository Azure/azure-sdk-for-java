// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.batching.SearchIndexingPublisher;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a buffered sender that contains operations for conveniently indexing documents to an Azure Search
 * index.
 */
@ServiceClient(builder = SearchClientBuilder.class, isAsync = true)
public final class SearchIndexingBufferedAsyncSender<T> {
    private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedAsyncSender.class);

    private final boolean autoFlush;
    private final long flushWindowMillis;

    final SearchIndexingPublisher<T> publisher;

    private Timer autoFlushTimer;
    private final AtomicReference<TimerTask> flushTask = new AtomicReference<>();

    private volatile boolean isClosed = false;

    SearchIndexingBufferedAsyncSender(SearchIndexClientImpl restClient, JsonSerializer serializer,
        Function<T, String> documentKeyRetriever, boolean autoFlush, Duration autoFlushInterval,
        int initialBatchActionCount, int maxRetriesPerAction, Duration throttlingDelay, Duration maxThrottlingDelay,
        Consumer<OnActionAddedOptions<T>> onActionAddedConsumer,
        Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer,
        Consumer<OnActionErrorOptions<T>> onActionErrorConsumer,
        Consumer<OnActionSentOptions<T>> onActionSentConsumer) {
        this.publisher = new SearchIndexingPublisher<>(restClient, serializer, documentKeyRetriever, autoFlush,
            initialBatchActionCount, maxRetriesPerAction, throttlingDelay, maxThrottlingDelay, onActionAddedConsumer,
            onActionSucceededConsumer, onActionErrorConsumer, onActionSentConsumer);

        this.autoFlush = autoFlush;
        this.flushWindowMillis = Math.max(0, autoFlushInterval.toMillis());
        this.autoFlushTimer = (this.autoFlush && this.flushWindowMillis > 0) ? new Timer() : null;

    }

    /**
     * Gets the {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<T>> getActions() {
        return publisher.getActions();
    }

    /**
     * Gets the number of documents required in a batch for it to be flushed.
     * <p>
     * This configuration is only taken into account if auto flushing is enabled.
     *
     * @return The number of documents required before a flush is triggered.
     */
    int getBatchActionCount() {
        return publisher.getBatchActionCount();
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
        return publisher.flush(false, false, context);
    }

    private void rescheduleFlushTask() {
        if (!autoFlush) {
            return;
        }

        TimerTask newTask = new TimerTask() {
            @Override
            public void run() {
                Mono.defer(() -> publisher.flush(false, false, Context.NONE)).subscribe();
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

                    return publisher.flush(true, true, context);
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
