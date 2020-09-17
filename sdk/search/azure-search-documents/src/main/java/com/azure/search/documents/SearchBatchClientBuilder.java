// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.implementation.util.Constants;
import com.azure.search.documents.implementation.util.Utility;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link SearchBatchClient
 * SearchBatchClients} and {@link SearchBatchAsyncClient SearchBatchAsyncClients}. Call {@link #buildClient()
 * buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired
 * client.
 * <p>
 * The following must be provided to construct a client instance.
 * <ul>
 * <li>The Azure Cognitive Search service URL.</li>
 * <li>An {@link AzureKeyCredential} that grants access to the Azure Cognitive Search service.</li>
 * <li>The search index name.</li>
 * </ul>
 *
 * @see SearchBatchClient
 * @see SearchBatchAsyncClient
 */
@ServiceClientBuilder(serviceClients = {SearchBatchClient.class,
    SearchBatchAsyncClient.class})
public final class SearchBatchClientBuilder {
    private final ClientLogger logger = new ClientLogger(SearchBatchClientBuilder.class);
    private final List<HttpPipelinePolicy> policies = new ArrayList<>();

    private AzureKeyCredential credential;
    private SearchServiceVersion serviceVersion;
    private String endpoint;
    private HttpClient httpClient;
    private HttpPipeline httpPipeline;
    private HttpLogOptions httpLogOptions;
    private Configuration configuration;
    private String indexName;
    private RetryPolicy retryPolicy;
    private JsonSerializer jsonSerializer;

    private Boolean autoFlush;
    private Duration flushWindow;
    private Integer batchSize;
    private IndexingHook indexingHook;

    /**
     * Creates a builder instance that is able to configure and construct {@link SearchBatchClient SearchBatchClients}
     * and {@link SearchBatchAsyncClient SearchBatchAsyncClients}.
     */
    public SearchBatchClientBuilder() {
    }

    /**
     * Creates a {@link SearchBatchClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link SearchBatchClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchBatchClient client}. All
     * other builder settings are ignored.
     *
     * @return A SearchBatchClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are null.
     */
    public SearchBatchClient buildClient() {
        return new SearchBatchClient(buildAsyncClient());
    }

    /**
     * Creates a {@link SearchBatchAsyncClient} based on options set in the builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link SearchBatchAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline}, {@link #endpoint(String)
     * endpoint}, and {@link #indexName(String) indexName} are used to create the {@link SearchBatchAsyncClient client}.
     * All other builder settings are ignored.
     *
     * @return A SearchBatchAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code indexName} or {@code endpoint} are null.
     */
    public SearchBatchAsyncClient buildAsyncClient() {
        Objects.requireNonNull(indexName, "'indexName' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        SearchServiceVersion buildVersion = (serviceVersion == null)
            ? SearchServiceVersion.getLatest()
            : serviceVersion;

        SearchAsyncClient searchAsyncClient;
        if (httpPipeline != null) {
            searchAsyncClient = new SearchAsyncClient(endpoint, indexName, buildVersion, httpPipeline,
                jsonSerializer);
        } else {
            Objects.requireNonNull(credential, "'credential' cannot be null.");
            HttpPipeline pipeline = Utility.buildHttpPipeline(httpLogOptions, configuration, retryPolicy, credential,
                policies, httpClient);

            searchAsyncClient = new SearchAsyncClient(endpoint, indexName, buildVersion, pipeline, jsonSerializer);
        }

        return new SearchBatchAsyncClient(searchAsyncClient, autoFlush, flushWindow, batchSize, indexingHook);
    }

    /**
     * Sets the service endpoint for the Azure Cognitive Search instance.
     *
     * @param endpoint The URL of the Azure Cognitive Search instance.
     * @return The updated SearchBatchClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public SearchBatchClientBuilder endpoint(String endpoint) {
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
     * @return The updated SearchBatchClientBuilder object.
     * @throws NullPointerException If {@code credential} is null.
     * @throws IllegalArgumentException If {@link AzureKeyCredential#getKey()} is null or empty.
     */
    public SearchBatchClientBuilder credential(AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        return this;
    }

    /**
     * Sets the name of the index.
     *
     * @param indexName Name of the index.
     * @return The updated SearchBatchClientBuilder object.
     * @throws IllegalArgumentException If {@code indexName} is null or empty.
     */
    public SearchBatchClientBuilder indexName(String indexName) {
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
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder httpLogOptions(HttpLogOptions logOptions) {
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
     * Adds a pipeline policy to apply to each request sent.
     * <p>
     * This method may be called multiple times, each time it is called the policy will be added to the end of added
     * policy list. All policies will be added after the retry policy.
     *
     * @param policy The pipeline policies to added to the policy list.
     * @return The updated SearchBatchClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public SearchBatchClientBuilder addPolicy(HttpPipelinePolicy policy) {
        policies.add(Objects.requireNonNull(policy));
        return this;
    }

    /**
     * Custom JSON serializer that is used to handle model types that are not contained in the Azure Search Documents
     * library.
     *
     * @param jsonSerializer The serializer to serialize user defined models.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder serializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending requests and receiving responses.
     *
     * @param client The HTTP client that will handle sending requests and receiving responses.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder httpClient(HttpClient client) {
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
     * {@link #indexName(String) index} when building a {@link SearchBatchClient} and {@link SearchBatchAsyncClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that will attempt to retry requests when needed.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     *
     * @param retryPolicy The {@link RetryPolicy} that will attempt to retry requests when needed.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder serviceVersion(SearchServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Flag determining whether a batching client will automatically flush its document batch based on the
     * configurations of {@link #flushWindow(Duration)} and {@link #batchSize(Integer)}.
     * <p>
     * If {@code autoFlush} is null the client will be set to automatically flush.
     *
     * @param autoFlush Flag determining whether a batching client will automatically flush.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder autoFlush(Boolean autoFlush) {
        this.autoFlush = autoFlush;
        return this;
    }

    /**
     * Duration that the a client will wait between documents being added to the batch before sending them to the
     * index.
     * <p>
     * If {@code flushWindow} is negative or zero and {@link #autoFlush(Boolean)} is enabled the client will flush when
     * {@link #batchSize(Integer)} is met. If {@code flushWindow} is null a default value of 60 seconds is used.
     *
     * @param flushWindow Duration that will be waited between document being added to the batch before they will sent
     * to the index.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder flushWindow(Duration flushWindow) {
        this.flushWindow = flushWindow;
        return this;
    }

    /**
     * The number of documents before a client will send the batch to be indexed.
     * <p>
     * This will only trigger a batch to be sent automatically if {@link #flushWindow} is configured. Default value is
     * {@code 1000}.
     *
     * @param batchSize The number of documents in a batch that will trigger it to be indexed.
     * @return The updated SearchBatchClientBuilder object.
     * @throws IllegalArgumentException If {@code batchSize} is less than one.
     */
    public SearchBatchClientBuilder batchSize(Integer batchSize) {
        if (batchSize != null && batchSize < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'batchSize' cannot be less than one."));
        }
        this.batchSize = batchSize;
        return this;
    }

    /**
     * An {@link IndexingHook} that will be used to handle callback triggers when document indexing actions are added,
     * succeed, fail, or are removed from a batching client's queue.
     *
     * @param indexingHook An implementation of {@link IndexingHook}.
     * @return The updated SearchBatchClientBuilder object.
     */
    public SearchBatchClientBuilder indexingHook(IndexingHook indexingHook) {
        this.indexingHook = indexingHook;
        return this;
    }
}
