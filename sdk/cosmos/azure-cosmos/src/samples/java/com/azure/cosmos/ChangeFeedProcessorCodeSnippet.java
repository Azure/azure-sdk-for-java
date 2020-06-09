// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

/**
 * Code snippets for {@link ChangeFeedProcessor}
 */
public class ChangeFeedProcessorCodeSnippet {

    public void changeFeedProcessorBuilderCodeSnippet() {
        String hostName = null;
        CosmosAsyncContainer feedContainer = null;
        CosmosAsyncContainer leaseContainer = null;
        // BEGIN: com.azure.cosmos.changeFeedProcessor.builder
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleChanges(docs -> {
                // Implementation for handling and processing CosmosItemProperties list goes here
            })
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.changeFeedProcessor.builder
    }
}

