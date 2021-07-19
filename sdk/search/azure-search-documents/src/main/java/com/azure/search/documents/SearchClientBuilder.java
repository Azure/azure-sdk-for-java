// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.search.documents.implementation.util.Constants;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.options.OnActionAddedOptions;
import com.azure.search.documents.options.OnActionErrorOptions;
import com.azure.search.documents.options.OnActionSentOptions;
import com.azure.search.documents.options.OnActionSucceededOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.search.documents.implementation.util.Utility.buildRestClient;
import static com.azure.search.documents.implementation.util.Utility.getDefaultSerializerAdapter;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link SearchClient
 * SearchClients} and {@link SearchAsyncClient SearchAsyncClients}. Call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 * <p>
 * The following must be provided to construct a client instance.
 * <ul>
 * <li>The Azure Cognitive Search service URL.</li>
 * <li>An {@link AzureKeyCredential} that grants access to the Azure Cognitive Search service.</li>
 * <li>The search index name.</li>
 * </ul>
 *
 * <p><strong>Instantiating an asynchronous Search Client</strong></p>
 *
 * {@codesnippet com.azure.search.documents.SearchAsyncClient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Search Client</strong></p>
 *
 * {@codesnippet com.azure.search.documents.SearchClient.instantiation}
 *
 * @see SearchClient
 * @see SearchAsyncClient
 */
@ServiceClientBuilder(serviceClients = {SearchClient.class, SearchAsyncClient.class})
public final class SearchClientBuilder {
    private static final boolean DEFAULT_AUTO_FLUSH = true;
    private static final int DEFAULT_INITIAL_BATCH_ACTION_COUNT = 512;
    private static final Duration DEFAULT_FLUSH_INTERVAL = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_RETRIES_PER_ACTION = 3;
    private static final Duration DEFAULT_THROTTLING_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_THROTTLING_DELAY = Duration.ofMinutes(1);
    // Retaining this commented out code as it may be added back in a future release.
//    private static final Function<Integer, Integer> DEFAULT_SCALE_DOWN_FUNCTION = oldBatchCount -> {
//        if (oldBatchCount == 1) {
//            return 1;
//        } else {
//            return Math.max(1, oldBatchCount / 2);
//        }
//    };

    private final ClientLogger logger = new ClientLogger(SearchClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;

    private SearchServiceVersion serviceVersion;
    private String endpoint;
    private HttpClient httpClient;
    private HttpPipeline httpPipeline;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;
    private Configuration configuration;
    private String indexName;
    private RetryPolicy retryPolicy;
    private JsonSerializer jsonSerializer;

    /**
     * Creates a builder instance that is able to configure and construct {@link SearchClient SearchClients} and {@link
     * SearchAsyncClient SearchAsyncClients}.
     */
    public SearchClientBuilder() {
    }

    /**
     * Creates a {@link SearchClient} based on options set in the builder. Every time {@code buildClient()} is called a
     * new instance of {@link SearchClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchClient client}. All other
     * builder settings are ignored.
     *
     * @return A SearchClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are null.
     */
    public SearchClient buildClient() {
        return new SearchClient(buildAsyncClient());
    }

    /**
     * Creates a {@link SearchAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
     * called a new instance of {@link SearchAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchAsyncClient client}. All
     * other builder settings are ignored.
     *
     * @return A SearchClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are null.
     */
    public SearchAsyncClient buildAsyncClient() {
        validateIndexNameAndEndpoint();
        SearchServiceVersion buildVersion = (serviceVersion == null)
            ? SearchServiceVersion.getLatest()
            : serviceVersion;

        HttpPipeline pipeline = getHttpPipeline();
        return new SearchAsyncClient(endpoint, indexName, buildVersion, pipeline, jsonSerializer,
            Utility.buildRestClient(endpoint, indexName, pipeline, getDefaultSerializerAdapter()));
    }

    /**
     * Create a new instance of {@link SearchIndexingBufferedSenderBuilder} used to configure {@link
     * SearchIndexingBufferedSender SearchIndexingBufferedSenders} and {@link SearchIndexingBufferedAsyncSender
     * SearchIndexingBufferedAsyncSenders}.
     *
     * @param documentType The {@link TypeReference} representing the document type associated with the sender.
     * @param <T> The type of the document that the buffered sender will use.
     * @return A new instance of {@link SearchIndexingBufferedSenderBuilder}.
     */
    public <T> SearchIndexingBufferedSenderBuilder<T> bufferedSender(TypeReference<T> documentType) {
        return new SearchIndexingBufferedSenderBuilder<>();
    }

    private void validateIndexNameAndEndpoint() {
        Objects.requireNonNull(indexName, "'indexName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
    }

    private HttpPipeline getHttpPipeline() {
        if (httpPipeline != null) {
            return httpPipeline;
        }

        return Utility.buildHttpPipeline(clientOptions, httpLogOptions, configuration, retryPolicy,
            azureKeyCredential, tokenCredential, perCallPolicies, perRetryPolicies, httpClient, logger);
    }

    /**
     * Sets the service endpoint for the Azure Cognitive Search instance.
     *
     * @param endpoint The URL of the Azure Cognitive Search instance.
     * @return The updated SearchClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public SearchClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param credential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder credential(AzureKeyCredential credential) {
        this.azureKeyCredential = credential;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param credential The {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets the name of the index.
     *
     * @param indexName Name of the index.
     * @return The updated SearchClientBuilder object.
     * @throws IllegalArgumentException If {@code indexName} is null or empty.
     */
    public SearchClientBuilder indexName(String indexName) {
        if (CoreUtils.isNullOrEmpty(indexName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'indexName' cannot be null or empty."));
        }
        this.indexName = indexName;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     * <p>
     * If logging configurations aren't provided HTTP requests and responses won't be logged.
     *
     * @param logOptions The logging configuration for HTTP requests and responses.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Gets the default Azure Search headers and query parameters allow list.
     *
     * @return The default {@link HttpLogOptions} allow list.
     */
    public static HttpLogOptions getDefaultLogOptions() {
        return Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Adds a pipeline policy to apply to each request sent.
     * <p>
     * This method may be called multiple times, each time it is called the policy will be added to the end of added
     * policy list. All policies will be added after the retry policy.
     *
     * @param policy The pipeline policies to added to the policy list.
     * @return The updated SearchClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public SearchClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Custom JSON serializer that is used to handle model types that are not contained in the Azure Search Documents
     * library.
     *
     * @param jsonSerializer The serializer to serialize user defined models.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder serializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending requests and receiving responses.
     *
     * @param client The HTTP client that will handle sending requests and receiving responses.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} and
     * {@link #indexName(String) index} when building a {@link SearchClient} or {@link SearchAsyncClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store that will be used.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that will attempt to retry requests when needed.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     *
     * @param retryPolicy The {@link RetryPolicy} that will attempt to retry requests when needed.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link SearchServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, {@link SearchServiceVersion#getLatest()} will be used as a default. When
     * the default is used, updating to a newer client library may implicitly use a newer version of the service.
     *
     * @param serviceVersion The version of the service to be used when making requests.
     * @return The updated SearchClientBuilder object.
     */
    public SearchClientBuilder serviceVersion(SearchServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

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
    @ServiceClientBuilder(serviceClients = {
        SearchIndexingBufferedSender.class, SearchIndexingBufferedAsyncSender.class
    })
    public final class SearchIndexingBufferedSenderBuilder<T> {
        private final ClientLogger logger = new ClientLogger(SearchIndexingBufferedSenderBuilder.class);

        private Function<T, String> documentKeyRetriever;

        private boolean autoFlush = DEFAULT_AUTO_FLUSH;
        private Duration autoFlushInterval = DEFAULT_FLUSH_INTERVAL;
        private int initialBatchActionCount = DEFAULT_INITIAL_BATCH_ACTION_COUNT;
        //    private Function<Integer, Integer> scaleDownFunction = DEFAULT_SCALE_DOWN_FUNCTION;
        private int maxRetriesPerAction = DEFAULT_MAX_RETRIES_PER_ACTION;
        private Duration throttlingDelay = DEFAULT_THROTTLING_DELAY;
        private Duration maxThrottlingDelay = DEFAULT_MAX_THROTTLING_DELAY;

        private Consumer<OnActionAddedOptions<T>> onActionAddedConsumer;
        private Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer;
        private Consumer<OnActionErrorOptions<T>> onActionErrorConsumer;
        private Consumer<OnActionSentOptions<T>> onActionSentConsumer;

        private SearchIndexingBufferedSenderBuilder() {
        }

        /**
         * Creates a {@link SearchIndexingBufferedSender} based on options set in the builder. Every time this is called
         * a new instance of {@link SearchIndexingBufferedSender} is created.
         *
         * @return A SearchIndexingBufferedSender with the options set from the builder.
         * @throws NullPointerException If {@code indexName}, {@code endpoint}, or {@code documentKeyRetriever} are
         * null.
         */
        public SearchIndexingBufferedSender<T> buildSender() {
            return new SearchIndexingBufferedSender<>(buildAsyncSender());
        }

        /**
         * Creates a {@link SearchIndexingBufferedAsyncSender} based on options set in the builder. Every time this is
         * called a new instance of {@link SearchIndexingBufferedAsyncSender} is created.
         *
         * @return A SearchIndexingBufferedAsyncSender with the options set from the builder.
         * @throws NullPointerException If {@code indexName}, {@code endpoint}, or {@code documentKeyRetriever} are
         * null.
         */
        public SearchIndexingBufferedAsyncSender<T> buildAsyncSender() {
            validateIndexNameAndEndpoint();
            Objects.requireNonNull(documentKeyRetriever, "'documentKeyRetriever' cannot be null");
            return new SearchIndexingBufferedAsyncSender<>(buildRestClient(endpoint, indexName, getHttpPipeline(),
                getDefaultSerializerAdapter()), jsonSerializer, documentKeyRetriever, autoFlush, autoFlushInterval,
                initialBatchActionCount, maxRetriesPerAction, throttlingDelay, maxThrottlingDelay,
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
                throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
            }

            this.initialBatchActionCount = initialBatchActionCount;
            return this;
        }

        // Retaining this commented out code as it may be added back in a future release.
//    /**
//     * Sets the function that handles scaling down the batch size when a 413 (Payload too large) response is returned
//     * by the service.
//     * <p>
//     * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
//     *
//     * @param scaleDownFunction The batch size scale down function.
//     * @return The updated SearchIndexingBufferedSenderOptions object.
//     * @throws NullPointerException If {@code scaleDownFunction} is null.
//     */
//    public SearchIndexingBufferedSenderOptions<T> setPayloadTooLargeScaleDown(
//        Function<Integer, Integer> scaleDownFunction) {
//        this.scaleDownFunction = Objects.requireNonNull(scaleDownFunction, "'scaleDownFunction' cannot be null.");
//        return this;
//    }

        // Retaining this commented out code as it may be added back in a future release.
//    /**
//     * Gets the function that handles scaling down the batch size when a 413 (Payload too large) response is returned
//     * by the service.
//     * <p>
//     * By default the batch size will halve when a 413 is returned with a minimum allowed value of one.
//     *
//     * @return The batch size scale down function.
//     */
//    public Function<Integer, Integer> getPayloadTooLargeScaleDown() {
//        return scaleDownFunction;
//    }

        /**
         * Sets the number of times an action will retry indexing before it is considered failed.
         * <p>
         * Documents are only retried on retryable status codes.
         * <p>
         * Default value is {@code 3}.
         *
         * @param maxRetriesPerAction The number of times a document will retry indexing before it is considered
         * failed.
         * @return The updated SearchIndexingBufferedSenderBuilder object.
         * @throws IllegalArgumentException If {@code maxRetriesPerAction} is less than one.
         */
        public SearchIndexingBufferedSenderBuilder<T> maxRetriesPerAction(int maxRetriesPerAction) {
            if (maxRetriesPerAction < 1) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("'maxRetries' cannot be less than one."));
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
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("'throttlingDelay' cannot be negative or zero."));
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
                throw logger.logExceptionAsError(
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
        public SearchIndexingBufferedSenderBuilder<T> onActionAdded(
            Consumer<OnActionAddedOptions<T>> onActionAddedConsumer) {
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
        public SearchIndexingBufferedSenderBuilder<T> onActionSucceeded(
            Consumer<OnActionSucceededOptions<T>> onActionSucceededConsumer) {
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
        public SearchIndexingBufferedSenderBuilder<T> onActionError(
            Consumer<OnActionErrorOptions<T>> onActionErrorConsumer) {
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
        public SearchIndexingBufferedSenderBuilder<T> onActionSent(
            Consumer<OnActionSentOptions<T>> onActionSentConsumer) {
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
        public SearchIndexingBufferedSenderBuilder<T> documentKeyRetriever(Function<T, String> documentKeyRetriever) {
            this.documentKeyRetriever = Objects.requireNonNull(documentKeyRetriever,
                "'documentKeyRetriever' cannot be null");
            return this;
        }
    }
}
