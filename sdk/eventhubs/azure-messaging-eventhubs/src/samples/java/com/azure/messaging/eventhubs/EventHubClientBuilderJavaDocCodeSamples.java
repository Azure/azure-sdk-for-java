// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * Code snippets for {@link EventHubClientBuilder}.
 */
public class EventHubClientBuilderJavaDocCodeSamples {
    /**
     * Code snippet for {@link EventHubClientBuilder#shareConnection()}.
     */
    public void sharingConnection() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
        // Toggling `shareConnection` instructs the builder to use the same underlying connection
        // for each consumer or producer created using the same builder instance.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString("event-hubs-instance-connection-string")
            .shareConnection();

        // Both the producer and consumer created share the same underlying connection.
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        EventHubConsumerAsyncClient consumer = builder
            .consumerGroup("my-consumer-group")
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
    }
}
