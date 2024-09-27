// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.eventhubs;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;

import java.util.Arrays;

/**
 * An Event Hubs sample to demonstrate sending events to Event Hubs using GraalVM.
 */
public final class EventHubsSample {
    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING
        = Configuration.getGlobalConfiguration().get("AZURE_EVENT_HUBS_CONNECTION_STRING", "");
    private static final String AZURE_EVENT_HUBS_NAMESPACE
        = Configuration.getGlobalConfiguration().get("AZURE_EVENT_HUBS_NAMESPACE", "");
    private static final String AZURE_EVENT_HUBS_NAME
        = Configuration.getGlobalConfiguration().get("AZURE_EVENT_HUBS_NAME", "");

    /**
     * The method to run Event Hubs sample.
     */
    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting Event Hubs Sender Sample");
        System.out.println("================================================================");

        if (AZURE_EVENT_HUBS_CONNECTION_STRING.isEmpty()
            || (AZURE_EVENT_HUBS_NAMESPACE.isEmpty() && AZURE_EVENT_HUBS_NAME.isEmpty())) {
            System.err.println("AZURE_EVENT_HUBS_CONNECTION_STRING environment variable should be set or "
                + "AZURE_EVENT_HUBS_NAMESPACE and AZURE_EVENT_HUBS_NAME environment variables should be set to "
                + "run this sample.");
            return;

        }
        EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder();

        if (AZURE_EVENT_HUBS_CONNECTION_STRING.isEmpty()) {
            eventHubClientBuilder.credential(AZURE_EVENT_HUBS_NAMESPACE, AZURE_EVENT_HUBS_NAME,
                new DefaultAzureCredentialBuilder().build());
        } else {
            eventHubClientBuilder.connectionString(AZURE_EVENT_HUBS_CONNECTION_STRING);
        }

        final EventHubProducerClient eventHubProducerClient = eventHubClientBuilder.buildProducerClient();

        System.out.println("Event Hub producer client created");
        eventHubProducerClient.send(Arrays.asList(new EventData("Test event - graalvm")));
        System.out.println("Sent message to Event Hub");

        eventHubProducerClient.close();

        System.out.println("\n================================================================");
        System.out.println(" Event Hubs Sender Sample Complete");
        System.out.println("================================================================");
    }

    private EventHubsSample() {
    }
}
