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
    private String searchDnsSuffix;
    private String apiAdminKey;
    private String indexName;
    private String indexDataFileName;

    private SearchServiceRestClientImpl searchServiceClient;

    /**
     * Creates an instance of SearchIndexService to be used in creating a sample index in Azure Search,
     * to be used in tests.
     *
     * @param indexDataFileName the name of a file that contains a JSON index definition.
     * @param searchServiceName the name of Search Service in Azure.
     * @param searchServiceName the DNS suffix for the Search Service.
     * @param apiAdminKey       the Admin Key of Search Service
     */
    public SearchIndexService(String indexDataFileName, String searchServiceName, String searchDnsSuffix, String apiAdminKey) {
        this.indexDataFileName = indexDataFileName;
        this.searchServiceName = searchServiceName;
        this.searchDnsSuffix = searchDnsSuffix;
        this.apiAdminKey = apiAdminKey;
    }

    /**
     * Creates a new sample Index in Azure Search with configuration retrieved from INDEX_DATA_JSON
     *
     * @throws IOException thrown when indexDataFileName does not exist or has invalid contents.
     */
    public void initialize() throws IOException {
        validate();

        if (searchServiceClient == null) {
            searchServiceClient = new SearchServiceRestClientBuilder()
                .apiVersion("2019-05-06")
                .searchServiceName(searchServiceName)
                .searchDnsSuffix(searchDnsSuffix)
                .pipeline(
                    new HttpPipelineBuilder()
                        .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                        .policies(new SearchApiKeyPipelinePolicy(new ApiKeyCredentials(apiAdminKey)))
                        .build()
                ).build();
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
        this.indexName = index.getName();
        searchServiceClient.indexes()
            .createOrUpdateWithRestResponseAsync(index.getName(), index, Context.NONE)
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
