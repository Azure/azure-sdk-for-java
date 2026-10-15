// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;

import java.util.Collections;
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
 * </ul>
 */
public class KnowledgeBaseCrudExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();

        String resourceSuffix = UUID.randomUUID().toString().replace("-", "");
        String indexName = "sample-kb-index-" + resourceSuffix;
        String knowledgeBaseName = "sample-knowledge-base-" + resourceSuffix;
        String knowledgeSourceName = "sample-knowledge-source-" + resourceSuffix;
        boolean indexCreated = false;
        boolean knowledgeSourceCreated = false;
        boolean knowledgeBaseCreated = false;
        Throwable primaryFailure = null;

        try {
            searchIndexClient.createIndex(new SearchIndex(indexName,
                Collections.singletonList(new SearchField("id", SearchFieldDataType.STRING).setKey(true))));
            indexCreated = true;

            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(indexName));
            searchIndexClient.createKnowledgeSource(knowledgeSource);
            knowledgeSourceCreated = true;

            Map<String, String> initialTags = new LinkedHashMap<>();
            initialTags.put("environment", "sample");
            initialTags.put("owner", "search-sdk");

            // Tags are customer-defined resource metadata. They don't configure or prove billing attribution.
            KnowledgeBase knowledgeBase
                = new KnowledgeBase(knowledgeBaseName, new KnowledgeSourceReference(knowledgeSourceName))
                    .setTags(initialTags);

            KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);
            knowledgeBaseCreated = true;
            System.out.println("Created knowledge base: " + created.getName());
            verifyTags(initialTags, created.getTags(), "create");
            if (!knowledgeBaseName.equals(created.getName())) {
                throw new IllegalStateException("The created knowledge base name didn't match the request.");
            }

            // Get a knowledge base by name
            KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBaseName);
            System.out.println("Retrieved knowledge base: " + retrieved.getName());
            System.out.println("ETag: " + retrieved.getETag());
            System.out.println("Knowledge sources: " + retrieved.getKnowledgeSources().size());
            verifyTags(initialTags, retrieved.getTags(), "get");
            if (retrieved.getETag() == null || retrieved.getKnowledgeSources().size() != 1) {
                throw new IllegalStateException("The retrieved knowledge base was missing its ETag or source.");
            }

            // List all knowledge bases
            boolean listed = searchIndexClient.listKnowledgeBases()
                .stream()
                .anyMatch(kb -> knowledgeBaseName.equals(kb.getName()));
            if (!listed) {
                throw new IllegalStateException("The created knowledge base wasn't returned by listKnowledgeBases.");
            }

            // Update a knowledge base
            Map<String, String> updatedTags = new LinkedHashMap<>(initialTags);
            updatedTags.put("release", "2026-08-01-preview");
            retrieved.setDescription("Updated description for sample knowledge base");
            retrieved.setTags(updatedTags);
            KnowledgeBase updated = searchIndexClient.createOrUpdateKnowledgeBase(retrieved);
            System.out.println("\nUpdated knowledge base: " + updated.getName());
            System.out.println("Description: " + updated.getDescription());
            verifyTags(updatedTags, updated.getTags(), "update");
            if (!retrieved.getDescription().equals(updated.getDescription())) {
                throw new IllegalStateException("The updated description wasn't persisted.");
            }
        } catch (RuntimeException | Error exception) {
            primaryFailure = exception;
            throw exception;
        } finally {
            RuntimeException cleanupFailure = null;
            if (knowledgeBaseCreated) {
                cleanupFailure
                    = cleanup(() -> searchIndexClient.deleteKnowledgeBase(knowledgeBaseName), cleanupFailure);
            }
            if (knowledgeSourceCreated) {
                cleanupFailure
                    = cleanup(() -> searchIndexClient.deleteKnowledgeSource(knowledgeSourceName), cleanupFailure);
            }
            if (indexCreated) {
                cleanupFailure = cleanup(() -> searchIndexClient.deleteIndex(indexName), cleanupFailure);
            }

            if (cleanupFailure != null) {
                if (primaryFailure != null) {
                    primaryFailure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            } else {
                System.out.println("\nDeleted sample knowledge base, knowledge source, and index.");
            }
        }
    }

    private static RuntimeException cleanup(Runnable cleanup, RuntimeException previousFailure) {
        try {
            cleanup.run();
        } catch (RuntimeException exception) {
            if (previousFailure == null) {
                return exception;
            }
            previousFailure.addSuppressed(exception);
        }
        return previousFailure;
    }

    private static void verifyTags(Map<String, String> expected, Map<String, String> actual, String operation) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Knowledge base tags didn't persist after " + operation + ".");
        }
    }
}
