// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.SearchPipelinePolicy;

/**
 * Fluent SearchIndexClientBuilder for instantiating a {@link SearchIndexClientImpl} or a {@link SearchIndexAsyncClientImpl}
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
 * {@link SearchIndexClientImpl} or {@code .buildAsyncClient()} to create a
 *  * {@link SearchIndexAsyncClientImpl}
 */
@ServiceClientBuilder(serviceClients = SearchIndexClientImpl.class)
public class SearchIndexClientBuilder {

    private String apiVersion;
    private String serviceName;
    private String indexName;
    private SearchPipelinePolicy policy;
    private String searchDnsSuffix;

    /**
     * Default Constructor
     */
    public SearchIndexClientBuilder() {
        searchDnsSuffix = "search.windows.net";
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
     * @param serviceName name of the service
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
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
     * Set the authentication policy (api-key)
     *
     * @param policy value of api-key
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder policy(SearchPipelinePolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Set search service dns suffix
     *
     * @param searchDnsSuffix search service dns suffix
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder searchDnsSuffix(String searchDnsSuffix) {
        this.searchDnsSuffix = searchDnsSuffix;
        return this;
    }

    /**
     * @return a {@link SearchIndexClient} created from the configurations in this builder.
     */
    public SearchIndexClient buildClient() {

        return new SearchIndexClientImpl(buildAsyncClient());
    }

    /**
     * @return a {@link SearchIndexAsyncClient} created from the configurations in this builder.
     */
    public SearchIndexAsyncClient buildAsyncClient() {
        return new SearchIndexAsyncClientImpl(serviceName, searchDnsSuffix, indexName, apiVersion, policy);
    }
}
