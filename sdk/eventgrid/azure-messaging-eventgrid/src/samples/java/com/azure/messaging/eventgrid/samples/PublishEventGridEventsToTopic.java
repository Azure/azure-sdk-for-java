// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.samples.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This sample code shows how to send {@link EventGridEvent EventGridEvents} to an Event Grid Topic that accepts Event Grid event schema.
 * Refer to the <a href="https://docs.microsoft.com/en-us/azure/event-grid/event-schema">EventGridEvent schema</a>.
 *
 * @see PublishCloudEventsToTopic for a sample to send a CloudEvent
 */
public class PublishEventGridEventsToTopic {
    public static void main(String[] args) {
        EventGridPublisherClient<EventGridEvent> publisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")))
            .buildEventGridEventPublisherClient();

        // Create a EventGridEvent with String data
        String str = "FirstName: John1, LastName: James";
        EventGridEvent eventJson = new EventGridEvent("com/example/MyApp", "User.Created.Text", BinaryData.fromObject(str), "0.1");
        // Create a CloudEvent with Object data
        User newUser = new User("John2", "James");
        EventGridEvent eventModelClass = new EventGridEvent("com/example/MyApp", "User.Created.Object", BinaryData.fromObject(newUser), "0.1");
        // Send them to the event grid topic altogether.

        List<EventGridEvent> events = new ArrayList<>();
        events.add(eventJson);
        events.add(eventModelClass);

        publisherClient.sendEvents(events);
    }
}
