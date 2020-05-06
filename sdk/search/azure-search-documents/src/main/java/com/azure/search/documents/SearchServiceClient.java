// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchDataSourceClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchSkillsetClient;
import com.azure.search.documents.indexes.SearchSynonymMapClient;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.ServiceStatistics;

/**
 * Synchronous Client to manage and query indexes, as well as manage other resources, on a Cognitive Search service
 */
@ServiceClient(builder = SearchServiceClientBuilder.class)
public final class SearchServiceClient {
    private final SearchServiceAsyncClient asyncClient;

    SearchServiceClient(SearchServiceAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.asyncClient.getHttpPipeline();
    }

    /**
     * Initializes a new {@link SearchClient} using the given Index name and the same configuration as the
     * SearchServiceClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchClient} created from the service client configuration
     */
    public SearchClient getSearchClient(String indexName) {
        return new SearchClient(asyncClient.getSearchAsyncClient(indexName));
    }

    /**
     * Initializes a new {@link SearchDataSourceClient} using the same configuration as the SearchServiceClient.
     *
     * @return a {@link SearchDataSourceClient} created from the service client configuration.
     */
    public SearchDataSourceClient getDataSourceClient() {
        return asyncClient.prepareBuilder().buildDataSourceClient();
    }

    /**
     * Initializes a new {@link SearchIndexClient} using the same configuration as the SearchServiceClient.
     *
     * @return a {@link SearchIndexClient} created from the service client configuration.
     */
    public SearchIndexClient getSearchIndexClient() {
        return asyncClient.prepareBuilder().buildSearchIndexClient();
    }

    /**
     * Initializes a new {@link SearchIndexerClient} using the same configuration as the SearchServiceClient.
     *
     * @return a {@link SearchIndexerClient} created from the service client configuration.
     */
    public SearchIndexerClient getSearchIndexerClient() {
        return asyncClient.prepareBuilder().buildSearchIndexerClient();
    }

    /**
     * Initializes a new {@link SearchSkillsetClient} using the same configuration as the SearchServiceClient.
     *
     * @return a {@link SearchSkillsetClient} created from the service client configuration.
     */
    public SearchSkillsetClient getSkillsetClient() {
        return asyncClient.prepareBuilder().buildSkillsetClient();
    }

    /**
     * Initializes a new {@link SearchSynonymMapClient} using the same configuration as the SearchServiceClient.
     *
     * @return a {@link SearchSynonymMapClient} created from the service client configuration.
     */
    public SearchSynonymMapClient getSynonymMapClient() {
        return asyncClient.prepareBuilder().buildSynonymMapClient();
    }

    /**
     * Gets search service version.
     *
     * @return the search service version value.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.asyncClient.getServiceVersion();
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.asyncClient.getEndpoint();
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @return the search service statistics result.
     */
    public ServiceStatistics getServiceStatistics() {
        return getServiceStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the search service statistics result.
     */
    public Response<ServiceStatistics> getServiceStatisticsWithResponse(RequestOptions requestOptions,
        Context context) {
        return asyncClient.getServiceStatisticsWithResponse(requestOptions, context).block();
    }
}
