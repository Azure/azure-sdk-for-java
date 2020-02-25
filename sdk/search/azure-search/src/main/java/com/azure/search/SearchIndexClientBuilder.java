// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link SearchIndexClient
 * SearchIndexClients} and {@link SearchIndexAsyncClient SearchIndexAsyncClients}. Call {@link #buildClient()
 * buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired
 * client.
 *
 * The following information must be provided on this builder: the Azure Cognitive Search service endpoint through
 * {@code .endpoint()} the index name through {@code .indexName()} the API key through {@code .credential()}
 */
@ServiceClientBuilder(serviceClients = {SearchIndexClient.class, SearchIndexAsyncClient.class})
public class SearchIndexClientBuilder {

    // This header tells the server to return the request id in the HTTP response. Useful for correlation with what
    // request was sent.
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";

    private static final String SEARCH_PROPERTIES = "azure-search.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";

    SearchApiKeyCredential searchApiKeyCredential;
    SearchServiceVersion apiVersion;
    String endpoint;
    HttpClient httpClient;
    HttpLogOptions httpLogOptions;
    Configuration configuration;
    List<HttpPipelinePolicy> policies;
    private String clientName;
    private String clientVersion;

    private String indexName;
    private final HttpHeaders headers;
    private RetryPolicy retryPolicy;
    private final ClientLogger logger = new ClientLogger(SearchIndexClientBuilder.class);

    /**
     * Default Constructor
     */
    public SearchIndexClientBuilder() {
        apiVersion = SearchServiceVersion.getLatest();
        policies = new ArrayList<>();
        httpClient = HttpClient.createDefault();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(SEARCH_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
        headers = new HttpHeaders()
            .put(ECHO_REQUEST_ID_HEADER, "true");
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder apiVersion(SearchServiceVersion apiVersion) {
        if (apiVersion == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Returns the list of policies configured for this builder.
     *
     * @return List of HttpPipelinePolicy
     */
    List<HttpPipelinePolicy> getPolicies() {
        return this.policies;
    }

    /**
     * Sets the Azure Cognitive Search service endpoint
     *
     * @param endpoint the endpoint URL to the Azure Cognitive Search service
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException on invalid service endpoint
     */
    public SearchIndexClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
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
        if (CoreUtils.isNullOrEmpty(indexName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid indexName"));
        }
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
     */
    public SearchIndexClientBuilder credential(SearchApiKeyCredential searchApiKeyCredential) {
        if (searchApiKeyCredential == null) {
            throw logger.logExceptionAsError(new NullPointerException("Empty apiKeyCredentials"));
        }
        if (CoreUtils.isNullOrEmpty(searchApiKeyCredential.getApiKey())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Empty apiKeyCredentials"));
        }
        this.searchApiKeyCredential = searchApiKeyCredential;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client. The default configuration
     * store is a clone of the {@link Configuration#getGlobalConfiguration() global configuration store}, use {@link
     * Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Http Pipeline policy
     *
     * @param policy policy to add to the pipeline
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        this.policies.add(policy);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided {@link SearchIndexClientBuilder#buildAsyncClient()} to
     * build {@link SearchServiceAsyncClient} or {@link SearchServiceClient}.
     *
     * @param retryPolicy RetryPolicy applied to each request.
     * @return The updated SearchIndexClientBuilder object.
     */
    public SearchIndexClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
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

        policies.add(new AddHeadersPolicy(headers));
        // We need to add RequestId and override the default header, with the one
        // That the service expects, in order to capture the request ids.
        policies.add(new RequestIdPolicy("client-request-id"));
        policies.add(new AddDatePolicy());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        return new SearchIndexAsyncClient(endpoint, indexName, apiVersion, prepareForBuildClient());
    }

    HttpPipeline prepareForBuildClient() {
        // Global Env configuration store
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        if (searchApiKeyCredential != null) {
            this.policies.add(new SearchApiKeyPipelinePolicy(searchApiKeyCredential));
        }

        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}
