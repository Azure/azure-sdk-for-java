// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.implementation.batching.SearchIndexingAsyncPublisher;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides a buffered sender that contains operations for conveniently indexing documents to an Azure Search
 * index.
 *
 * @param <T> The type of the document handled by this buffered indexing sender.
 */
@ServiceClient(builder = SearchClientBuilder.class, isAsync = true)
public final class SearchIndexingBufferedAsyncSender<T> {

    private final boolean autoFlush;
    private final long flushWindowMillis;

    final SearchIndexingAsyncPublisher publisher;
    private final JsonSerializer serializer;

    private Timer autoFlushTimer;
    private final AtomicReference<TimerTask> flushTask = new AtomicReference<>();

    private volatile boolean isClosed = false;
    private final ReentrantLock closeLock = new ReentrantLock();

    SearchIndexingBufferedAsyncSender(SearchAsyncClient searchAsyncClient, JsonSerializer serializer,
        Function<Map<String, Object>, String> documentKeyRetriever, boolean autoFlush, Duration autoFlushInterval,
        int initialBatchActionCount, int maxRetriesPerAction, Duration throttlingDelay, Duration maxThrottlingDelay,
        Consumer<OnActionAddedOptions> onActionAdded, Consumer<OnActionSucceededOptions> onActionSucceeded,
        Consumer<OnActionErrorOptions> onActionError, Consumer<OnActionSentOptions> onActionSent) {
        this.publisher = new SearchIndexingAsyncPublisher(searchAsyncClient, documentKeyRetriever, autoFlush,
            initialBatchActionCount, maxRetriesPerAction, throttlingDelay, maxThrottlingDelay, onActionAdded,
            onActionSucceeded, onActionError, onActionSent);

        this.autoFlush = autoFlush;
        this.flushWindowMillis = Math.max(0, autoFlushInterval.toMillis());
        this.autoFlushTimer = (this.autoFlush && this.flushWindowMillis > 0) ? new Timer() : null;
        this.serializer = serializer;

    }

    /**
     * Gets the {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction> getActions() {
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
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addUploadActions(Collection<T> documents) {
        return addUploadActions(documents, null);
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addUploadActions(Collection<T> documents, RequestOptions requestOptions) {
        return createAndAddActions(documents, IndexActionType.UPLOAD, requestOptions);
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
        return addDeleteActions(documents, null);
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addDeleteActions(Collection<T> documents, RequestOptions requestOptions) {
        return createAndAddActions(documents, IndexActionType.DELETE, requestOptions);
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
        return addMergeActions(documents, null);
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addMergeActions(Collection<T> documents, RequestOptions requestOptions) {
        return createAndAddActions(documents, IndexActionType.MERGE, requestOptions);
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
        return addMergeOrUploadActions(documents, null);
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addMergeOrUploadActions(Collection<T> documents, RequestOptions requestOptions) {
        return createAndAddActions(documents, IndexActionType.MERGE_OR_UPLOAD, requestOptions);
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
    public Mono<Void> addActions(Collection<IndexAction> actions) {
        return addActions(actions, null);
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the documents have been added to the batch.
     */
    public Mono<Void> addActions(Collection<IndexAction> actions, RequestOptions requestOptions) {
        return addActions(Mono.just(actions), requestOptions);
    }

    Mono<Void> createAndAddActions(Collection<T> documents, IndexActionType actionType, RequestOptions requestOptions) {
        return addActions(createDocumentActions(documents, actionType), requestOptions);
    }

    Mono<Void> addActions(Mono<Collection<IndexAction>> actionsMono, RequestOptions requestOptions) {
        return ensureOpen().then(actionsMono)
            .flatMap(actions -> publisher.addActions(actions, requestOptions, () -> rescheduleFlushTask(requestOptions)));
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @return A reactive response that indicates if the flush operation has completed.
     */
    public Mono<Void> flush() {
        return flush(null);
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response that indicates if the flush operation has completed.
     */
    public Mono<Void> flush(RequestOptions requestOptions) {
        return ensureOpen().then(rescheduleFlushTask(requestOptions))
            .then(publisher.flush(false, false, requestOptions));
    }

    private Mono<Void> rescheduleFlushTask(RequestOptions requestOptions) {
        return Mono.fromRunnable(() -> {
            if (!autoFlush) {
                return;
            }

            TimerTask newTask = new TimerTask() {
                @Override
                public void run() {
                    Mono.defer(() -> publisher.flush(false, false, requestOptions)).subscribe();
                }
            };

            // If the previous flush task exists cancel it. If it has already executed cancel does nothing.
            TimerTask previousTask = this.flushTask.getAndSet(newTask);
            if (previousTask != null) {
                previousTask.cancel();
            }

            this.autoFlushTimer.schedule(newTask, flushWindowMillis);
        });
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
        return close(null);
    }

    /**
     * Closes the buffered sender, any documents remaining in the batch will be sent to the Search index for indexing.
     * <p>
     * Once the buffered sender has been closed any attempts to add documents or flush it will cause an {@link
     * IllegalStateException} to be thrown.
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return A reactive response indicating that the buffered sender has been closed.
     */
    public Mono<Void> close(RequestOptions requestOptions) {
        if (!isClosed) {
            closeLock.lock();
            try {
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

                    return publisher.flush(true, true, requestOptions);
                }

                return Mono.empty();
            } finally {
                closeLock.unlock();
            }
        }

        return Mono.empty();
    }

    private Mono<Void> ensureOpen() {
        return isClosed ? Mono.error(new IllegalStateException("Buffered sender has been closed.")) : Mono.empty();
    }

    private Mono<Collection<IndexAction>> createDocumentActions(Collection<T> documents,
        IndexActionType actionType) {
        return Mono.fromCallable(() -> {
            Collection<IndexAction> actions = new ArrayList<>(documents.size());

            for (T document : documents) {
                try (JsonReader jsonReader = JsonProviders.createReader(serializer.serializeToBytes(document))) {
                    actions.add(new IndexAction().setActionType(actionType)
                        .setAdditionalProperties(jsonReader.readMap(JsonReader::readUntyped)));
                }
            }

            return actions;
        });
    }
}
