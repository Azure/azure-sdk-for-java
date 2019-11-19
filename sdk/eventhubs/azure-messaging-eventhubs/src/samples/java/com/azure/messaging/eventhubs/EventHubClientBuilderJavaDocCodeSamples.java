// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

public class EventHubClientBuilderJavaDocCodeSamples {
    public void sharingConnection() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
        // Toggling `shareConnection` instructs the builder to use the same underlying connection
        // for each consumer or producer created using the same builder instance.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString("event-hubs-instance-connection-string")
            .shareConnection();

        // Both the producer and consumer created share the same underlying connection.
        EventHubProducerAsyncClient producer = builder.buildAsyncProducer();

        EventHubConsumerAsyncClient consumer = builder
            .consumerGroup("my-consumer-group")
            .buildAsyncConsumer();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
    }
}
