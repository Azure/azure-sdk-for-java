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
    private static final int DEFAULT_INITIAL_BATCH_ACTION_COUNT = 512;
    private static final Duration DEFAULT_FLUSH_WINDOW = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_RETRY_DELAY = Duration.ofMinutes(1);

    private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedSenderOptions.class);

    private Boolean autoFlush;
    private Duration autoFlushWindow;
    private Integer initialBatchActionCount;
    private Integer maxRetries;
    private Duration retryDelay;
    private Duration maxRetryDelay;

    private Consumer<IndexAction<T>> onActionAddedConsumer;
    private Consumer<IndexAction<T>> onActionSucceededConsumer;
    private BiConsumer<IndexAction<T>, Throwable> onActionErrorBiConsumer;
    private Consumer<IndexAction<T>> onActionSentConsumer;

    private Function<T, String> documentKeyRetriever;

    /**
     * Sets the flag determining whether a buffered sender will automatically flush its document batch based on the
     * configurations of {@link #setAutoFlushWindow(Duration)} and {@link #setInitialBatchActionCount(Integer)}.
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
     * Sets the duration between a buffered sender sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #setInitialBatchActionCount(Integer)} or by a manual trigger.
     * <p>
     * If {@code flushWindow} is negative or zero and {@link #setAutoFlush(Boolean)} is enabled the buffered sender will
     * only flush when {@link #setInitialBatchActionCount(Integer)} is met. If {@code flushWindow} is null a default
     * value of 60 seconds is used.
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
     * #setInitialBatchActionCount(Integer)} or by a manual trigger.
     * <p>
     * If the duration is less than or equal to zero the buffered sender will only flush when {@link
     * #getInitialBatchActionCount()} is triggered.
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
     * Sets the number of documents before a buffered sender will send the batch to be indexed.
     * <p>
     * This will only trigger a batch to be sent automatically if {@link #autoFlushWindow} is configured. Default value
     * is {@code 512}.
     *
     * @param initialBatchActionCount The number of documents in a batch that will trigger it to be indexed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code batchSize} is less than one.
     */
    public SearchIndexingBufferedSenderOptions<T> setInitialBatchActionCount(Integer initialBatchActionCount) {
        if (initialBatchActionCount != null && initialBatchActionCount < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }
        this.initialBatchActionCount = initialBatchActionCount;
        return this;
    }

    /**
     * Gets the number of documents required in a batch for it to be flushed.
     * <p>
     * This configuration is only taken into account if {@link #getAutoFlush()} is true or null.
     *
     * @return The number of documents required before a flush is triggered.
     */
    public int getInitialBatchActionCount() {
        return (initialBatchActionCount == null) ? DEFAULT_INITIAL_BATCH_ACTION_COUNT : initialBatchActionCount;
    }

    /**
     * Sets the number of times a document will retry indexing before it is considered failed.
     * <p>
     * Documents are only retried on retryable status codes.
     * <p>
     * Default value is {@code 3}.
     *
     * @param maxRetries The number of times a document will attempt indexing before it is considered failed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code documentTryLimit} is less than one.
     */
    public SearchIndexingBufferedSenderOptions<T> setMaxRetries(Integer maxRetries) {
        if (maxRetries != null && maxRetries < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maxRetries' cannot be less than one."));
        }

        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Gets the number of times a document will retry indexing before it is considered failed.
     *
     * @return The number of times a document will attempt indexing.
     */
    public int getMaxRetries() {
        return (maxRetries == null) ? DEFAULT_MAX_RETRIES : maxRetries;
    }

    /**
     * Sets the initial duration that requests will be delayed when the service is throttling.
     * <p>
     * Default value is {@code Duration.ofMillis(800)}.
     *
     * @param retryDelay The initial duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code retryDelay.isNegative()} or {@code retryDelay.isZero()} is true.
     */
    public SearchIndexingBufferedSenderOptions<T> setRetryDelay(Duration retryDelay) {
        if (retryDelay != null && (retryDelay.isNegative() || retryDelay.isZero())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'retryDelay' cannot be negative or zero."));
        }

        this.retryDelay = retryDelay;
        return this;
    }

    /**
     * Gets the initial duration that requests will be delay when the service is throttling.
     *
     * @return The initial duration requests will delay when the service is throttling.
     */
    public Duration getRetryDelay() {
        return (retryDelay == null) ? DEFAULT_RETRY_DELAY : retryDelay;
    }

    /**
     * Sets the maximum duration that requests will be delayed when the service is throttling.
     * <p>
     * If {@code maxRetryDelay} is less than {@link #getRetryDelay()} then {@link #getRetryDelay()} will be used as the
     * maximum delay.
     * <p>
     * Default value is {@code Duration.ofMinutes(1)}.
     *
     * @param maxRetryDelay The maximum duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code maxRetryDelay.isNegative()} or {@code maxRetryDelay.isZero()} is true.
     */
    public SearchIndexingBufferedSenderOptions<T> setMaxRetryDelay(Duration maxRetryDelay) {
        if (maxRetryDelay != null && (maxRetryDelay.isNegative() || maxRetryDelay.isZero())) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maxRetryDelay' cannot be negative or zero."));
        }

        this.maxRetryDelay = maxRetryDelay;
        return this;
    }

    /**
     * Gets the maximum duration that requests will delay when the service is throttling.
     *
     * @return The maximum duration requests will delay when the service is throttling.
     */
    public Duration getMaxRetryDelay() {
        return (maxRetryDelay == null) ? DEFAULT_MAX_RETRY_DELAY : maxRetryDelay;
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
     * Sets the callback hook for when a document indexing action has successfully completed indexing.
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
     * Sets the callback hook for when a document indexing action has failed to index and isn't retryable.
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
     * Sets the callback hook for when a document indexing has been sent in a batching request.
     *
     * @param onActionSentConsumer The {@link Consumer} that is called when a document has been sent in a batch
     * request.
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
     * Sets the function that retrieves the key value from a document.
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
