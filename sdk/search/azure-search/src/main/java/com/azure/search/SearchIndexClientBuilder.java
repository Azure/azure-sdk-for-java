// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import org.apache.commons.lang3.StringUtils;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link SearchIndexClient SearchIndexClients} and {@link SearchIndexAsyncClient SearchIndexAsyncClients}.
 * Call {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct
 * an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *     <ul>
 *         <li>the Azure Cognitive Search service endpoint through {@code .endpoint()}
 *         <li>the index name through {@code .indexName()}
 *         <li>the API key through {@code .credential()}</li>
 *     </ul>
 * </p>
 */
@ServiceClientBuilder(serviceClients = {SearchIndexClient.class, SearchIndexAsyncClient.class})
public class SearchIndexClientBuilder extends SearchClientBuilder {

    private final ClientLogger logger = new ClientLogger(SearchIndexClientBuilder.class);
    private String indexName;

    /**
     * Default Constructor
     */
    public SearchIndexClientBuilder() {
        init();
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder apiVersion(SearchServiceVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets the Azure Cognitive Search service endpoint
     *
     * @param endpoint the endpoint URL to the Azure Cognitive Search service
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException on invalid service endpoint
     */
    public SearchIndexClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the index name
     *
     * @param indexName name of the index
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder indexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    /**
     * Set the http client (optional). If this is not set, a default httpClient will be created
     *
     * @param httpClient value of httpClient
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the api key to use for request authentication.
     *
     * @param searchApiKeyCredential api key for request authentication
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException when the api key is empty
     * @throws IllegalArgumentException when the api key is empty
     */
    public SearchIndexClientBuilder credential(SearchApiKeyCredential searchApiKeyCredential) {
        if (searchApiKeyCredential == null || StringUtils.isBlank(searchApiKeyCredential.getApiKey())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Empty apiKeyCredentials"));
        }
        this.searchApiKeyCredential = searchApiKeyCredential;
        return this;
    }

    /**
     * Http Pipeline policy
     *
     * @param policy policy to add to the pipeline
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.policies.add(policy);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * @return a {@link SearchIndexClient} created from the configurations in this builder.
     */
    public SearchIndexClient buildClient() {
        return new SearchIndexClient(buildAsyncClient());
    }

    /**
     * @return a {@link SearchIndexAsyncClient} created from the configurations in this builder.
     */
    public SearchIndexAsyncClient buildAsyncClient() {
        return new SearchIndexAsyncClient(endpoint, indexName, apiVersion, prepareForBuildClient());
    }
}
