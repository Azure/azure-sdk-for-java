// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Code snippets for {@link ChangeFeedProcessor}
 */
public class ChangeFeedProcessorCodeSnippet {

    public void changeFeedProcessorBuilderCodeSnippet() {
        String hostName = "test-host-name";
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase = cosmosAsyncClient.getDatabase("testDb");
        CosmosAsyncContainer feedContainer = cosmosAsyncDatabase.getContainer("feedContainer");
        CosmosAsyncContainer leaseContainer = cosmosAsyncDatabase.getContainer("leaseContainer");
        // BEGIN: com.azure.cosmos.changeFeedProcessor.builder
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleChanges(docs -> {
                for (JsonNode item : docs) {
                    // Implementation for handling and processing of each JsonNode item goes here
                }
            })
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.changeFeedProcessor.builder
    }

    public void handleChangesCodeSnippet() {
        String hostName = "test-host-name";
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase = cosmosAsyncClient.getDatabase("testDb");
        CosmosAsyncContainer feedContainer = cosmosAsyncDatabase.getContainer("feedContainer");
        CosmosAsyncContainer leaseContainer = cosmosAsyncDatabase.getContainer("leaseContainer");
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            // BEGIN: com.azure.cosmos.changeFeedProcessor.handleChanges
            .handleChanges(docs -> {
                for (JsonNode item : docs) {
                    // Implementation for handling and processing of each JsonNode item goes here
                }
            })
            // END: com.azure.cosmos.changeFeedProcessor.handleChanges
            .buildChangeFeedProcessor();
    }
}

