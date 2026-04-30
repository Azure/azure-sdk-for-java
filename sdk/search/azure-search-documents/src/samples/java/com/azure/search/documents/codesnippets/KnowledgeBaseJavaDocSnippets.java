// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;

@SuppressWarnings("unused")
public class KnowledgeBaseJavaDocSnippets {

    private static SearchIndexClient searchIndexClient;

    /**
     * Code snippet for creating a {@link SearchIndexClient} to manage knowledge bases.
     */
    private static SearchIndexClient createSearchIndexClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.knowledgeBase.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexClient.knowledgeBase.instantiation
        return searchIndexClient;
    }

    /**
     * Code snippet for creating a knowledge base.
     */
    public static void createKnowledgeBase() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createKnowledgeBase#KnowledgeBase
        KnowledgeBase knowledgeBase = new KnowledgeBase("my-knowledge-base",
            new KnowledgeSourceReference("my-knowledge-source"));

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);
        System.out.println("Created knowledge base: " + created.getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createKnowledgeBase#KnowledgeBase
    }

    /**
     * Code snippet for getting a knowledge base.
     */
    public static void getKnowledgeBase() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeBase#String
        KnowledgeBase knowledgeBase = searchIndexClient.getKnowledgeBase("my-knowledge-base");
        System.out.println("Knowledge base: " + knowledgeBase.getName());
        System.out.println("ETag: " + knowledgeBase.getETag());
        System.out.println("Knowledge sources: " + knowledgeBase.getKnowledgeSources().size());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getKnowledgeBase#String
    }

    /**
     * Code snippet for listing all knowledge bases.
     */
    public static void listKnowledgeBases() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listKnowledgeBases
        searchIndexClient.listKnowledgeBases()
            .forEach(kb -> System.out.println("Knowledge base: " + kb.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexClient.listKnowledgeBases
    }

    /**
     * Code snippet for updating a knowledge base.
     */
    public static void updateKnowledgeBase() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateKnowledgeBase#KnowledgeBase
        KnowledgeBase knowledgeBase = searchIndexClient.getKnowledgeBase("my-knowledge-base");
        knowledgeBase.setDescription("Updated description for my knowledge base");

        KnowledgeBase updated = searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);
        System.out.println("Updated knowledge base: " + updated.getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateKnowledgeBase#KnowledgeBase
    }

    /**
     * Code snippet for deleting a knowledge base.
     */
    public static void deleteKnowledgeBase() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteKnowledgeBase#String
        searchIndexClient.deleteKnowledgeBase("my-knowledge-base");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteKnowledgeBase#String
    }
}

