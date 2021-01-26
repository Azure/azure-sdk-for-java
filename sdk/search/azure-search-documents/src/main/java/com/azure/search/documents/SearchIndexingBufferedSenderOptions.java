// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;

import java.time.Duration;
import java.util.Objects;
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
    private static final Function<Integer, Integer> DEFAULT_SCALE_DOWN_FUNCTION = oldBatchCount -> {
        if (oldBatchCount == 1) {
            return 1;
        } else {
            return Math.max(1, oldBatchCount / 2);
        }
    };
    private static final Duration DEFAULT_FLUSH_INTERVAL = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_RETRIES_PER_ACTION = 3;
    private static final Duration DEFAULT_THROTTLING_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_THROTTLING_DELAY = Duration.ofMinutes(1);

    private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedSenderOptions.class);

    private final Function<T, String> documentKeyRetriever;

    private boolean autoFlush = DEFAULT_AUTO_FLUSH;
    private Duration autoFlushInterval = DEFAULT_FLUSH_INTERVAL;
    private int initialBatchActionCount = DEFAULT_INITIAL_BATCH_ACTION_COUNT;
    private Function<Integer, Integer> scaleDownFunction = DEFAULT_SCALE_DOWN_FUNCTION;
    private int maxRetriesPerAction = DEFAULT_MAX_RETRIES_PER_ACTION;
    private Duration throttlingDelay = DEFAULT_THROTTLING_DELAY;
    private Duration maxThrottlingDelay = DEFAULT_MAX_THROTTLING_DELAY;

    private Consumer<OnActionAddedOptions<T>> onActionAddedConsumer;
    private Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer;
    private Consumer<OnActionErrorOptions<T>> onActionErrorConsumer;
    private Consumer<OnActionSentOptions<T>> onActionSentConsumer;

    /**
     * Creates a new SearchIndexingBufferedSenderOptions with the specified {@code documentKeyRetriever}.
     *
     * @param documentKeyRetriever Function that retrieves the key from an {@link IndexAction}.
     * @throws NullPointerException If {@code documentKeyRetriever} is null.
     */
    public SearchIndexingBufferedSenderOptions(Function<T, String> documentKeyRetriever) {
        this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
            "'documentKeyRetriever' cannot be null");
    }

    /**
     * Sets the flag determining whether a buffered sender will automatically flush its document batch based on the
     * configurations of {@link #setAutoFlushInterval(Duration)} and {@link #setInitialBatchActionCount(int)}.
     *
     * @param autoFlush Flag determining whether a buffered sender will automatically flush.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    /**
     * Gets the flag that indicates whether the buffered sender will be configured to automatically flush.
     *
     * @return Flag indicating if the buffered sender will automatically flush.
     */
    public boolean getAutoFlush() {
        return autoFlush;
    }

    /**
     * Sets the duration between a buffered sender sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #setInitialBatchActionCount(int)} or by a manual trigger.
     * <p>
     * If {@code autoFlushInterval} is negative or zero and {@link #setAutoFlush(boolean)} is enabled the buffered
     * sender will only flush when {@link #setInitialBatchActionCount(int)} is met.
     *
     * @param autoFlushInterval Duration between document batches being sent for indexing.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws NullPointerException If {@code autoFlushInterval} is null.
     */
    public SearchIndexingBufferedSenderOptions<T> setAutoFlushInterval(Duration autoFlushInterval) {
        Objects.requireNonNull(autoFlushInterval, "'autoFlushInterval' cannot be null.");

        this.autoFlushInterval = autoFlushInterval;
        return this;
    }

    /**
     * Gets the {@link Duration} that the buffered sender will wait between sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #setInitialBatchActionCount(int)} or by a manual trigger.
     * <p>
     * If the duration is less than or equal to zero the buffered sender will only flush when {@link
     * #getInitialBatchActionCount()} is triggered.
     * <p>
     * This configuration is only taken into account if {@link #getAutoFlush()} is true.
     *
     * @return The {@link Duration} to wait after the last document has been added to the batch before the batch is
     * flushed.
     */
    public Duration getAutoFlushInterval() {
        return autoFlushInterval;
    }

    /**
     * Sets the number of documents before a buffered sender will send the batch to be indexed.
     * <p>
     * This will only trigger a batch to be sent automatically if {@link #autoFlushInterval} is configured. Default
     * value is {@code 512}.
     *
     * @param initialBatchActionCount The number of documents in a batch that will trigger it to be indexed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code batchSize} is less than one.
     */
    public SearchIndexingBufferedSenderOptions<T> setInitialBatchActionCount(int initialBatchActionCount) {
        if (initialBatchActionCount < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }

        this.initialBatchActionCount = initialBatchActionCount;
        return this;
    }

    /**
     * Gets the number of documents required in a batch for it to be flushed.
     * <p>
     * This configuration is only taken into account if {@link #getAutoFlush()} is true.
     *
     * @return The number of documents required before a flush is triggered.
     */
    public int getInitialBatchActionCount() {
        return initialBatchActionCount;
    }

    /**
     * Sets the function that handles scaling down the batch size when a 413 (Payload too large) response is returned
     * by the service.
     * <p>
     * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
     *
     * @param scaleDownFunction The batch size scale down function.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws NullPointerException If {@code scaleDownFunction} is null.
     */
    public SearchIndexingBufferedSenderOptions<T> setPayloadTooLargeScaleDown(
        Function<Integer, Integer> scaleDownFunction) {
        this.scaleDownFunction = Objects.requireNonNull(scaleDownFunction, "'scaleDownFunction' cannot be null.");
        return this;
    }

    /**
     * Gets the function that handles scaling down the batch size when a 413 (Payload too large) response is returned
     * by the service.
     * <p>
     * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
     *
     * @return The batch size scale down function.
     */
    public Function<Integer, Integer> getPayloadTooLargeScaleDown() {
        return scaleDownFunction;
    }

    /**
     * Sets the number of times an action will retry indexing before it is considered failed.
     * <p>
     * Documents are only retried on retryable status codes.
     * <p>
     * Default value is {@code 3}.
     *
     * @param maxRetriesPerAction The number of times a document will retry indexing before it is considered failed.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code maxRetriesPerAction} is less than one.
     */
    public SearchIndexingBufferedSenderOptions<T> setMaxRetriesPerAction(int maxRetriesPerAction) {
        if (maxRetriesPerAction < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maxRetries' cannot be less than one."));
        }

        this.maxRetriesPerAction = maxRetriesPerAction;
        return this;
    }

    /**
     * Gets the number of times a document will retry indexing before it is considered failed.
     *
     * @return The number of times a document will attempt indexing.
     */
    public int getMaxRetriesPerAction() {
        return maxRetriesPerAction;
    }

    /**
     * Sets the initial duration that requests will be delayed when the service is throttling.
     * <p>
     * Default value is {@code Duration.ofMillis(800)}.
     *
     * @param throttlingDelay The initial duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code throttlingDelay.isNegative()} or {@code throttlingDelay.isZero()} is
     * true.
     * @throws NullPointerException If {@code throttlingDelay} is null.
     */
    public SearchIndexingBufferedSenderOptions<T> setThrottlingDelay(Duration throttlingDelay) {
        Objects.requireNonNull(throttlingDelay, "'throttlingDelay' cannot be null.");

        if (throttlingDelay.isNegative() || throttlingDelay.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'throttlingDelay' cannot be negative or zero."));
        }

        this.throttlingDelay = throttlingDelay;
        return this;
    }

    /**
     * Gets the initial duration that requests will be delayed when the service is throttling.
     *
     * @return The initial duration requests will delay when the service is throttling.
     */
    public Duration getThrottlingDelay() {
        return throttlingDelay;
    }

    /**
     * Sets the maximum duration that requests will be delayed when the service is throttling.
     * <p>
     * If {@code maxThrottlingDelay} is less than {@link #getThrottlingDelay()} then {@link #getThrottlingDelay()} will
     * be used as the maximum delay.
     * <p>
     * Default value is {@code Duration.ofMinutes(1)}.
     *
     * @param maxThrottlingDelay The maximum duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     * @throws IllegalArgumentException If {@code maxThrottlingDelay.isNegative()} or {@code
     * maxThrottlingDelay.isZero()} is true.
     * @throws NullPointerException If {@code maxThrottlingDelay} is null.
     */
    public SearchIndexingBufferedSenderOptions<T> setMaxThrottlingDelay(Duration maxThrottlingDelay) {
        Objects.requireNonNull(maxThrottlingDelay, "'maxThrottlingDelay' cannot be null.");

        if (maxThrottlingDelay.isNegative() || maxThrottlingDelay.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maxThrottlingDelay' cannot be negative or zero."));
        }

        this.maxThrottlingDelay = maxThrottlingDelay;
        return this;
    }

    /**
     * Gets the maximum duration that requests will delay when the service is throttling.
     *
     * @return The maximum duration requests will delay when the service is throttling.
     */
    public Duration getMaxThrottlingDelay() {
        return maxThrottlingDelay;
    }

    /**
     * Callback hook for when a document indexing action has been added to a batch queued.
     *
     * @param onActionAddedConsumer The {@link Consumer} that is called when a document has been added to a batch
     * queue.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionAdded(
        Consumer<OnActionAddedOptions<T>> onActionAddedConsumer) {
        this.onActionAddedConsumer = onActionAddedConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is added to a batch.
     *
     * @return The {@link Consumer} called when a document is added to a batch.
     */
    public Consumer<OnActionAddedOptions<T>> getOnActionAdded() {
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
        Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer) {
        this.onActionSucceededConsumer = onActionSucceededConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is successfully indexed.
     *
     * @return The {@link Consumer} called when a document is successfully indexed.
     */
    public Consumer<OnActionSucceededOptions<T>> getOnActionSucceeded() {
        return onActionSucceededConsumer;
    }

    /**
     * Sets the callback hook for when a document indexing action has failed to index and isn't retryable.
     *
     * @param onActionErrorConsumer The {@link Consumer} that is called when a document has failed to index and isn't
     * retryable.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionError(
        Consumer<OnActionErrorOptions<T>> onActionErrorConsumer) {
        this.onActionErrorConsumer = onActionErrorConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document has failed to index.
     *
     * @return The {@link Consumer} called when a document has failed to index.
     */
    public Consumer<OnActionErrorOptions<T>> getOnActionError() {
        return onActionErrorConsumer;
    }

    /**
     * Sets the callback hook for when a document indexing has been sent in a batching request.
     *
     * @param onActionSentConsumer The {@link Consumer} that is called when a document has been sent in a batch
     * request.
     * @return The updated SearchIndexingBufferedSenderOptions object.
     */
    public SearchIndexingBufferedSenderOptions<T> setOnActionSent(
        Consumer<OnActionSentOptions<T>> onActionSentConsumer) {
        this.onActionSentConsumer = onActionSentConsumer;
        return this;
    }

    /**
     * Gets the {@link Consumer} that will be called when a document is sent in a batch.
     *
     * @return The {@link Consumer} called when a document is sent in a batch.
     */
    public Consumer<OnActionSentOptions<T>> getOnActionSent() {
        return onActionSentConsumer;
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
