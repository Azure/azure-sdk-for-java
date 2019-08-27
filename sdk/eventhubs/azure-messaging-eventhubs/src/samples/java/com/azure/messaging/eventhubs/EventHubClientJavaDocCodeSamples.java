// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubClient}.
 */
public class EventHubClientJavaDocCodeSamples {
    /**
     * Creating an {@link EventHubClient} using a connection string specific to an Event Hub instance with different
     * retry options.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclient.instantiation
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.messaging.eventhubs.eventhubclient.instantiation

        client.close();
    }
}
