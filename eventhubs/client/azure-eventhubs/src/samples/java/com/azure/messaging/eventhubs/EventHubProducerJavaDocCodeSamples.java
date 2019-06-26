// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubProducer}.
 */
public class EventHubProducerJavaDocCodeSamples {
    private final EventHubClient client = new EventHubClientBuilder().connectionString("fake-string").build();

    /**
     * Code snippet demonstrating how to create an EventHubProducer that automatically routes events to any partition.
     */
    public void instantiate() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.instantiate
        EventHubClient client = new EventHubClientBuilder()
            .connectionString("event-hubs-namespace-connection-string", "event-hub-name")
            .build();

        EventHubProducer producer = client.createProducer();
        // END: com.azure.messaging.eventhubs.eventhubproducer.instantiate

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an EventHubProducer that routes events to a single partition.
     */
    public void instantiatePartitionProducer() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.instantiatePartitionProducer
        EventHubProducerOptions options = new EventHubProducerOptions()
            .partitionId("foo")
            .timeout(Duration.ofSeconds(45));

        EventHubProducer producer = client.createProducer(options);
        // END: com.azure.messaging.eventhubs.eventhubproducer.instantiatePartitionProducer

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void sendEventsFluxSendOptions() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.send#publisher-sendOptions
        Flux<EventData> events = Flux.just(
            new EventData("sourdough".getBytes(UTF_8)),
            new EventData("rye".getBytes(UTF_8)),
            new EventData("wheat".getBytes(UTF_8))
        );

        EventHubProducer producer = client.createProducer();
        SendOptions options = new SendOptions()
            .partitionKey("bread");

        producer.send(events, options).subscribe(ignored -> System.out.println("sent"),
            error -> System.err.println("Error received:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubproducer.send#publisher-sendOptions
    }
}
