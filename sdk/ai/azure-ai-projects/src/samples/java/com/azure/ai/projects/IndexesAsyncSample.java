// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AzureAISearchIndex;
import com.azure.ai.projects.models.Index;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IndexesAsyncSample {

    private static IndexesAsyncClient indexesAsyncClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildIndexesAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
        //createOrUpdateIndex().block();
        //listIndexVersions().blockLast();
        //getIndex().block();
        //deleteIndex().block();
        listIndexes().blockLast();
    }

    public static Mono<Index> createOrUpdateIndex() {
        // BEGIN:com.azure.ai.projects.IndexesAsyncSample.createOrUpdateIndex

        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "2.0");
        String aiSearchConnectionName = Configuration.getGlobalConfiguration().get("AI_SEARCH_CONNECTION_NAME", "");
        String aiSearchIndexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME", "");

        return indexesAsyncClient.createOrUpdate(
            indexName,
            indexVersion,
            new AzureAISearchIndex()
                .setConnectionName(aiSearchConnectionName)
                .setIndexName(aiSearchIndexName)
        ).doOnNext(index -> System.out.println("Index created: " + index.getName()));

        // END:com.azure.ai.projects.IndexesAsyncSample.createOrUpdateIndex
    }

    public static Flux<Index> listIndexes() {
        // BEGIN:com.azure.ai.projects.IndexesAsyncSample.listIndexes

        return indexesAsyncClient.list()
            .doOnNext(index -> {
                System.out.println("Index name: " + index.getName());
                System.out.println("Index version: " + index.getVersion());
            });

        // END:com.azure.ai.projects.IndexesAsyncSample.listIndexes
    }
    
    public static Flux<Index> listIndexVersions() {
        // BEGIN:com.azure.ai.projects.IndexesAsyncSample.listIndexVersions
        
        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        
        return indexesAsyncClient.listVersions(indexName)
            .doOnNext(index -> {
                System.out.println("Index name: " + index.getName());
                System.out.println("Index version: " + index.getVersion());
                System.out.println("Index type: " + index.getType());
            });
        
        // END:com.azure.ai.projects.IndexesAsyncSample.listIndexVersions
    }
    
    public static Mono<Index> getIndex() {
        // BEGIN:com.azure.ai.projects.IndexesAsyncSample.getIndex
        
        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");
        
        return indexesAsyncClient.get(indexName, indexVersion)
            .doOnNext(index -> {
                System.out.println("Retrieved index:");
                System.out.println("Name: " + index.getName());
                System.out.println("Version: " + index.getVersion());
                System.out.println("Type: " + index.getType());
            });
        
        // END:com.azure.ai.projects.IndexesAsyncSample.getIndex
    }
    
    public static Mono<Void> deleteIndex() {
        // BEGIN:com.azure.ai.projects.IndexesAsyncSample.deleteIndex
        
        String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
        String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");
        
        // Delete the index version
        return indexesAsyncClient.delete(indexName, indexVersion)
            .doOnSuccess(unused -> 
                System.out.println("Deleted index: " + indexName + ", version: " + indexVersion));
        
        // END:com.azure.ai.projects.IndexesAsyncSample.deleteIndex
    }
}
