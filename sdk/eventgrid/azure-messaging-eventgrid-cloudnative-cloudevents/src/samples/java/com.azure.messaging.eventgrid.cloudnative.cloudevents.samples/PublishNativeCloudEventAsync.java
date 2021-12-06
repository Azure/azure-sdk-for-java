// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

/**
 * This sample shows how to publish a native cloud event to Azure EventGrid with the EventGrid client asynchronously.
 */
public class PublishNativeCloudEventAsync {
    public static void main(String[] args) {
        // Prepare Event Grid async client
        EventGridPublisherAsyncClient<CloudEvent> egClientAsync =
            new EventGridPublisherClientBuilder()
                .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
                .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
                .buildCloudEventPublisherAsyncClient();

        // Prepare a native cloud event input, the cloud event input should be replace with your own.
        io.cloudevents.CloudEvent cloudEvent =
            CloudEventBuilder.v1()
                .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
                .withId(UUID.randomUUID().toString()) // Replace it
                .withType("User.Created.Text") // Replace it
                .withSource(URI.create("http://localHost")) // Replace it
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

