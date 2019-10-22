// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.common.SearchApiKeyPipelinePolicy;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent SearchIndexClientBuilder
 * for instantiating a {@link SearchIndexClient} or a {@link SearchIndexAsyncClient}
 * using {@link SearchIndexClientBuilder#buildClient()} or {@link SearchIndexClientBuilder#buildAsyncClient()}
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the search service name through {@code .serviceName()}
 * <li>the index name through {@code .indexName()}
 * <li>the api version through {@code .apiVersion()}
 * <li>the api-key though {@code .policy()}</li>
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link SearchIndexClient} or {@code .buildAsyncClient()} to create a {@link SearchIndexAsyncClient}
 */
@ServiceClientBuilder(serviceClients = { SearchIndexClient.class, SearchIndexAsyncClient.class})
public class SearchIndexClientBuilder {

    private ApiKeyCredentials apiKeyCredentials;
    private String apiVersion;
    private String serviceName;
    private String indexName;
    private String searchDnsSuffix;
    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> policies;

    private final ClientLogger logger = new ClientLogger(SearchIndexClientBuilder.class);

    /**
     * Default Constructor
     */
    public SearchIndexClientBuilder() {
        apiVersion = "2019-05-06";
        policies = new ArrayList<>();
        httpClient = HttpClient.createDefault();
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets search service name
     *
     * @param endpoint the endpoint URL to the search service
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException on invalid service endpoint
     */
    public SearchIndexClientBuilder serviceEndpoint(String endpoint) throws IllegalArgumentException {
        if (StringUtils.isBlank(endpoint)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Illegal endpoint URL: endpoint cannot be blank"));
        }

        URL url;
        try {
            // Using the URL class to validate the given endpoint structure
            url = new URL(endpoint);
        } catch (MalformedURLException exc) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: " + exc.getMessage()));
        }

        // Now that we know that the endpoint is in a valid form, extract the host part
        // (e.g. http://myservice.search.windows.net ==> myservice.search.windows.net) and verify its structure,
        // we expect the service name and domain to be present.
        String extractedHost = url.getHost();
        if (StringUtils.isBlank(extractedHost) || extractedHost.startsWith(".") || extractedHost.endsWith(".")) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        String[] tokens = StringUtils.split(extractedHost, ".");
        if ((tokens.length < 3) || (StringUtils.isBlank(tokens[0]))) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Illegal endpoint URL: invalid host"));
        }

        // split the service name and dns suffix
        this.serviceName = tokens[0];
        int index = StringUtils.indexOf(extractedHost, ".");
        this.searchDnsSuffix = extractedHost.substring(index + 1);
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
     * Sets the api key to use for requests authentication.
     * @param apiKeyCredentials api key for requests authentication
     * @throws IllegalArgumentException when the api key is empty
     * @return the updated SearchIndexClientBuilder object
     * @throws IllegalArgumentException when the api key is empty
     */
    public SearchIndexClientBuilder credential(ApiKeyCredentials apiKeyCredentials) {
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
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder addPolicy(HttpPipelinePolicy policy) {
        this.policies.add(policy);
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
        if (apiKeyCredentials != null) {
            this.policies.add(new SearchApiKeyPipelinePolicy(apiKeyCredentials));
        }

        return new SearchIndexAsyncClient(serviceName,
            searchDnsSuffix,
            indexName,
            apiVersion,
            httpClient,
            policies);
    }
}
