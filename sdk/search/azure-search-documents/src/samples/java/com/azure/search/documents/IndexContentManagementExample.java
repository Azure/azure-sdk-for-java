// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This example shows how to manage the contents of an Azure Cognitive Search index.
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class IndexContentManagementExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        basicIndexing();
        advancedIndexing();
    }

    /**
     * Quickly upload, merge, mergeOrUpload, and delete Lists of the same
     * type directly from the index client
     */
    private static void basicIndexing() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
        List<SearchDocument> documents = new ArrayList<>();
        documents.add(new SearchDocument(new HashMap<String, String>(){{
            put("HotelId", "100");
        }}));
        documents.add(new SearchDocument(new HashMap<String, String>(){{
            put("HotelId", "200");
        }}));
        documents.add(new SearchDocument(new HashMap<String, String>(){{
            put("HotelId", "300");
        }}));

        // Perform index operations on a list of documents
        IndexDocumentsResult result = client.mergeOrUploadDocuments(documents);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }

    /**
     * Advanced usage that includes different types of operations in a single batch
     */
    private static void advancedIndexing() {
        SearchClient client = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();

        IndexDocumentsBatch<SearchDocument> batch = new IndexDocumentsBatch<SearchDocument>()
            .addMergeOrUploadActions(new SearchDocument(new HashMap<String, String>(){{
                put("HotelId", "100");
            }}))
            .addDeleteActions(new SearchDocument(new HashMap<String, String>(){{
                put("HotelId", "200");
            }}));

        // Send a single batch that performs many different actions
        IndexDocumentsResult result = client.indexDocuments(batch);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }
}
