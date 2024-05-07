// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@SuppressWarnings("unused")
public class SearchIndexerAsyncClientJavaDocSnippets {

    private static SearchIndexerAsyncClient searchIndexerAsyncClient;

    /**
     * Code snippet for creating a {@link SearchIndexerAsyncClient}
     */
    private static SearchIndexerAsyncClient createSearchIndexerAsyncClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.instantiation
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{admin-key}"))
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.instantiation
        return searchIndexerAsyncClient;
    }

    /**
     * Code snippet for creating an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void createIndexer() {


        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.createIndexer#SearchIndexer
        SearchIndexer indexer = new SearchIndexer("example-indexer", "example-datasource", "example-index");
        SearchIndexer createdIndexer = searchIndexerAsyncClient.createIndexer(indexer).block();
        if (createdIndexer != null) {
            System.out.printf("Created indexer name: %s%n", createdIndexer.getName());
        }
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.createIndexer#SearchIndexer

    }

    /**
     * Code snippet for listing all indexers using {@link SearchIndexerAsyncClient}
     */
    public static void listIndexers() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.listIndexers
        searchIndexerAsyncClient.listIndexers().subscribe(indexer ->
            System.out.printf("Retrieved indexer name: %s%n", indexer.getName())
        );
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.listIndexers
    }

    /**
     * Code snippet for getting an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void getIndexer() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.getIndexer#String
        SearchIndexer indexer = searchIndexerAsyncClient.getIndexer("example-indexer").block();
        if (indexer != null) {
            System.out.printf("Retrieved indexer name: %s%n", indexer.getName());
        }
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.getIndexer#String
    }

    /**
     * Code snippet for updating an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void updateIndexer() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.updateIndexer#SearchIndexer
        SearchIndexer indexer = searchIndexerAsyncClient.getIndexer("example-indexer").block();
        if (indexer != null) {
            System.out.printf("Retrieved indexer name: %s%n", indexer.getName());
            indexer.setDescription("This is a new description for this indexer");
            SearchIndexer updatedIndexer = searchIndexerAsyncClient.createOrUpdateIndexer(indexer).block();

            if (updatedIndexer != null) {
                System.out.printf("Updated indexer name: %s, description: %s%n", updatedIndexer.getName(),
                    updatedIndexer.getDescription());
            }
        }

        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.updateIndexer#SearchIndexer
    }

    /**
     * Code snippet for deleting an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void deleteIndexer() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.deleteIndexer#String
        searchIndexerAsyncClient.deleteIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.deleteIndexer#String
    }

    /**
     * Code snippet for running an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void runIndexer() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.runIndexer#String
        searchIndexerAsyncClient.runIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.runIndexer#String
    }

    /**
     * Code snippet for resetting an indexer using {@link SearchIndexerAsyncClient}
     */
    public static void resetIndexer() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.resetIndexer#String
        searchIndexerAsyncClient.resetIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.resetIndexer#String
    }

    /**
     * Code snippet for creating a skillset using {@link SearchIndexerAsyncClient}
     */
    public static void createSkillset() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.createSkillset#SearchIndexerSkillset
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image")
                .setSource("/document/normalized_images/*")
        );

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text")
                .setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText")
                .setTargetName("myLayoutText")
        );

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")
        );

        SearchIndexerSkillset skillset = new SearchIndexerSkillset("skillsetName", skills)
            .setDescription("Extracts text (plain and structured) from image.");

        System.out.println(String.format("Creating OCR skillset '%s'", skillset.getName()));

        SearchIndexerSkillset createdSkillset = searchIndexerAsyncClient.createSkillset(skillset).block();

        if (createdSkillset != null) {
            System.out.println("Created OCR skillset");
            System.out.println(String.format("Name: %s", createdSkillset.getName()));
            System.out.println(String.format("ETag: %s", createdSkillset.getETag()));
        }
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for listing all skillsets using {@link SearchIndexerAsyncClient}
     */
    public static void listSkillsets() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.listSkillsets
        searchIndexerAsyncClient.listSkillsets().subscribe(skillset ->
            System.out.printf("Retrieved skillset name: %s%n", skillset.getName())
        );
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.listSkillsets
    }

    /**
     * Code snippet for getting a skillset using {@link SearchIndexerAsyncClient}
     */
    public static void getSkillset() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.getSkillset#String
        SearchIndexerSkillset skillset = searchIndexerAsyncClient.getSkillset("example-skillset").block();
        if (skillset != null) {
            System.out.printf("Retrieved skillset name: %s%n", skillset.getName());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.getSkillset#String
    }

    /**
     * Code snippet for updating a skillset using {@link SearchIndexerAsyncClient}
     */
    public static void updateSkillset() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.updateSkillset#SearchIndexerSkillset
        SearchIndexerSkillset skillset = searchIndexerAsyncClient.getSkillset("example-skillset").block();
        if (skillset != null) {
            System.out.printf("Retrieved skillset name: %s%n", skillset.getName());
            SearchIndexerSkillset updatedSkillset = searchIndexerAsyncClient.createOrUpdateSkillset(skillset).block();

            if (updatedSkillset != null) {
                System.out.printf("Updated skillset name: %s, description: %s%n", updatedSkillset.getName(),
                    updatedSkillset.getDescription());
            }
        }
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient-classLevelJavaDoc.updateSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for deleting a skillset using {@link SearchIndexerAsyncClient}
     */
    public static void deleteSkillset() {
        searchIndexerAsyncClient = createSearchIndexerAsyncClient();
        // BEGIN: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.deleteSkillset#String
        searchIndexerAsyncClient.deleteSkillset("example-skillset");
        // END: com.azure.search.documents.SearchIndexerAsyncClient-classLevelJavaDoc.deleteSkillset#String
    }

}
