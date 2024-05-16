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

public class PublishEventsToDomain {
    public static void main(String[] args) {
        EventGridPublisherClient<EventGridEvent> publisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_DOMAIN_ENDPOINT"))  // Event Grid Domain endpoint
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_DOMAIN_KEY")))
            .buildEventGridEventPublisherClient();

        User newUser = new User("John2", "James");
        EventGridEvent eventModelClass = new EventGridEvent("A user is created", "User.Created.Object", BinaryData.fromObject(newUser), "0.1")
            .setTopic("usertopic");  // topic must be set when sending to an Event Grid Domain.

        List<EventGridEvent> events = new ArrayList<>();
        events.add(eventModelClass);
        publisherClient.sendEvents(events);
    }
}
