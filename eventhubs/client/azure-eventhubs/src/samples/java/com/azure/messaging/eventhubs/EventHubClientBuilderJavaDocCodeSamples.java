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

    public void instantiation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
        String eventHubPath = "my-event-hub";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubPath)
            .build();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
    }

    public void instantiationInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiationEventHub
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiationEventHub
    }

    public void instantiationRetry() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiationOptions
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .retry(Retry.getNoRetry())
            .timeout(Duration.ofSeconds(30))
            .scheduler(Schedulers.newElastic("dedicated-event-hub-scheduler"))
            .build();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiationOptions
    }
}
