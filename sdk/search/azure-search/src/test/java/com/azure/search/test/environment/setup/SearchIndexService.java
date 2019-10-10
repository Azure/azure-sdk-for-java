// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.search.ApiKeyCredentials;
import com.azure.search.common.SearchApiKeyPipelinePolicy;
import com.azure.search.implementation.SearchServiceRestClientBuilder;
import com.azure.search.implementation.SearchServiceRestClientImpl;
import com.azure.search.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SearchIndexService {

    private String searchServiceName;
    private String apiAdminKey;
    private String indexName;
    private String indexDataFileName;

    private SearchServiceRestClientImpl searchServiceClient;

    /**
     * Creates an instance of SearchIndexService to be used in creating a sample index in Azure Search,
     * to be used in tests.
     *
     * @param searchServiceName the name of Search Service in Azure.
     * @param apiAdminKey       the Admin Key of Search Service
     */
    public SearchIndexService(String indexDataFileName, String searchServiceName, String apiAdminKey) {
        this.indexDataFileName = indexDataFileName;
        this.searchServiceName = searchServiceName;
        this.apiAdminKey = apiAdminKey;
    }

    /**
     * Creates a new sample Index in Azure Search with configuration retrieved from INDEX_DATA_JSON
     *
     * @throws IOException If the file in INDEX_DATA_JSON is not existing or invalid.
     */
    public void initialize() throws IOException {
        validate();

        if (searchServiceClient == null) {
            searchServiceClient = new SearchServiceRestClientBuilder()
                .apiVersion("2019-05-06")
                .searchServiceName(searchServiceName)
                .pipeline(
                    new HttpPipelineBuilder()
                        .httpClient(new NettyAsyncHttpClientBuilder().setWiretap(true).build())
                        .policies(new SearchApiKeyPipelinePolicy(new ApiKeyCredentials(apiAdminKey)))
                        .build())
                .build();
        }
        addIndexes();
    }

    private void validate() {
        if (StringUtils.isBlank(this.searchServiceName)) {
            throw new IllegalArgumentException("searchServiceName cannot be blank");
        }
        if (StringUtils.isBlank(this.apiAdminKey)) {
            throw new IllegalArgumentException("apiAdminKey cannot be blank");
        }
    }

    private void addIndexes() throws IOException {
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(indexDataFileName));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        this.indexName = index.name();
        searchServiceClient.indexes()
            .createOrUpdateWithRestResponseAsync(index.name(), index, Context.NONE)
            .block();
    }

    /**
     *
     * @return the sample index name
     */
    public String indexName() {
        return this.indexName;
    }

    public SearchServiceRestClientImpl searchServiceClient() {
        return searchServiceClient;
    }
}
