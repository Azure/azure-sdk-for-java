// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This example demonstrates CRUD operations for knowledge bases using the SearchIndexClient.
 * <p>
 * It demonstrates:
 * <ul>
 *     <li>Creating a knowledge base</li>
 *     <li>Getting a knowledge base by name</li>
 *     <li>Listing all knowledge bases</li>
 *     <li>Updating a knowledge base</li>
 *     <li>Creating, retrieving, and updating knowledge base tags</li>
 *     <li>Deleting a knowledge base</li>
 * </ul>
 * <p>
 * Set the following environment variables before running this sample:
 * <ul>
 *     <li>SEARCH_ENDPOINT - the endpoint of your Azure AI Search service</li>
 *     <li>SEARCH_API_KEY - the admin key of your Azure AI Search service</li>
 *     <li>SEARCH_INDEX_NAME - the name of an existing search index</li>
 * </ul>
 */
public class KnowledgeBaseCrudExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String INDEX_NAME = System.getenv("SEARCH_INDEX_NAME");

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();

        String resourceSuffix = UUID.randomUUID().toString().replace("-", "");
        String knowledgeBaseName = "sample-knowledge-base-" + resourceSuffix;
        String knowledgeSourceName = "sample-knowledge-source-" + resourceSuffix;

        try {
            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(INDEX_NAME));
            searchIndexClient.createKnowledgeSource(knowledgeSource);

            Map<String, String> initialTags = new LinkedHashMap<>();
            initialTags.put("environment", "sample");
            initialTags.put("owner", "search-sdk");

            // Tags are customer-defined resource metadata. They don't configure or prove billing attribution.
            KnowledgeBase knowledgeBase
                = new KnowledgeBase(knowledgeBaseName, new KnowledgeSourceReference(knowledgeSourceName))
                    .setTags(initialTags);

            KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);
            System.out.println("Created knowledge base: " + created.getName());
            verifyTags(initialTags, created.getTags(), "create");

            // Get a knowledge base by name
            KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBaseName);
            System.out.println("Retrieved knowledge base: " + retrieved.getName());
            System.out.println("ETag: " + retrieved.getETag());
            System.out.println("Knowledge sources: " + retrieved.getKnowledgeSources().size());
            verifyTags(initialTags, retrieved.getTags(), "get");

            // List all knowledge bases
            System.out.println("\nAll knowledge bases:");
            searchIndexClient.listKnowledgeBases()
                .forEach(kb -> System.out.println("  - " + kb.getName()));

            // Update a knowledge base
            Map<String, String> updatedTags = new LinkedHashMap<>(initialTags);
            updatedTags.put("release", "2026-08-01-preview");
            retrieved.setDescription("Updated description for sample knowledge base");
            retrieved.setTags(updatedTags);
            KnowledgeBase updated = searchIndexClient.createOrUpdateKnowledgeBase(retrieved);
            System.out.println("\nUpdated knowledge base: " + updated.getName());
            System.out.println("Description: " + updated.getDescription());
            verifyTags(updatedTags, updated.getTags(), "update");
        } finally {
            searchIndexClient.deleteKnowledgeBase(knowledgeBaseName);
            searchIndexClient.deleteKnowledgeSource(knowledgeSourceName);
            System.out.println("\nDeleted sample knowledge base and knowledge source.");
        }
    }

    private static void verifyTags(Map<String, String> expected, Map<String, String> actual, String operation) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Knowledge base tags didn't persist after " + operation + ".");
        }
    }
}
