// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AzureAISearchIndex;
import com.azure.ai.projects.models.Index;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class IndexesSample {

    private static IndexesClient indexesClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildIndexesClient();

    public static void main(String[] args) {
        // Uncomment the sample you want to run
        //createOrUpdateIndex();
        //listIndexVersions();
        //getIndex();
        //deleteIndex();
        //listIndexes();
    }

    public static void createOrUpdateIndex() {
        // BEGIN:com.azure.ai.projects.IndexesGetSample.createOrUpdateIndex

        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "2.0");
        String aiSearchConnectionName = Configuration.getGlobalConfiguration().get("AI_SEARCH_CONNECTION_NAME", "");
        String aiSearchIndexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME", "");

        Index index = indexesClient.createOrUpdate(
            indexName,
            indexVersion,
            new AzureAISearchIndex()
                .setConnectionName(aiSearchConnectionName)
                .setIndexName(aiSearchIndexName)
        );

        System.out.println("Index created: " + index.getName());

        // END:com.azure.ai.projects.IndexesGetSample.createOrUpdateIndex
    }

    public static void listIndexes() {
        // BEGIN:com.azure.ai.projects.IndexesListSample.listIndexes

        indexesClient.list().forEach(index -> {
            System.out.println("Index name: " + index.getName());
            System.out.println("Index version: " + index.getVersion());
        });

        // END:com.azure.ai.projects.IndexesListSample.listIndexes
    }
    
    public static void listIndexVersions() {
        // BEGIN:com.azure.ai.projects.IndexesListVersionsSample.listIndexVersions
        
        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        
        indexesClient.listVersions(indexName).forEach(index -> {
            System.out.println("Index name: " + index.getName());
            System.out.println("Index version: " + index.getVersion());
            System.out.println("Index type: " + index.getType());
        });
        
        // END:com.azure.ai.projects.IndexesListVersionsSample.listIndexVersions
    }
    
    public static void getIndex() {
        // BEGIN:com.azure.ai.projects.IndexesGetSample.getIndex
        
        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");
        
        Index index = indexesClient.get(indexName, indexVersion);
        
        System.out.println("Retrieved index:");
        System.out.println("Name: " + index.getName());
        System.out.println("Version: " + index.getVersion());
        System.out.println("Type: " + index.getType());
        
        // END:com.azure.ai.projects.IndexesGetSample.getIndex
    }
    
    public static void deleteIndex() {
        // BEGIN:com.azure.ai.projects.IndexesDeleteSample.deleteIndex
        
        String indexName = "test-index"; //Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");
        
        // Delete the index version
        indexesClient.delete(indexName, indexVersion);
        
        System.out.println("Deleted index: " + indexName + ", version: " + indexVersion);
        
        // END:com.azure.ai.projects.IndexesDeleteSample.deleteIndex
    }
}
