// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridClient;
import com.azure.messaging.eventgrid.EventGridClientBuilder;
import com.azure.messaging.eventgrid.models.AcknowledgeOptions;
import com.azure.messaging.eventgrid.models.ReceiveDetails;
import com.azure.messaging.eventgrid.models.ReceiveResult;
import com.azure.messaging.eventgrid.models.RejectOptions;
import com.azure.messaging.eventgrid.models.ReleaseOptions;
import com.azure.messaging.eventgrid.samples.models.User;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

public class EventGridNamespaceSample {

    public static final String TOPIC_NAME = System.getenv("AZURE_EVENTGRID_TOPIC");
    public static final String EVENT_SUBSCRIPTION_NAME = System.getenv("AZURE_EVENTGRID_EVENT_SUBSCRIPTION");
    public static final String EVENT_SOURCE = System.getenv("AZURE_EVENTGRID_EVENT_SOURCE");
    public static final String AZURE_EVENTGRID_ENDPOINT = System.getenv("AZURE_EVENTGRID_ENDPOINT");
    public static final AzureKeyCredential AZURE_EVENTGRID_KEY = new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_KEY"));

    public static void main(String[] args) {
        EventGridClient client = new EventGridClientBuilder()
            .endpoint(AZURE_EVENTGRID_ENDPOINT)
            .credential(AZURE_EVENTGRID_KEY)
            .buildClient();

        User john = new User("John", "James");
        User jack = new User("Jack", "James");
        User jim = new User("Jim", "James");

        CloudEvent event = new CloudEvent(EVENT_SOURCE, "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(john), CloudEventDataFormat.JSON, "application/json");
        CloudEvent event2 = new CloudEvent(EVENT_SOURCE, "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(jack), CloudEventDataFormat.JSON, "application/json");
        CloudEvent event3 = new CloudEvent(EVENT_SOURCE, "Microsoft.MockPublisher.TestEvent",
            BinaryData.fromObject(jim), CloudEventDataFormat.JSON, "application/json");
        // publish the event
        client.publishCloudEvent(TOPIC_NAME, event);

        // publish a batch of events

        client.publishCloudEvents(TOPIC_NAME, Arrays.asList(event2, event3));

        // receive events
        ReceiveResult result = client.receiveCloudEvents(TOPIC_NAME,
            EVENT_SUBSCRIPTION_NAME,
            10,
            Duration.ofSeconds(10));
        for (ReceiveDetails receiveDetails : result.getValue()) {
            CloudEvent cloudEvent = receiveDetails.getEvent();
            System.out.println("Event received: " + cloudEvent.getSubject());
            System.out.println("LockToken: " + receiveDetails.getBrokerProperties().getLockToken());
        }

        // Acknowledge events for John
        result.getValue().stream()
            .filter(receiveDetails -> (receiveDetails.getEvent().getData().toObject(User.class)).getFirstName().equals("John"))
            .forEach(receiveDetails -> client.acknowledgeCloudEvents(TOPIC_NAME,
                EVENT_SUBSCRIPTION_NAME,
                new AcknowledgeOptions(Collections.singletonList(receiveDetails.getBrokerProperties().getLockToken()))));

        // Release events for Jack
        result.getValue().stream()
            .filter(receiveDetails -> (receiveDetails.getEvent().getData().toObject(User.class)).getFirstName().equals("Jack"))
            .forEach(receiveDetails -> client.releaseCloudEvents(TOPIC_NAME,
                EVENT_SUBSCRIPTION_NAME,
                new ReleaseOptions(Collections.singletonList(receiveDetails.getBrokerProperties().getLockToken()))));

        // Reject events for Jim
        result.getValue().stream()
            .filter(receiveDetails -> (receiveDetails.getEvent().getData().toObject(User.class)).getFirstName().equals("Jim"))
            .forEach(receiveDetails -> client.rejectCloudEvents(TOPIC_NAME,
                EVENT_SUBSCRIPTION_NAME,
                new RejectOptions(Collections.singletonList(receiveDetails.getBrokerProperties().getLockToken()))));

    }
}
