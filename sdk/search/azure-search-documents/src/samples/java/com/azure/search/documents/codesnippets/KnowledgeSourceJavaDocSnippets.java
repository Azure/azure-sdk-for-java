// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;

@SuppressWarnings("unused")
public class KnowledgeSourceJavaDocSnippets {

    private static SearchIndexClient searchIndexClient;

    /**
     * Code snippet for creating a {@link SearchIndexClient} to manage knowledge sources.
     */
    private static SearchIndexClient createSearchIndexClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.knowledgeSource.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexClient.knowledgeSource.instantiation
        return searchIndexClient;
    }

    /**
     * Code snippet for creating a knowledge source.
     */
    public static void createKnowledgeSource() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createKnowledgeSource#KnowledgeSource
        SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(
            "my-knowledge-source",
            new SearchIndexKnowledgeSourceParameters("my-search-index"));
        knowledgeSource.setDescription("Knowledge source backed by a search index");

        KnowledgeSource created = searchIndexClient.createKnowledgeSource(knowledgeSource);
        System.out.println("Created knowledge source: " + created.getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createKnowledgeSource#KnowledgeSource
    }

    /**
     * Code snippet for getting a knowledge source.
     */
    public static void getKnowledgeSource() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeSource#String
        KnowledgeSource knowledgeSource = searchIndexClient.getKnowledgeSource("my-knowledge-source");
        System.out.println("Knowledge source: " + knowledgeSource.getName());
        System.out.println("Kind: " + knowledgeSource.getKind());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeSource#String
    }

    /**
     * Code snippet for listing all knowledge sources.
     */
    public static void listKnowledgeSources() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listKnowledgeSources
        searchIndexClient.listKnowledgeSources()
            .forEach(ks -> System.out.println("Knowledge source: " + ks.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexClient.listKnowledgeSources
    }

    /**
     * Code snippet for updating a knowledge source.
     */
    public static void updateKnowledgeSource() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateKnowledgeSource#KnowledgeSource
        KnowledgeSource knowledgeSource = searchIndexClient.getKnowledgeSource("my-knowledge-source");
        knowledgeSource.setDescription("Updated description");

        KnowledgeSource updated = searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);
        System.out.println("Updated knowledge source: " + updated.getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateKnowledgeSource#KnowledgeSource
    }

    /**
     * Code snippet for getting the status of a knowledge source.
     */
    public static void getKnowledgeSourceStatus() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeSourceStatus#String
        KnowledgeSourceStatus status = searchIndexClient.getKnowledgeSourceStatus("my-knowledge-source");
        System.out.println("Synchronization status: " + status.getSynchronizationStatus());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeSourceStatus#String
    }

    /**
     * Code snippet for deleting a knowledge source.
     */
    public static void deleteKnowledgeSource() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteKnowledgeSource#String
        searchIndexClient.deleteKnowledgeSource("my-knowledge-source");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteKnowledgeSource#String
    }
}

