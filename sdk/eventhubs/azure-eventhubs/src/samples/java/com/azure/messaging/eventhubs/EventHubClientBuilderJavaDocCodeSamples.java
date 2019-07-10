// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubClient}.
 */
public class EventHubClientBuilderJavaDocCodeSamples {

    /**
     * Creating an {@link EventHubClient} using an Event Hubs namespace connection string with an Event Hub name.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionString#string-string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
        String eventHubPath = "my-event-hub";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubPath)
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionString#string-string

        client.close();
    }

    /**
     * Creating an {@link EventHubClient} using a connection string specific to an Event Hub instance.
     */
    public void instantiationInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionstring#string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionstring#string

        client.close();
    }

    /**
     * Demonstrates an {@link EventHubClientBuilder} using retry, timeout and a different scheduler.
     */
    public void instantiationRetry() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.retry-timeout-scheduler
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .retry(Retry.getNoRetry())
            .timeout(Duration.ofSeconds(30))
            .scheduler(Schedulers.newElastic("dedicated-event-hub-scheduler"))
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.retry-timeout-scheduler

        client.close();
    }
}
