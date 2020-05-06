// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.implementation.SearchServiceRestClientImpl;

/**
 * This class provides a client that contains all operations that apply to any search service resource type.
 *
 * @see SearchDataSourceAsyncClient
 * @see SearchIndexAsyncClient
 * @see SearchIndexerAsyncClient
 * @see SearchSkillsetAsyncClient
 * @see SearchSynonymMapAsyncClient
 */
public class SearchServiceResourceAsyncClientBase {
    private final ClientLogger logger = new ClientLogger(SearchServiceResourceAsyncClientBase.class);

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    protected final SearchServiceRestClientImpl restClient;

    SearchServiceResourceAsyncClientBase(String endpoint, SearchServiceVersion serviceVersion,
        HttpPipeline httpPipeline) {
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
}
