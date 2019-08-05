// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubAsyncClient}.
 */
public class EventHubClientBuilderJavaDocCodeSamples {

    /**
     * Creating an {@link EventHubAsyncClient} using an Event Hubs namespace connection string with an Event Hub name.
     */
    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionString#string-string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        String eventHubName = "my-event-hub";

        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionString#string-string

        client.close();
    }

    /**
     * Creating an {@link EventHubAsyncClient} using a connection string specific to an Event Hub instance.
     */
    public void instantiationInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.connectionstring#string
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventHubAsyncClient client = new EventHubClientBuilder()
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
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        RetryOptions retryOptions = new RetryOptions()
            .tryTimeout(Duration.ofSeconds(30));
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .retry(retryOptions)
            .scheduler(Schedulers.newElastic("dedicated-event-hub-scheduler"))
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.retry-timeout-scheduler

        client.close();
    }
}
