// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingSchedule;
import com.azure.search.documents.indexes.models.SearchIndexer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class CreateIndexerExample {

    /**
     * This example shows how to create a new Indexer in a Cognitive Search Service.
     * <p>
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");


    private static final String INDEX_NAME = "hotels-sample-index";
    private static final String DATA_SOURCE_NAME = "hotels-sample";
    private static final String INDEXER_NAME = "hotels-indexer-test";

    public static void main(String[] args) {
        SearchIndexerAsyncClient indexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildAsyncClient();

        createOrUpdateIndexer(indexerAsyncClient);
    }

    private static void createOrUpdateIndexer(SearchIndexerAsyncClient searchIndexerAsyncClient) {
        // Create indexer parameters
        IndexingParameters indexingParameters = new IndexingParameters()
            .setBatchSize(50)
            .setMaxFailedItems(10)
            .setMaxFailedItemsPerBatch(10);

        // Create field mappings
        List<FieldMapping> fieldMappings = Collections.singletonList(new FieldMapping("id")
            .setTargetFieldName("HotelId"));

        // Create schedule
        IndexingSchedule indexingSchedule = new IndexingSchedule(Duration.ofHours(12));

        // Create the indexer
        SearchIndexer indexer = new SearchIndexer(INDEXER_NAME, DATA_SOURCE_NAME, INDEX_NAME)
            .setParameters(indexingParameters)
            .setFieldMappings(fieldMappings)
            .setSchedule(indexingSchedule);

        System.out.println(String.format("Creating Indexer: %s", indexer.getName()));
        Response<SearchIndexer> response = searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(
            indexer, false
        ).block();

        if (response != null) {
            System.out.println(String.format("Response code: %s", response.getStatusCode()));

            SearchIndexer createdIndexer = response.getValue();
            System.out.println(String
                .format("Created indexer name: %s, ETag: %s", createdIndexer.getName(), createdIndexer.getETag()));
        }
    }
}
