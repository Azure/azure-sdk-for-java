// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.CloudEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.samples.models.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This sample code shows how to send {@link CloudEvent}s to an Event Grid Topic that accepts cloud event schema.
 * Refer to https://docs.microsoft.com/en-us/azure/event-grid/cloud-event-schema.
 *
 * @see PublishEventGridEventsToTopic for a sample to send an Event Grid event.
 */
public class PublishCloudEventsToTopic {
    public static void main(String[] args) {
        EventGridPublisherClient publisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
            .buildClient();

        // Create a CloudEvent with String data
        String str = "FirstName: John1, LastName:James";
        CloudEvent cloudEventJson = new CloudEvent("com/example/MyApp", "User.Created.Text", str);
        // TODO: apache avro format using binary format data

        // Create a CloudEvent with Object data
        User newUser = new User("John2", "James");
        CloudEvent cloudEventModel = new CloudEvent("com/example/MyApp", "User.Created.Object", newUser);
        // Create a CloudEvent with binary data
        byte[] byteSample = "FirstName: John3, LastName: James".getBytes(StandardCharsets.UTF_8);
        CloudEvent cloudEventBytes = new CloudEvent("com/example/MyApp", "User.Created.Binary", byteSample);

        // Send them to the event grid topic altogether.
        publisherClient.sendCloudEvents(List.of(cloudEventJson, cloudEventModel, cloudEventBytes));
    }
}
