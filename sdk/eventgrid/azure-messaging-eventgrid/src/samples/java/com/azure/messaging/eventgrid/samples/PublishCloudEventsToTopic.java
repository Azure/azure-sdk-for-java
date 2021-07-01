// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.samples.models.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample code shows how to send {@link CloudEvent CloudEvents} to an Event Grid Topic that accepts cloud event schema.
 * Refer to the <a href="https://docs.microsoft.com/en-us/azure/event-grid/cloud-event-schema">CloudEvent schema</a>.
 *
 * @see PublishEventGridEventsToTopic for a sample to send an Event Grid event.
 */
public class PublishCloudEventsToTopic {
    public static void main(String[] args) {
        EventGridPublisherClient<CloudEvent> publisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
            .buildCloudEventPublisherClient();

        // Create a CloudEvent with String data
        String str = "FirstName: John1, LastName:James";
        CloudEvent cloudEventStr = new CloudEvent("https://com.example.myapp", "User.Created.Text",
            BinaryData.fromObject(str), CloudEventDataFormat.JSON, "text/plain");

        // Create a CloudEvent with Object data
        User newUser = new User("John2", "James");
        CloudEvent cloudEventModel = new CloudEvent("https://com.example.myapp", "User.Created.Object",
            BinaryData.fromObject(newUser), CloudEventDataFormat.JSON, "application/json");
        // Create a CloudEvent with bytes data
        byte[] byteSample = "FirstName: John3, LastName: James".getBytes(StandardCharsets.UTF_8);
        CloudEvent cloudEventBytes = new CloudEvent("https://com.example.myapp", "User.Created.Binary",
            BinaryData.fromBytes(byteSample), CloudEventDataFormat.BYTES, "application/octet-stream");

        // Create a CloudEvent with Json String data
        String jsonStrData = "\"FirstName: John1, LastName:James\"";
        CloudEvent cloudEventJsonStrData = new CloudEvent("https://com.example.myapp", "User.Created.Text",
            BinaryData.fromString(jsonStrData), CloudEventDataFormat.JSON, "text/plain");

        // Send them to the event grid topic altogether.
        List<CloudEvent> events = new ArrayList<>();
        events.add(cloudEventStr);
        events.add(cloudEventModel);
        events.add(cloudEventBytes);
        events.add(cloudEventJsonStrData);
        events.add(cloudEventBytes.addExtensionAttribute("extension", "value"));
        publisherClient.sendEvents(events);
    }
}
