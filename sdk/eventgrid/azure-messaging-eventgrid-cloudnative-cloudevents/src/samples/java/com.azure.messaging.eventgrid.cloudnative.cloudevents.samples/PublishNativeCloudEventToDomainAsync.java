// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.cloudnative.cloudevents.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.cloudnative.cloudevents.EventGridCloudNativeEventPublisher;
import io.cloudevents.core.builder.CloudEventBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PublishNativeCloudEventToDomainAsync {
    public static void main(String[] args) {
        EventGridPublisherAsyncClient<CloudEvent> egClientAsync =
            new EventGridPublisherClientBuilder()
                .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT")) // Event Grid Domain endpoint with CloudEvent Schema
                .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
                .buildCloudEventPublisherAsyncClient();

        // When publishing to an Event Grid domain with cloud events, the cloud event source is used as the domain topic.
        // The Event Grid service doesn't support using an absolute URI for a domain topic, so you would need to do
        // something like the following to integrate with the cloud native cloud events:

        // Prepare a native cloud event input, the cloud event input should be replace with your own.
        io.cloudevents.CloudEvent cloudEvent =
            CloudEventBuilder.v1()
                .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
                .withId(UUID.randomUUID().toString()) // Replace it
                .withType("User.Created.Text") // Replace it
                .withSource(URI.create("/relative/path")) // Replace it. Event Grid does not allow absolute URIs as the domain topic
                .withDataContentType("application/json") // Replace it
                .build();

        // Prepare multiple native cloud events input
        final List<io.cloudevents.CloudEvent> cloudEvents = new ArrayList<>();
        cloudEvents.add(cloudEvent);

        // Publishing a single event
        EventGridCloudNativeEventPublisher.sendEventAsync(egClientAsync, cloudEvent);

        // Publishing multiple events
        EventGridCloudNativeEventPublisher.sendEventsAsync(egClientAsync, cloudEvents);
    }
}
