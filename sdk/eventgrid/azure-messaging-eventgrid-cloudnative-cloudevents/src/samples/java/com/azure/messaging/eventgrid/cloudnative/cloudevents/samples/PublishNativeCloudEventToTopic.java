// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.cloudnative.cloudevents.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.cloudnative.cloudevents.EventGridCloudNativeEventPublisher;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This sample shows how to publish a native cloud event to Azure EventGrid.
 */
public class PublishNativeCloudEventToTopic {
    public static void main(String[] args) {
        // Prepare Event Grid client
        EventGridPublisherClient<com.azure.core.models.CloudEvent> egClient =
            new EventGridPublisherClientBuilder()
                .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT")) // Event Grid topic endpoint with CloudEvent Schema
                .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
                .buildCloudEventPublisherClient();

        // Prepare a native cloud event input, the cloud event input should be replace with your own.
        CloudEvent cloudEvent =
            CloudEventBuilder.v1()
                .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
                .withId(UUID.randomUUID().toString()) // Replace it
                .withType("User.Created.Text") // Replace it
                .withSource(URI.create("http://localHost")) // Replace it
                .withDataContentType("application/json") // Replace it
                .build();

        // Prepare multiple native cloud events input
        final List<CloudEvent> cloudEvents = new ArrayList<>();
        cloudEvents.add(cloudEvent);

        // Publishing a single event
        EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);

        // Publishing multiple events
        EventGridCloudNativeEventPublisher.sendEvents(egClient, cloudEvents);
    }
}
