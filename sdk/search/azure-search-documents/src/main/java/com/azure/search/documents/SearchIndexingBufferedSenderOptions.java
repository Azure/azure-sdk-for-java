// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexAction;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configuration options used when constructing a {@link SearchIndexingBufferedSender} or {@link
 * SearchIndexingBufferedAsyncSender}.
 *
 * @see SearchIndexingBufferedSender
 * @see SearchIndexingBufferedAsyncSender
 */
public final class SearchIndexingBufferedSenderOptions<T> {
    private static final boolean DEFAULT_AUTO_FLUSH = true;
    private static final int DEFAULT_BATCH_SIZE = 500;
    private static final Duration DEFAULT_FLUSH_WINDOW = Duration.ofSeconds(60);
    private static final int DEFAULT_DOCUMENT_TRY_LIMIT = 3;

    private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedSenderOptions.class);

    private Boolean autoFlush;
    private Duration autoFlushWindow;
    private Integer batchSize;
    private Integer documentTryLimit;

    private Consumer<IndexAction<T>> onActionAddedConsumer;
    private Consumer<IndexAction<T>> onActionSucceededConsumer;
    private BiConsumer<IndexAction<T>, Throwable> onActionErrorBiConsumer;
    private Consumer<IndexAction<T>> onActionSentConsumer;

    private Function<T, String> documentKeyRetriever;

    /**
     * Flag determining whether a buffered sender will automatically flush its document batch based on the
     * configurations of {@link #setAutoFlushWindow(Duration)} and {@link #setBatchSize(Integer)}.
     * <p>
     * If {@code autoFlush} is null the buffered sender will be set to automatically flush.
     *
     * @param autoFlush Flag determining whether a buffered sender will automatically flush.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setAutoFlush(Boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    /**
     * Gets the flag that indicates whether the buffered sender will be configured to automatically flush.
     *
     * @return Flag indicating if the buffered sender will automatically flush.
     */
    public boolean getAutoFlush() {
        return (autoFlush == null) ? DEFAULT_AUTO_FLUSH : autoFlush;
    }

    /**
     * Duration between a buffered sender sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #setBatchSize(Integer)} or by a manual trigger.
     * <p>
     * If {@code flushWindow} is negative or zero and {@link #setAutoFlush(Boolean)} is enabled the buffered sender will
     * only flush when {@link #setBatchSize(Integer)} is met. If {@code flushWindow} is null a default value of 60
     * seconds is used.
     *
     * @param autoFlushWindow Duration between document batches being sent for indexing.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setAutoFlushWindow(Duration autoFlushWindow) {
        this.autoFlushWindow = autoFlushWindow;
        return this;
    }

    /**
     * Gets the {@link Duration} that the buffered sender will wait between sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #setBatchSize(Integer)} or by a manual trigger.
     * <p>
     * If the duration is less than or equal to zero the buffered sender will only flush when {@link #getBatchSize()} is
     * triggered.
     * <p>
     * This configuration is only taken into account if {@link #getAutoFlush()} is true or null.
     *
     * @return The {@link Duration} to wait after the last document has been added to the batch before the batch is
     * flushed.
     */
    public Duration getAutoFlushWindow() {
        return (autoFlushWindow == null) ? DEFAULT_FLUSH_WINDOW : autoFlushWindow;
    }

    /**
     * The number of documents before a buffered sender will send the batch to be indexed.
     * <p>
     * This will only trigger a batch to be sent automatically if {@link #autoFlushWindow} is configured. Default value
     * is {@code 100}.
     *
     * @param batchSize The number of documents in a batch that will trigger it to be indexed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code batchSize} is less than one.
     */
    SearchIndexingBufferedSenderOptions<T> setBatchSize(Integer batchSize) {
        if (batchSize != null && batchSize < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Gets the number of documents required in a batch for it to be flushed.
     * <p>
     * This configuration is only taken into account if {@link #getAutoFlush()} is true or null.
     *
     * @return The number of documents required before a flush is triggered.
     */
    int getBatchSize() {
        return (batchSize == null) ? DEFAULT_BATCH_SIZE : batchSize;
    }

    /**
     * The number of times a document will attempt indexing before it is considered failed.
     * <p>
     * Documents are only retried on retryable status codes.
     * <p>
     * Default value is {@code 3}.
     *
     * @param documentTryLimit The number of times a document will attempt indexing before it is considered failed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code documentTryLimit} is less than one.
     */
    SearchIndexingBufferedSenderOptions<T> setDocumentTryLimit(Integer documentTryLimit) {
        if (documentTryLimit != null && documentTryLimit < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'documentTryLimit' cannot be less than one."));
        }

        this.documentTryLimit = documentTryLimit;
        return this;
    }

    /**
     * Gets the number of times a document will attempt indexing before it is considered failed.
     *
     * @return The number of times a document will attempt indexing.
     */
    int getDocumentTryLimit() {
        return (documentTryLimit == null) ? DEFAULT_DOCUMENT_TRY_LIMIT : documentTryLimit;
    }

    /**
     * Callback hook for when a document indexing action has been added to a batch queued.
     *
     * @param onActionAddedConsumer The {@link Consumer} that is called when a document has been added to a batch
     * queue.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionAdded(Consumer<IndexAction<T>> onActionAddedConsumer) {
        this.onActionAddedConsumer = onActionAddedConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is added to a batch.
     *
     * @return The {@link Consumer} called when a document is added to a batch.
     */
    public Consumer<IndexAction<T>> getOnActionAdded() {
        return onActionAddedConsumer;
    }

    /**
     * Callback hook for when a document indexing action has successfully completed indexing.
     *
     * @param onActionSucceededConsumer The {@link Consumer} that is called when a document has been successfully
     * indexing.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionSucceeded(
        Consumer<IndexAction<T>> onActionSucceededConsumer) {
        this.onActionSucceededConsumer = onActionSucceededConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is successfully indexed.
     *
     * @return The {@link Consumer} called when a document is successfully indexed.
     */
    public Consumer<IndexAction<T>> getOnActionSucceeded() {
        return onActionSucceededConsumer;
    }

    /**
     * Callback hook for when a document indexing action has failed to index and isn't retryable.
     *
     * @param onActionErrorBiConsumer The {@link BiConsumer} that is called when a document has failed to index and
     * isn't retryable.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionError(
        BiConsumer<IndexAction<T>, Throwable> onActionErrorBiConsumer) {
        this.onActionErrorBiConsumer = onActionErrorBiConsumer;
        return this;
    }

    /**
     * Gets the {@link BiConsumer} that will be called when a document has failed to index.
     *
     * @return The {@link BiConsumer} called when a document has failed to index.
     */
    public BiConsumer<IndexAction<T>, Throwable> getOnActionError() {
        return onActionErrorBiConsumer;
    }

    /**
     * Callback hook for when a document indexing has been sent in a batching request.
     *
     * @param onActionSentConsumer The {@link Consumer} that is called when a document has been sent in a batch request.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionSent(Consumer<IndexAction<T>> onActionSentConsumer) {
        this.onActionSentConsumer = onActionSentConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is sent in a batch.
     *
     * @return The {@link Consumer} called when a document is sent in a batch.
     */
    public Consumer<IndexAction<T>> getOnActionSent() {
        return onActionSentConsumer;
    }

    /**
     * Function that retrieves the key value from a document.
     * <p>
     * This function must be sent for a buffered sender to be properly constructed. It is used to correlate response
     * values to the originating document.
     *
     * @param documentKeyRetriever Function that retrieves the key from an {@link IndexAction}.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws NullPointerException If {@code documentKeyRetriever} is null.
     */
    public SearchIndexingBufferedSenderOptions<T> setDocumentKeyRetriever(
        Function<T, String> documentKeyRetriever) {
        this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
            "'documentKeyRetriever' cannot be null");
        return this;
    }

    /**
     * Gets the function that retrieves the key value from a document.
     *
     * @return The function that retrieves the key value from a document.
     */
    public Function<T, String> getDocumentKeyRetriever() {
        return documentKeyRetriever;
    }
}
