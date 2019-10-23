// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.Configuration;
import com.azure.search.models.DocumentIndexResult;
import com.azure.search.models.IndexBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * This example shows how to manage the contents of an Azure Cognitive Search index.
 */
public class IndexContentManagementExample {
    /*
      From the Azure portal, get your Azure Cognitive Search service URL and API key,
      and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        basicIndexing();
        advancedIndexing();
    }

    /**
     * Quickly upload, merge, mergeOrUpload, and delete Lists of the same
     * type directly from the index client
     */
    private static void basicIndexing() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().hotelId("100"));
        hotels.add(new Hotel().hotelId("200"));
        hotels.add(new Hotel().hotelId("300"));

        // Perform index operations on a list of documents
        DocumentIndexResult result = client.mergeOrUploadDocuments(hotels);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }

    /**
     * Advanced usage that includes different types of operations in a single batch
     */
    private static void advancedIndexing() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        IndexBatch<Hotel> batch = new IndexBatch<Hotel>()
            .addMergeOrUploadAction(new Hotel().hotelId("100"))
            .addDeleteAction(new Hotel().hotelId("200"));

        // Send a single batch that performs many different actions
        DocumentIndexResult result = client.index(batch);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }
}
