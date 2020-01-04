// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;


import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstrates how to publish events when using web sockets and a proxy and synchronous producer.
 */
public class PublishEventsWithWebSocketsAndProxy {
    /**
     * Main method to invoke this sample.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        List<EventData> telemetryEvents = Arrays.asList(
            new EventData("Roast beef".getBytes(UTF_8)),
            new EventData("Cheese".getBytes(UTF_8)),
            new EventData("Tofu".getBytes(UTF_8)),
            new EventData("Turkey".getBytes(UTF_8)));

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // By default, the AMQP port 5671 is used, but clients can use web sockets, port 443.
        // When using web sockets, developers can specify proxy options.
        // ProxyOptions.SYSTEM_DEFAULTS can be used if developers want to use the JVM configured proxy.
        ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.DIGEST,
            new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("10.13.1.3", 9992)),
            "digest-user", "digest-user-password");

        // Instantiate a client that will be used to call the service.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .proxyOptions(proxyOptions)
            .connectionString(connectionString)
            .buildProducerClient();

        // Creates an EventDataBatch where the Event Hubs service will automatically load balance the events between all
        // available partitions.
        EventDataBatch currentBatch = producer.createBatch();

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        for (EventData event : telemetryEvents) {
            if (currentBatch.tryAdd(event)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            producer.send(currentBatch);
            currentBatch = producer.createBatch();

            // Add that event that we couldn't before.
            if (!currentBatch.tryAdd(event)) {
                System.err.printf("Event is too large for an empty batch. Skipping. Max size: %s. Event: %s%n",
                    currentBatch.getMaxSizeInBytes(), event.getBodyAsString());
            }
        }

        producer.close();
    }
}
