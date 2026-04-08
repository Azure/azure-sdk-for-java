// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.OnActionAddedOptions;
import com.azure.search.documents.models.OnActionErrorOptions;
import com.azure.search.documents.models.OnActionSentOptions;
import com.azure.search.documents.models.OnActionSucceededOptions;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * SearchIndexingBufferedSender SearchIndexingBufferedSenders} and {@link SearchIndexingBufferedAsyncSender
 * SearchIndexingBufferedAsyncSenders}. Call {@link #buildSender()} and {@link #buildAsyncSender()} respectively to
 * construct an instance of the desired sender.
 *
 * @param <T> The type of the document that the buffered sender will use.
 * @see SearchIndexingBufferedSender
 * @see SearchIndexingBufferedAsyncSender
 */
@ServiceClientBuilder(serviceClients = { SearchIndexingBufferedSender.class, SearchIndexingBufferedAsyncSender.class })
public final class SearchIndexingBufferedSenderBuilder<T> {

    private static final boolean DEFAULT_AUTO_FLUSH = true;

    private static final int DEFAULT_INITIAL_BATCH_ACTION_COUNT = 512;

    private static final Duration DEFAULT_FLUSH_INTERVAL = Duration.ofSeconds(60);

    private static final int DEFAULT_MAX_RETRIES_PER_ACTION = 3;

    private static final Duration DEFAULT_THROTTLING_DELAY = Duration.ofMillis(800);

    private static final Duration DEFAULT_MAX_THROTTLING_DELAY = Duration.ofMinutes(1);

    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexingBufferedSenderBuilder.class);

    private final SearchClientBuilder clientBuilder;

    private Function<Map<String, Object>, String> documentKeyRetriever;

    private boolean autoFlush = DEFAULT_AUTO_FLUSH;

    private Duration autoFlushInterval = DEFAULT_FLUSH_INTERVAL;

    private int initialBatchActionCount = DEFAULT_INITIAL_BATCH_ACTION_COUNT;

    private int maxRetriesPerAction = DEFAULT_MAX_RETRIES_PER_ACTION;

    private Duration throttlingDelay = DEFAULT_THROTTLING_DELAY;

    private Duration maxThrottlingDelay = DEFAULT_MAX_THROTTLING_DELAY;

    private JsonSerializer jsonSerializer;

    private Consumer<OnActionAddedOptions> onActionAddedConsumer;

    private Consumer<OnActionSucceededOptions> onActionSucceededConsumer;

    private Consumer<OnActionErrorOptions> onActionErrorConsumer;

    private Consumer<OnActionSentOptions> onActionSentConsumer;

    SearchIndexingBufferedSenderBuilder(SearchClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * Creates a {@link SearchIndexingBufferedSender} based on options set in the builder. Every time this is called
     * a new instance of {@link SearchIndexingBufferedSender} is created.
     *
     * @return A SearchIndexingBufferedSender with the options set from the builder.
     * @throws NullPointerException If {@code indexName}, {@code endpoint}, or {@code documentKeyRetriever} are null.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and {@link #retryPolicy(RetryPolicy)}
     * have been set.
     */
    public SearchIndexingBufferedSender<T> buildSender() {
        Objects.requireNonNull(documentKeyRetriever, "'documentKeyRetriever' cannot be null");
        SearchClient client = clientBuilder.buildClient();
        JsonSerializer serializer
            = (jsonSerializer == null) ? JsonSerializerProviders.createInstance(true) : jsonSerializer;
        return new SearchIndexingBufferedSender<>(client, serializer, documentKeyRetriever, autoFlush,
            autoFlushInterval, initialBatchActionCount, maxRetriesPerAction, throttlingDelay, maxThrottlingDelay,
            onActionAddedConsumer, onActionSucceededConsumer, onActionErrorConsumer, onActionSentConsumer);
    }

    /**
     * Creates a {@link SearchIndexingBufferedAsyncSender} based on options set in the builder. Every time this is
     * called a new instance of {@link SearchIndexingBufferedAsyncSender} is created.
     *
     * @return A SearchIndexingBufferedAsyncSender with the options set from the builder.
     * @throws NullPointerException If {@code indexName}, {@code endpoint}, or {@code documentKeyRetriever} are null.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)} and {@link #retryPolicy(RetryPolicy)}
     * have been set.
     */
    public SearchIndexingBufferedAsyncSender<T> buildAsyncSender() {
        Objects.requireNonNull(documentKeyRetriever, "'documentKeyRetriever' cannot be null");
        SearchAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        JsonSerializer serializer
            = (jsonSerializer == null) ? JsonSerializerProviders.createInstance(true) : jsonSerializer;
        return new SearchIndexingBufferedAsyncSender<>(asyncClient, serializer, documentKeyRetriever, autoFlush,
            autoFlushInterval, initialBatchActionCount, maxRetriesPerAction, throttlingDelay, maxThrottlingDelay,
            onActionAddedConsumer, onActionSucceededConsumer, onActionErrorConsumer, onActionSentConsumer);
    }

    /**
     * Sets the flag determining whether a buffered sender will automatically flush its document batch based on the
     * configurations of {@link #autoFlushInterval(Duration)} and {@link #initialBatchActionCount(int)}.
     *
     * @param autoFlush Flag determining whether a buffered sender will automatically flush.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> autoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    /**
     * Sets the duration between a buffered sender sending documents to be indexed.
     * <p>
     * The buffered sender will reset the duration when documents are sent for indexing, either by reaching {@link
     * #initialBatchActionCount(int)} or by a manual trigger.
     * <p>
     * If {@code autoFlushInterval} is negative or zero and {@link #autoFlush(boolean)} is enabled the buffered
     * sender will only flush when {@link #initialBatchActionCount(int)} is met.
     *
     * @param autoFlushInterval Duration between document batches being sent for indexing.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws NullPointerException If {@code autoFlushInterval} is null.
     */
    public SearchIndexingBufferedSenderBuilder<T> autoFlushInterval(Duration autoFlushInterval) {
        Objects.requireNonNull(autoFlushInterval, "'autoFlushInterval' cannot be null.");
        this.autoFlushInterval = autoFlushInterval;
        return this;
    }

    /**
     * Sets the number of documents before a buffered sender will send the batch to be indexed.
     * <p>
     * This will only trigger a batch to be sent automatically if {@link #autoFlushInterval} is configured. Default
     * value is {@code 512}.
     *
     * @param initialBatchActionCount The number of documents in a batch that will trigger it to be indexed.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws IllegalArgumentException If {@code batchSize} is less than one.
     */
    public SearchIndexingBufferedSenderBuilder<T> initialBatchActionCount(int initialBatchActionCount) {
        if (initialBatchActionCount < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }
        this.initialBatchActionCount = initialBatchActionCount;
        return this;
    }

    /**
     * Sets the number of times an action will retry indexing before it is considered failed.
     * <p>
     * Documents are only retried on retryable status codes.
     * <p>
     * Default value is {@code 3}.
     *
     * @param maxRetriesPerAction The number of times a document will retry indexing before it is considered failed.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws IllegalArgumentException If {@code maxRetriesPerAction} is less than one.
     */
    public SearchIndexingBufferedSenderBuilder<T> maxRetriesPerAction(int maxRetriesPerAction) {
        if (maxRetriesPerAction < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'maxRetries' cannot be less than one."));
        }
        this.maxRetriesPerAction = maxRetriesPerAction;
        return this;
    }

    /**
     * Sets the initial duration that requests will be delayed when the service is throttling.
     * <p>
     * Default value is {@code Duration.ofMillis(800)}.
     *
     * @param throttlingDelay The initial duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws IllegalArgumentException If {@code throttlingDelay.isNegative()} or {@code throttlingDelay.isZero()}
     * is true.
     * @throws NullPointerException If {@code throttlingDelay} is null.
     */
    public SearchIndexingBufferedSenderBuilder<T> throttlingDelay(Duration throttlingDelay) {
        Objects.requireNonNull(throttlingDelay, "'throttlingDelay' cannot be null.");
        if (throttlingDelay.isNegative() || throttlingDelay.isZero()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'throttlingDelay' cannot be negative or zero."));
        }
        this.throttlingDelay = throttlingDelay;
        return this;
    }

    /**
     * Sets the maximum duration that requests will be delayed when the service is throttling.
     * <p>
     * If {@code maxThrottlingDelay} is less than {@link #throttlingDelay(Duration)} then {@link
     * #throttlingDelay(Duration)} will be used as the maximum delay.
     * <p>
     * Default value is {@code Duration.ofMinutes(1)}.
     *
     * @param maxThrottlingDelay The maximum duration requests will delay when the service is throttling.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws IllegalArgumentException If {@code maxThrottlingDelay.isNegative()} or {@code
     * maxThrottlingDelay.isZero()} is true.
     * @throws NullPointerException If {@code maxThrottlingDelay} is null.
     */
    public SearchIndexingBufferedSenderBuilder<T> maxThrottlingDelay(Duration maxThrottlingDelay) {
        Objects.requireNonNull(maxThrottlingDelay, "'maxThrottlingDelay' cannot be null.");
        if (maxThrottlingDelay.isNegative() || maxThrottlingDelay.isZero()) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maxThrottlingDelay' cannot be negative or zero."));
        }
        this.maxThrottlingDelay = maxThrottlingDelay;
        return this;
    }

    /**
     * Callback hook for when a document indexing action has been added to a batch queued.
     *
     * @param onActionAddedConsumer The {@link Consumer} that is called when a document has been added to a batch
     * queue.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> onActionAdded(Consumer<OnActionAddedOptions> onActionAddedConsumer) {
        this.onActionAddedConsumer = onActionAddedConsumer;
        return this;
    }

    /**
     * Sets the callback hook for when a document indexing action has successfully completed indexing.
     *
     * @param onActionSucceededConsumer The {@link Consumer} that is called when a document has been successfully
     * indexing.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T>
        onActionSucceeded(Consumer<OnActionSucceededOptions> onActionSucceededConsumer) {
        this.onActionSucceededConsumer = onActionSucceededConsumer;
        return this;
    }

    /**
     * Sets the callback hook for when a document indexing action has failed to index and isn't retryable.
     *
     * @param onActionErrorConsumer The {@link Consumer} that is called when a document has failed to index and
     * isn't retryable.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> onActionError(Consumer<OnActionErrorOptions> onActionErrorConsumer) {
        this.onActionErrorConsumer = onActionErrorConsumer;
        return this;
    }

    /**
     * Sets the callback hook for when a document indexing has been sent in a batching request.
     *
     * @param onActionSentConsumer The {@link Consumer} that is called when a document has been sent in a batch
     * request.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> onActionSent(Consumer<OnActionSentOptions> onActionSentConsumer) {
        this.onActionSentConsumer = onActionSentConsumer;
        return this;
    }

    /**
     * Sets the function that retrieves the key value from a document.
     *
     * @param documentKeyRetriever Function that retrieves the key from an {@link IndexAction}.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     * @throws NullPointerException If {@code documentKeyRetriever} is null.
     */
    public SearchIndexingBufferedSenderBuilder<T>
        documentKeyRetriever(Function<Map<String, Object>, String> documentKeyRetriever) {
        this.documentKeyRetriever
            = Objects.requireNonNull(documentKeyRetriever, "'documentKeyRetriever' cannot be null");
        return this;
    }

    /**
     * Custom JSON serializer that is used to handle model types that are not contained in the Azure Search
     * Documents library.
     *
     * @param jsonSerializer The serializer to serialize user defined models.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> serializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        return this;
    }

    // Retaining this commented out code as it may be added back in a future release.
    // /**
    // * Sets the function that handles scaling down the batch size when a 413 (Payload too large) response is
    // returned
    // * by the service.
    // * <p>
    // * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
    // *
    // * @param scaleDownFunction The batch size scale down function.
    // * @return The updated SearchIndexingBufferedSenderOptions object.
    // * @throws NullPointerException If {@code scaleDownFunction} is null.
    // */
    // public SearchIndexingBufferedSenderOptions<T> setPayloadTooLargeScaleDown(
    // Function<Integer, Integer> scaleDownFunction) {
    // this.scaleDownFunction = Objects.requireNonNull(scaleDownFunction, "'scaleDownFunction' cannot be null.");
    // return this;
    // }
    // Retaining this commented out code as it may be added back in a future release.
    // /**
    // * Gets the function that handles scaling down the batch size when a 413 (Payload too large) response is
    // returned
    // * by the service.
    // * <p>
    // * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
    // *
    // * @return The batch size scale down function.
    // */
    // public Function<Integer, Integer> getPayloadTooLargeScaleDown() {
    // return scaleDownFunction;
    // }

    /**
     * Sets the retry options for the builder.
     *
     * @param retryOptions The retry options.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> retryOptions(RetryOptions retryOptions) {
        clientBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Sets the retry policy for the builder.
     *
     * @param retryPolicy The retry policy.
     * @return The updated SearchIndexingBufferedSenderBuilder object.
     */
    public SearchIndexingBufferedSenderBuilder<T> retryPolicy(RetryPolicy retryPolicy) {
        clientBuilder.retryPolicy(retryPolicy);
        return this;
    }
}

