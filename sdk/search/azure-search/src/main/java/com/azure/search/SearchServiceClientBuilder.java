// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
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
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * SearchServiceClient SearchServiceClients} and {@link SearchServiceAsyncClient SearchServiceAsyncClients}. Call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * The following information must be provided on this builder: the Azure Cognitive Search service endpoint through
 * {@code .endpoint()} the API key through {@code .credential()}
 */
@ServiceClientBuilder(serviceClients = {SearchServiceClient.class, SearchServiceAsyncClient.class})
public class SearchServiceClientBuilder {
    private final ClientLogger logger = new ClientLogger(SearchServiceClientBuilder.class);

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

    /**
     * Default Constructor
     */
    public SearchServiceClientBuilder() {
        apiVersion = SearchServiceVersion.getLatest();
        policies = new ArrayList<>();
        httpClient = HttpClient.createDefault();
        httpLogOptions = new HttpLogOptions();

        Map<String, String> properties = CoreUtils.getProperties(SEARCH_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder apiVersion(SearchServiceVersion apiVersion) {
        if (apiVersion == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets the Azure Cognitive Search service endpoint
     *
     * @param endpoint the endpoint URL to the Azure Cognitive Search service
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchServiceClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the http client (optional). If this is not set, a default httpClient will be created
     *
     * @param httpClient value of httpClient
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the api key to use for request authentication.
     *
     * @param searchApiKeyCredential api key for request authentication
     * @return the updated SearchServiceClientBuilder object
     * @throws IllegalArgumentException when the api key is empty
     */
    public SearchServiceClientBuilder credential(SearchApiKeyCredential searchApiKeyCredential) {
        if (searchApiKeyCredential == null || CoreUtils.isNullOrEmpty(searchApiKeyCredential.getApiKey())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Empty apiKeyCredentials"));
        }
        this.searchApiKeyCredential = searchApiKeyCredential;
        return this;
    }

    /**
     * Http Pipeline policy
     *
     * @param policy policy to add to the pipeline
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
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
    public SearchServiceClientBuilder configuration(Configuration configuration) {
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
    public SearchServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * @return a {@link SearchServiceClient} created from the configurations in this builder.
     */
    public SearchServiceClient buildClient() {
        return new SearchServiceClient(buildAsyncClient());
    }

    /**
     * @return a {@link SearchIndexAsyncClient} created from the configurations in this builder.
     */
    public SearchServiceAsyncClient buildAsyncClient() {
        return new SearchServiceAsyncClient(endpoint, apiVersion, prepareForBuildClient());
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
