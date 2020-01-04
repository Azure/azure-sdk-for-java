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
        ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .setHostName(hostName)
            .setFeedContainer(feedContainer)
            .setLeaseContainer(leaseContainer)
            .setHandleChanges(docs -> {
                // Implementation for handling and processing CosmosItemProperties list goes here
            })
            .build();
        // END: com.azure.cosmos.changeFeedProcessor.builder
    }
}

