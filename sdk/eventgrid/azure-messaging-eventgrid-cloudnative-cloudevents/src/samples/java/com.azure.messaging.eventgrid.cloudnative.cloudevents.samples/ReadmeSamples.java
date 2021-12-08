// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.cloudnative.cloudevents.EventGridCloudNativeEventPublisher;
import io.cloudevents.core.builder.CloudEventBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {

    public void sendEventGridEventsToTopic() {
        // BEGIN: readme-sample-sendCNCFCloudEvents
        // Prepare Event Grid client
        EventGridPublisherClient<CloudEvent> egClient =
            new EventGridPublisherClientBuilder()
                .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
                .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
                .buildCloudEventPublisherClient();

        // Prepare a native cloud event input, the cloud event input should be replace with your own.
        io.cloudevents.CloudEvent cloudEvent =
            CloudEventBuilder.v1()
                .withData("{\"name\": \"joe\"}".getBytes(StandardCharsets.UTF_8)) // Replace it
                .withId(UUID.randomUUID().toString()) // Replace it
                .withType("User.Created.Text") // Replace it
                .withSource(URI.create("http://localHost")) // Replace it
                .withDataContentType("application/json") // Replace it
                .build();

        // Publishing a single event
        EventGridCloudNativeEventPublisher.sendEvent(egClient, cloudEvent);
        // END: readme-sample-sendCNCFCloudEvents
    }
}
