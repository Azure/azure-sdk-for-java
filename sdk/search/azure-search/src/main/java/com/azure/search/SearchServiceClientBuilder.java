// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.common.SearchApiKeyPipelinePolicy;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ServiceClientBuilder(serviceClients = { SearchServiceClient.class, SearchServiceAsyncClient.class})
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
        httpClient = new NettyAsyncHttpClientBuilder().setWiretap(true).build();
        policies = new ArrayList<>();
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
     * Sets search service name
     *
     * @param serviceName name of the service
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
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
     * Sets the api key to use for requests authentication.
     * @param apiKeyCredentials api key for requests authentication
     * @throws IllegalArgumentException when the api key is empty
     * @return the updated SearchServiceClientBuilder object
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
     * Set search service dns suffix
     *
     * @param searchDnsSuffix search service dns suffix
     * @return the updated SearchServiceClientBuilder object
     */
    public SearchServiceClientBuilder searchDnsSuffix(String searchDnsSuffix) {
        this.searchDnsSuffix = searchDnsSuffix;
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

        return new SearchServiceAsyncClient(serviceName,
            searchDnsSuffix,
            apiVersion,
            httpClient,
            policies);
    }
}
