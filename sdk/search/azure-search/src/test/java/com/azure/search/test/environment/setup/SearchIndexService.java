// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.azure.search.ApiKeyCredentials;
import com.azure.search.SearchServiceClient;
import com.azure.search.SearchServiceClientBuilder;
import com.azure.search.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SearchIndexService {

    private String endpoint;
    private String apiAdminKey;
    private String indexName;
    private String indexDataFileName;

    private SearchServiceClient searchServiceClient;

    /**
     * Creates an instance of SearchIndexService to be used in creating a sample index in Azure Search,
     * to be used in tests.
     *
     * @param indexDataFileName the name of a file that contains a JSON index definition.
     * @param endpoint the endpoint of an Azure Cognitive Search instance.
     * @param apiAdminKey       the Admin Key of Search Service
     */
    public SearchIndexService(String indexDataFileName, String endpoint, String apiAdminKey) {
        this.indexDataFileName = indexDataFileName;
        this.endpoint = endpoint;
        this.apiAdminKey = apiAdminKey;
    }

    /**
     * Creates a new sample Index in Azure Search with configuration retrieved from INDEX_DATA_JSON
     *
     * @throws IOException thrown when indexDataFileName does not exist or has invalid contents.
     */
    public void initialize() throws IOException {
        if (searchServiceClient == null) {
            searchServiceClient = new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .credential(new ApiKeyCredentials(apiAdminKey))
                .buildClient();
        }
        addIndexes();
    }

    private void addIndexes() throws IOException {
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(indexDataFileName));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        this.indexName = index.getName();
        searchServiceClient.createOrUpdateIndex(index);
    }

    /**
     *
     * @return the sample index name
     */
    public String indexName() {
        return this.indexName;
    }
}
