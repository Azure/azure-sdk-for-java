// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexerClient;
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
public class SearchIndexerClientJavaDocSnippets {


    private static SearchIndexerClient searchIndexerClient;

    /**
     * Code snippet for creating a {@link SearchIndexerClient}
     */
    private static SearchIndexerClient createSearchIndexerClient() {
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.instantiation
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{admin-key}"))
            .buildClient();
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.instantiation
        return searchIndexerClient;
    }

    /**
     * Code snippet for creating an indexer using {@link SearchIndexerClient}
     */
    public static void createIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.createIndexer#SearchIndexer
        SearchIndexer indexer = new SearchIndexer("example-indexer", "example-datasource", "example-index");
        SearchIndexer createdIndexer = searchIndexerClient.createIndexer(indexer);
        System.out.printf("Created indexer name: %s%n", createdIndexer.getName());
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.createIndexer#SearchIndexer
    }

    /**
     * Code snippet for listing all indexers using {@link SearchIndexerClient}
     */
    public static void listIndexers() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.listIndexers
        searchIndexerClient.listIndexers().forEach(indexer ->
            System.out.printf("Retrieved indexer name: %s%n", indexer.getName())
        );
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.listIndexers
    }

    /**
     * Code snippet for getting an indexer using {@link SearchIndexerClient}
     */
    public static void getIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.getIndexer#String
        SearchIndexer indexer = searchIndexerClient.getIndexer("example-indexer");
        System.out.printf("Retrieved indexer name: %s%n", indexer.getName());
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.getIndexer#String
    }

    /**
     * Code snippet for updating an indexer using {@link SearchIndexerClient}
     */
    public static void updateIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.updateIndexer#SearchIndexer
        SearchIndexer indexer = searchIndexerClient.getIndexer("example-indexer");
        indexer.setDescription("This is a new description for this indexer");
        SearchIndexer updatedIndexer = searchIndexerClient.createOrUpdateIndexer(indexer);
        System.out.printf("Updated indexer name: %s, description: %s%n", updatedIndexer.getName(),
            updatedIndexer.getDescription());
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.updateIndexer#SearchIndexer
    }

    /**
     * Code snippet for deleting an indexer using {@link SearchIndexerClient}
     */
    public static void deleteIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.deleteIndexer#String
        searchIndexerClient.deleteIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.deleteIndexer#String
    }

    /**
     * Code snippet for running an indexer using {@link SearchIndexerClient}
     */
    public static void runIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.runIndexer#String
        searchIndexerClient.runIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.runIndexer#String
    }

    /**
     * Code snippet for resetting an indexer using {@link SearchIndexerClient}
     */
    public static void resetIndexer() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.resetIndexer#String
        searchIndexerClient.resetIndexer("example-indexer");
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.resetIndexer#String
    }

    /**
     * Code snippet for creating a skillset using {@link SearchIndexerClient}
     */
    public static void createSkillset() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.createSkillset#SearchIndexerSkillset

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

        SearchIndexerSkillset createdSkillset = searchIndexerClient.createSkillset(skillset);

        System.out.println("Created OCR skillset");
        System.out.println(String.format("Name: %s", createdSkillset.getName()));
        System.out.println(String.format("ETag: %s", createdSkillset.getETag()));

        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for listing all skillsets using {@link SearchIndexerClient}
     */
    public static void listSkillsets() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.listSkillsets
        searchIndexerClient.listSkillsets().forEach(skillset ->
            System.out.printf("Retrieved skillset name: %s%n", skillset.getName())
        );
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.listSkillsets
    }

    /**
     * Code snippet for getting a skillset using {@link SearchIndexerClient}
     */
    public static void getSkillset() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.getSkillset#String
        SearchIndexerSkillset skillset = searchIndexerClient.getSkillset("example-skillset");
        System.out.printf("Retrieved skillset name: %s%n", skillset.getName());
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.getSkillset#String
    }

    /**
     * Code snippet for updating a skillset using {@link SearchIndexerClient}
     */
    public static void updateSkillset() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.updateSkillset#SearchIndexerSkillset
        SearchIndexerSkillset skillset = searchIndexerClient.getSkillset("example-skillset");
        skillset.setDescription("This is a new description for this skillset");
        SearchIndexerSkillset updatedSkillset = searchIndexerClient.createOrUpdateSkillset(skillset);
        System.out.printf("Updated skillset name: %s, description: %s%n", updatedSkillset.getName(),
            updatedSkillset.getDescription());
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.updateSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for deleting a skillset using {@link SearchIndexerClient}
     */
    public static void deleteSkillset() {
        searchIndexerClient = createSearchIndexerClient();
        // BEGIN: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.deleteSkillset#String
        searchIndexerClient.deleteSkillset("example-skillset");
        // END: com.azure.search.documents.SearchIndexerClient-classLevelJavaDoc.deleteSkillset#String
    }


}
