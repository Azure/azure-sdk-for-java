// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.CosmosChangeFeedPreviousImageRetentionMode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Code snippets for AllVersionsAndDeletesChangeFeedProcessor
 */
public class ChangeFeedProcessorAllVersionsAndDeletesModeCodeSnippet {

    public Mono<CosmosContainerResponse> createContainerWithPreviousImages(CosmosAsyncDatabase database) {
        // BEGIN: readme-sample-createContainerWithPreviousImages
        CosmosContainerProperties properties = new CosmosContainerProperties("feedContainer", "/pk")
            .setChangeFeedPolicy(ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofMinutes(10)))
            .setChangeFeedPreviousImageRetentionMode(
                CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS);
        Mono<CosmosContainerResponse> createResponse = database.createContainer(properties);
        // END: readme-sample-createContainerWithPreviousImages
        return createResponse;
    }

    public Mono<CosmosContainerResponse> updatePreviousImageRetention(CosmosAsyncContainer feedContainer) {
        // BEGIN: readme-sample-updatePreviousImageRetention
        Mono<CosmosContainerResponse> replaceResponse = feedContainer.read()
            .flatMap(response -> {
                CosmosContainerProperties properties = response.getProperties();
                properties.setChangeFeedPreviousImageRetentionMode(
                    CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_REPLACE_OPERATIONS);
                return feedContainer.replace(properties);
            });
        // END: readme-sample-updatePreviousImageRetention
        return replaceResponse;
    }

    public ChangeFeedProcessor processPreviousImages(CosmosAsyncContainer feedContainer, CosmosAsyncContainer leaseContainer) {
        // BEGIN: readme-sample-processPreviousImages
        ChangeFeedProcessor processor = new ChangeFeedProcessorBuilder()
            .hostName("previous-images-host")
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleAllVersionsAndDeletesChanges(items -> {
                for (ChangeFeedProcessorItem item : items) {
                    if (item.getPrevious() != null) {
                        // Process the captured previous item alongside the operation metadata.
                        System.out.println(item.getChangeFeedMetaData().getOperationType() + ": " + item.getPrevious());
                    }
                }
            })
            .buildChangeFeedProcessor();
        // END: readme-sample-processPreviousImages
        return processor;
    }

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
        // BEGIN: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.builder
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleAllVersionsAndDeletesChanges(docs -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
            })
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.builder
    }

    public void handleAllVersionsAndDeletesChangesCodeSnippet() {
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
            // BEGIN: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.handleChanges
            .handleAllVersionsAndDeletesChanges(docs -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
            })
            // END: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessor.handleChanges
            .buildChangeFeedProcessor();
    }

    public void changeFeedProcessorBuilderWithContextCodeSnippet() {
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
        // BEGIN: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessorWithContext.builder
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleAllVersionsAndDeletesChanges((docs, context) -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
                String leaseToken = context.getLeaseToken();
                // Handling of the lease token corresponding to a batch of change feed processor item goes here
            })
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessorWithContext.builder
    }

    public void handleAllVersionsAndDeletesChangesWithContextCodeSnippet() {
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
            // BEGIN: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessorWithContext.handleChanges
            .handleAllVersionsAndDeletesChanges((docs, context) -> {
                for (ChangeFeedProcessorItem item : docs) {
                    // Implementation for handling and processing of each ChangeFeedProcessorItem item goes here
                }
                String leaseToken = context.getLeaseToken();
                // Handling of the lease token corresponding to a batch of change feed processor item goes here
            })
            // END: com.azure.cosmos.allVersionsAndDeletesChangeFeedProcessorWithContext.handleChanges
            .buildChangeFeedProcessor();
    }
}
