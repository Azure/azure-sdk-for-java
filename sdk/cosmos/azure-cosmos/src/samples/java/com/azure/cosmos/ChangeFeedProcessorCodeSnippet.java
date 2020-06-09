// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.fasterxml.jackson.databind.JsonNode;

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
            // BEGIN: com.azure.cosmos.changeFeedProcessor.handleChanges
            .handleChanges(docs -> {
                for (JsonNode item : docs) {
                    // Implementation for handling and processing of each JsonNode item goes here
                }
            })
            // END: com.azure.cosmos.changeFeedProcessor.handleChanges
            .buildChangeFeedProcessor();
        // END: com.azure.cosmos.changeFeedProcessor.builder
    }
}

