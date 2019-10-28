// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.common.SearchApiKeyPipelinePolicy;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link SearchServiceClient SearchServiceClients} and {@link SearchServiceAsyncClient SearchServiceAsyncClients}.
 * Call {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct
 * an instance of the desired client.
 *
 * <p>
 * The following information must be provided on this builder:
 *     <ul>
 *         <li>the search service endpoint through {@code .endpoint()}
 *         <li>the API key through {@code .credential()}</li>
 *     </ul>
 * </p>
 */
@ServiceClientBuilder(serviceClients = {SearchServiceClient.class, SearchServiceAsyncClient.class})
public class SearchServiceClientBuilder {
    private ApiKeyCredentials apiKeyCredentials;
    private String apiVersion;
    private String serviceName;
    private String searchDnsSuffix;
    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> policies;

    private final ClientLogger logger = new ClientLogger(SearchServiceClientBuilder.class);

    /**
     * Default Constructor
     */
    public SearchServiceClientBuilder() {
        searchDnsSuffix = "search.windows.net";
        apiVersion = "2019-05-06";
        policies = new ArrayList<>();
        httpClient = HttpClient.createDefault();
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets the search service endpoint
     *
     * @param endpoint the endpoint URL to the search service
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException on invalid service endpoint
     */
    public SearchServiceClientBuilder endpoint(String endpoint) throws IllegalArgumentException {
        SearchServiceUrlParser.SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts(endpoint);
        this.serviceName = parts.serviceName;
        this.searchDnsSuffix = parts.dnsSuffix;
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
     * @param apiKeyCredentials api key for request authentication
     * @return the updated SearchServiceClientBuilder object
     * @throws IllegalArgumentException when the api key is empty
     */
    public SearchServiceClientBuilder credential(ApiKeyCredentials apiKeyCredentials) {
        if (apiKeyCredentials == null || StringUtils.isBlank(apiKeyCredentials.getApiKey())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Empty apiKeyCredentials"));
        }
        this.apiKeyCredentials = apiKeyCredentials;
        return this;
    }

    /**
     * Http Pipeline policy
     *
     * @param policy policy to add to the pipeline
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.policies.add(policy);
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
        if (apiKeyCredentials != null) {
            this.policies.add(new SearchApiKeyPipelinePolicy(apiKeyCredentials));
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new SearchServiceAsyncClient(serviceName,
            searchDnsSuffix,
            apiVersion,
            pipeline);
    }
}
