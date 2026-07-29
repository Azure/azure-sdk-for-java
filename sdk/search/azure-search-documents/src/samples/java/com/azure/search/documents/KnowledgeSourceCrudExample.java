// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;

/**
 * This example demonstrates CRUD operations for knowledge sources using the SearchIndexClient.
 * <p>
 * It demonstrates:
 * <ul>
 *     <li>Creating a knowledge source backed by a search index</li>
 *     <li>Getting a knowledge source by name</li>
 *     <li>Listing all knowledge sources</li>
 *     <li>Updating a knowledge source</li>
 *     <li>Getting the status of a knowledge source</li>
 *     <li>Deleting a knowledge source</li>
 * </ul>
 * <p>
 * Set the following environment variables before running this sample:
 * <ul>
 *     <li>SEARCH_ENDPOINT - the endpoint of your Azure AI Search service</li>
 *     <li>SEARCH_API_KEY - the admin key of your Azure AI Search service</li>
 *     <li>SEARCH_INDEX_NAME - the name of an existing search index to use as the knowledge source backing</li>
 * </ul>
 */
public class KnowledgeSourceCrudExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String INDEX_NAME = System.getenv("SEARCH_INDEX_NAME");
    private static final String KS_NAME = "sample-knowledge-source";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();

        try {
            // Create a knowledge source backed by a search index
            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(
                KS_NAME,
                new SearchIndexKnowledgeSourceParameters(INDEX_NAME));
            knowledgeSource.setDescription("Knowledge source backed by a search index");

            KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);
            System.out.println("Created knowledge source: " + created.getName());

            // Get a knowledge source by name
            KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(KS_NAME);
            System.out.println("Retrieved knowledge source: " + retrieved.getName());
            System.out.println("Kind: " + retrieved.getKind());

            // List all knowledge sources
            System.out.println("\nAll knowledge sources:");
            searchIndexClient.listKnowledgeSources()
                .forEach(ks -> System.out.println("  - " + ks.getName()));

            // Update a knowledge source
            retrieved.setDescription("Updated description for sample knowledge source");
            KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(retrieved);
            System.out.println("\nUpdated knowledge source: " + updated.getName());

            // Get the status of a knowledge source
            KnowledgeSourceStatus status = searchIndexClient.getKnowledgeSourceStatus(KS_NAME);
            System.out.println("Synchronization status: " + status.getSynchronizationStatus());
        } finally {
            // Delete the knowledge source
            searchIndexClient.deleteKnowledgeSource(KS_NAME);
            System.out.println("\nDeleted knowledge source: " + KS_NAME);
        }
    }
}
