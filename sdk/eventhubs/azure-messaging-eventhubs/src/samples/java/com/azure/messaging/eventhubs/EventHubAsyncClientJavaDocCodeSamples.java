// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubConnection}.
 */
public class EventHubAsyncClientJavaDocCodeSamples {

    /**
     * Creating an {@link EventHubConnection} using an Event Hubs namespace connection string with an Event Hub name.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string-string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey}";
        String eventHubName = "my-event-hub";

        EventHubConnection client = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .buildConnection();
        // END: com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string-string

        client.close();
    }

    /**
     * Creating an {@link EventHubConnection} using a connection string specific to an Event Hub instance.
     */
    public void instantiationInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventHubConnection client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildConnection();
        // END: com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string

        client.close();
    }
}
