// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.setupTestEnvironment;

import com.azure.search.service.SearchServiceClient;
import com.azure.search.service.customization.SearchCredentials;
import com.azure.search.service.implementation.SearchServiceClientImpl;
import com.azure.search.service.models.Index;
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

    private SearchServiceClient searchServiceClient;

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
            SearchCredentials searchCredentials = new SearchCredentials(apiAdminKey);
            searchServiceClient = new SearchServiceClientImpl(searchCredentials)
                .withSearchServiceName(searchServiceName);
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
        searchServiceClient.indexes().create(index);
    }

    /**
     *
     * @return the sample index name
     */
    public String indexName() {
        return this.indexName;
    }

    public SearchServiceClient searchServiceClient() {
        return searchServiceClient;
    }
}
