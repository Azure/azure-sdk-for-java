// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;

import com.azure.messaging.eventgrid.CloudEvent;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.EventGridSasCreator;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private final String endpoint = "endpoint";
    private final String key = "key";
    private final EventGridPublisherClient egClient = new EventGridPublisherClientBuilder().buildClient();
    private final String jsonData = "Json encoded event";

    public void createPublisherClient() {
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(key))
            .buildClient();
    }

    public void createAsyncPublisherClient() {
        EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(key))
            .buildAsyncClient();
    }

    public void sendEventGridEvents() {
        List<EventGridEvent> events = new ArrayList<>();
        events.add(
            new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", "Example Data",
                "1")
        );

        egClient.sendEvents(events);
    }

    public void sendCloudEvents() {
        List<CloudEvent> events = new ArrayList<>();
        events.add(
            new CloudEvent("com/example/source", "Com.Example.ExampleEventType")
                .setData("Example Data")
        );

        egClient.sendCloudEvents(events);
    }

    public void consumeEventGridEvent() {
        List<EventGridEvent> events = EventGridEvent.parse(jsonData);

        for (EventGridEvent event : events) {
            // system event data will be turned into it's rich object,
            // while custom event data will be turned into a byte[].
            if (event.isSystemEvent()) {
                Object systemEventData = event.asSystemEventData();
                if (systemEventData instanceof SubscriptionValidationEventData) {
                    SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) systemEventData;
                    System.out.println(validationData.getValidationCode());
                }
            }
            else {
                // we can turn the data into the correct type by calling this method.
                // since we set the data as a string when sending, we pass the String class in to get it back.
                String stringData = event.getData().toString();
                System.out.println(stringData); // "Example Data"
            }
        }
    }

    public void consumeCloudEvent() {
        List<CloudEvent> events = CloudEvent.parse(jsonData);

        for (CloudEvent event : events) {
            if (event.isSystemEvent()) {
                Object systemEventData = event.asSystemEventData();
                if (systemEventData instanceof SubscriptionValidationEventData) {
                    SubscriptionValidationEventData validationData = (SubscriptionValidationEventData) systemEventData;
                    System.out.println(validationData.getValidationCode());
                }
            }
            // this event type goes to any non-azure endpoint (such as a WebHook) when the subscription is created.
            else {
                // we can turn the data into the correct type by calling this method.
                // since we set the data as a string when sending, we pass the String class in to get it back.
                String stringData = event.getData().toString();
                System.out.println(stringData); // "Example Data"
            }
        }
    }

    public void createSharedAccessSignature() {
        OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
        String sasToken = EventGridSasCreator
            .createSas(endpoint, expiration, new AzureKeyCredential(key));
    }
}
