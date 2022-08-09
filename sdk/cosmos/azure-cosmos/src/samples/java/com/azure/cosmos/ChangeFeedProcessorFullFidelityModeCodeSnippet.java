// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedProcessorItem;

/**
 * Code snippets for FullFidelityChangeFeedProcessor
 */
public class ChangeFeedProcessorFullFidelityModeCodeSnippet {

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
        // BEGIN: com.azure.cosmos.fullFidelityChangeFeedProcessor.builder
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .changeFeedMode(ChangeFeedMode.FULL_FIDELITY)
            .handleAllChanges(docs -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
            })
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.fullFidelityChangeFeedProcessor.builder
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
            // BEGIN: com.azure.cosmos.fullFidelityChangeFeedProcessor.handleChanges
            .changeFeedMode(ChangeFeedMode.FULL_FIDELITY)
            .handleAllChanges(docs -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
            })
            // END: com.azure.cosmos.fullFidelityChangeFeedProcessor.handleChanges
            .buildChangeFeedProcessor();
    }
}

