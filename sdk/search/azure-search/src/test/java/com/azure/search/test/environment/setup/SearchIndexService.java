// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test.environment.setup;

import com.azure.search.SearchApiKeyCredential;
import com.azure.search.SearchServiceClient;
import com.azure.search.SearchServiceClientBuilder;
import com.azure.search.TestHelpers;
import com.azure.search.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SearchIndexService {

    private String endpoint;
    private String apiAdminKey;
    private String indexName;

    private SearchServiceClient searchServiceClient;

    /**
     * Creates an instance of SearchIndexService to be used in creating a sample index in Azure Cognitive Search, to be
     * used in tests.
     *
     * @param endpoint the endpoint of an Azure Cognitive Search instance.
     * @param apiAdminKey the Admin Key of Azure Cognitive Search service
     */
    public SearchIndexService(String endpoint, String apiAdminKey) {
        this.endpoint = endpoint;
        this.apiAdminKey = apiAdminKey;
    }

    /**
     * Creates a new sample Index in Azure Cognitive Search with configuration retrieved from INDEX_DATA_JSON
     *
     * @param index the index to be created.
     */
    public void initializeAndCreateIndex(Index index) {
        initServiceClient();

        if (index != null) {
            this.indexName = index.getName();
            searchServiceClient.createOrUpdateIndex(index);
        }
    }

    /**
     * Creates a new sample Index in Azure Cognitive Search with configuration retrieved from INDEX_DATA_JSON
     *
     * @param indexDataFileName the name of a file that contains a JSON index definition.
     * @throws IOException thrown when indexDataFileName does not exist or has invalid contents.
     */
    public void initializeAndCreateIndex(String indexDataFileName) throws IOException {
        initServiceClient();

        if (!TestHelpers.isBlank(indexDataFileName)) {
            Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(indexDataFileName));
            Index index = new ObjectMapper().readValue(indexData, Index.class);
            this.indexName = index.getName();
            searchServiceClient.createOrUpdateIndex(index);
        }
    }

    private void initServiceClient() {
        if (searchServiceClient == null) {
            searchServiceClient = new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .credential(new SearchApiKeyCredential(apiAdminKey))
                .buildClient();
        }
    }

    /**
     * @return the sample index name
     */
    public String indexName() {
        return this.indexName;
    }
}
