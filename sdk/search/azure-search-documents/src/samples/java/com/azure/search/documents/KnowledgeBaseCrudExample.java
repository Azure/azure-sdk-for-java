// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;

/**
 * This example demonstrates CRUD operations for knowledge bases using the SearchIndexClient.
 * <p>
 * It demonstrates:
 * <ul>
 *     <li>Creating a knowledge base</li>
 *     <li>Getting a knowledge base by name</li>
 *     <li>Listing all knowledge bases</li>
 *     <li>Updating a knowledge base</li>
 *     <li>Deleting a knowledge base</li>
 * </ul>
 * <p>
 * Set the following environment variables before running this sample:
 * <ul>
 *     <li>SEARCH_ENDPOINT - the endpoint of your Azure AI Search service</li>
 *     <li>SEARCH_API_KEY - the admin key of your Azure AI Search service</li>
 * </ul>
 */
public class KnowledgeBaseCrudExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String KB_NAME = "sample-knowledge-base";
    private static final String KS_NAME = "sample-knowledge-source";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();

        try {
            // Create a knowledge base
            KnowledgeBase knowledgeBase = new KnowledgeBase(KB_NAME,
                new KnowledgeSourceReference(KS_NAME));

            KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);
            System.out.println("Created knowledge base: " + created.getName());

            // Get a knowledge base by name
            KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(KB_NAME);
            System.out.println("Retrieved knowledge base: " + retrieved.getName());
            System.out.println("ETag: " + retrieved.getETag());
            System.out.println("Knowledge sources: " + retrieved.getKnowledgeSources().size());

            // List all knowledge bases
            System.out.println("\nAll knowledge bases:");
            searchIndexClient.listKnowledgeBases()
                .forEach(kb -> System.out.println("  - " + kb.getName()));

            // Update a knowledge base
            retrieved.setDescription("Updated description for sample knowledge base");
            KnowledgeBase updated = searchIndexClient.createOrUpdateKnowledgeBase(retrieved);
            System.out.println("\nUpdated knowledge base: " + updated.getName());
            System.out.println("Description: " + updated.getDescription());
        } finally {
            // Delete the knowledge base
            searchIndexClient.deleteKnowledgeBase(KB_NAME);
            System.out.println("\nDeleted knowledge base: " + KB_NAME);
        }
    }
}
