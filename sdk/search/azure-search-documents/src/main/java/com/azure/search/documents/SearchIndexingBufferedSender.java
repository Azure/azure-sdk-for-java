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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides a buffered sender that contains operations for conveniently indexing documents to an Azure Search
 * index.
 *
 * @param <T> The type of the document handled by this buffered indexing sender.
 */
@ServiceClient(builder = SearchClientBuilder.class)
public final class SearchIndexingBufferedSender<T> {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexingBufferedSender.class);

    private final boolean autoFlush;
    private final long flushWindowMillis;

    final SearchIndexingPublisher<T> publisher;

    private Timer autoFlushTimer;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SearchIndexingBufferedSender, TimerTask> FLUSH_TASK
        = AtomicReferenceFieldUpdater.newUpdater(SearchIndexingBufferedSender.class, TimerTask.class, "flushTask");
    private volatile TimerTask flushTask;

    private final AtomicBoolean closed = new AtomicBoolean();
    private final ReentrantLock closeLock = new ReentrantLock();

    SearchIndexingBufferedSender(SearchIndexClientImpl restClient, JsonSerializer serializer,
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
     * Gets the list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
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
        return publisher.getBatchSize();
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     */
    public void addUploadActions(Collection<T> documents) {
        addUploadActions(documents, null, Context.NONE);
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addUploadActions(Collection<T> documents, Duration timeout, Context context) {
        createAndAddActions(documents, IndexActionType.UPLOAD, timeout, context);
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     */
    public void addDeleteActions(Collection<T> documents) {
        addDeleteActions(documents, null, Context.NONE);
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addDeleteActions(Collection<T> documents, Duration timeout, Context context) {
        createAndAddActions(documents, IndexActionType.DELETE, timeout, context);
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     */
    public void addMergeActions(Collection<T> documents) {
        addMergeActions(documents, null, Context.NONE);
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addMergeActions(Collection<T> documents, Duration timeout, Context context) {
        createAndAddActions(documents, IndexActionType.MERGE, timeout, context);
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     */
    public void addMergeOrUploadActions(Collection<T> documents) {
        addMergeOrUploadActions(documents, null, Context.NONE);
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addMergeOrUploadActions(Collection<T> documents, Duration timeout, Context context) {
        createAndAddActions(documents, IndexActionType.MERGE_OR_UPLOAD, timeout, context);
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     */
    public void addActions(Collection<IndexAction<T>> actions) {
        addActions(actions, null, Context.NONE);
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addActions(Collection<IndexAction<T>> actions, Duration timeout, Context context) {
        addActionsInternal(actions, timeout, context);
    }

    void createAndAddActions(Collection<T> documents, IndexActionType actionType, Duration timeout, Context context) {
        addActionsInternal(createDocumentActions(documents, actionType), timeout, context);
    }

    void addActionsInternal(Collection<IndexAction<T>> actions, Duration timeout, Context context) {
        ensureOpen();

        publisher.addActions(actions, timeout, context, this::rescheduleFlushTask);
    }

    /**
     * Sends the current batch of documents to be indexed.
     */
    public void flush() {
        flush(null, Context.NONE);
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void flush(Duration timeout, Context context) {
        flushInternal(timeout, context);
    }

    void flushInternal(Duration timeout, Context context) {
        ensureOpen();

        rescheduleFlushTask();
        publisher.flush(false, false, timeout, context);
    }

    private void rescheduleFlushTask() {
        if (!autoFlush) {
            return;
        }

        TimerTask newTask = new TimerTask() {
            @Override
            public void run() {
                publisher.flush(false, false, null, Context.NONE);
            }
        };

        // If the previous flush task exists cancel it. If it has already executed cancel does nothing.
        TimerTask previousTask = FLUSH_TASK.getAndSet(this, newTask);
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
     */
    public void close() {
        close(null, Context.NONE);
    }

    /**
     * Closes the buffered, any documents remaining in the batch yet to be sent to the Search index for indexing.
     * <p>
     * Once the buffered sender has been closed any attempts to add documents or flush it will cause an {@link
     * IllegalStateException} to be thrown.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void close(Duration timeout, Context context) {
        closeInternal(timeout, context);
    }

    void closeInternal(Duration timeout, Context context) {
        if (!closed.get()) {
            closeLock.lock();
            try {
                if (closed.compareAndSet(false, true)) {
                    if (this.autoFlush) {
                        TimerTask currentTask = FLUSH_TASK.getAndSet(this, null);
                        if (currentTask != null) {
                            currentTask.cancel();
                        }

                        autoFlushTimer.purge();
                        autoFlushTimer.cancel();
                        autoFlushTimer = null;
                    }

                    publisher.flush(true, true, timeout, context);
                }
            } finally {
                closeLock.unlock();
            }
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Buffered sender has been closed."));
        }
    }

    private static <T> Collection<IndexAction<T>> createDocumentActions(Collection<T> documents,
        IndexActionType actionType) {
        Collection<IndexAction<T>> actions = new ArrayList<>(documents.size());

        for (T document : documents) {
            actions.add(new IndexAction<T>().setActionType(actionType).setDocument(document));
        }

        return actions;
    }
}
