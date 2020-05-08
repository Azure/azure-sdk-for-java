// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerDataSourceAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerSkillsetAsyncClient;
import com.azure.search.documents.indexes.SearchServiceResourceClientBuilder;
import com.azure.search.documents.indexes.SearchSynonymMapAsyncClient;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.ServiceStatistics;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

;

/**
 * Asynchronous Client to manage and query indexes, as well as manage other resources, on a Cognitive Search service
 */
@ServiceClient(builder = SearchServiceClientBuilder.class, isAsync = true)
public final class SearchServiceAsyncClient {

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchServiceAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    protected SearchServiceAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;

        this.restClient = new SearchServiceRestClientBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Gets search service version.
     *
     * @return the search service version value.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Initializes a new {@link SearchAsyncClient} using the given Index name and the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchAsyncClient} created from the service client configuration
     */
    public SearchAsyncClient getSearchAsyncClient(String indexName) {
        return new SearchAsyncClient(endpoint, indexName, serviceVersion, httpPipeline);
    }

    /**
     * Initializes a new {@link SearchIndexerDataSourceAsyncClient} using the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @return a {@link SearchIndexerDataSourceAsyncClient} created from the service client configuration.
     */
    public SearchIndexerDataSourceAsyncClient getSearchIndexerDataSourceAsyncClient() {
        return prepareBuilder().buildDataSourceAsyncClient();
    }

    /**
     * Initializes a new {@link SearchIndexAsyncClient} using the same configuration as the SearchServiceAsyncClient.
     *
     * @return a {@link SearchIndexAsyncClient} created from the service client configuration.
     */
    public SearchIndexAsyncClient getSearchIndexAsyncClient() {
        return prepareBuilder().buildSearchIndexAsyncClient();
    }

    /**
     * Initializes a new {@link SearchIndexerAsyncClient} using the same configuration as the SearchServiceAsyncClient.
     *
     * @return a {@link SearchIndexerAsyncClient} created from the service client configuration.
     */
    public SearchIndexerAsyncClient getSearchIndexerAsyncClient() {
        return prepareBuilder().buildSearchIndexerAsyncClient();
    }

    /**
     * Initializes a new {@link SearchIndexerSkillsetAsyncClient} using the same configuration as the SearchServiceAsyncClient.
     *
     * @return a {@link SearchIndexerSkillsetAsyncClient} created from the service client configuration.
     */
    public SearchIndexerSkillsetAsyncClient getSearchIndexerSkillsetAsyncClient() {
        return prepareBuilder().buildSkillsetAsyncClient();
    }

    /**
     * Initializes a new {@link SearchSynonymMapAsyncClient} using the same configuration as the
     * SearchServiceAsyncClient.
     *
     * @return a {@link SearchSynonymMapAsyncClient} created from the service client configuration.
     */
    public SearchSynonymMapAsyncClient getSynonymMapAsyncClient() {
        return prepareBuilder().buildSynonymMapAsyncClient();
    }


    SearchServiceResourceClientBuilder prepareBuilder() {
        return new SearchServiceResourceClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(getHttpPipeline())
            .serviceVersion(getServiceVersion());
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     * <p>
     * Contains the tracking ID sent with the request to help with debugging
     *
     * @return the search service statistics result.
     */
    public Mono<ServiceStatistics> getServiceStatistics() {
        return getServiceStatisticsWithResponse(null).map(Response::getValue);
    }


    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return the search service statistics result.
     */
    public Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions) {
        return withContext(context -> getServiceStatisticsWithResponse(requestOptions, context));
    }

    Mono<Response<ServiceStatistics>> getServiceStatisticsWithResponse(RequestOptions requestOptions, Context context) {
        try {
            return restClient.getServiceStatisticsWithRestResponseAsync(requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
