// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.CloudEvent;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.EventGridSasGenerator;
import com.azure.messaging.eventgrid.SystemEventNames;
import com.azure.messaging.eventgrid.samples.models.User;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobRenamedEventData;
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

    public void createPublisherClientWithSAS() {
        EventGridPublisherClient egClient = new EventGridPublisherClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureSasCredential(key))
            .buildClient();
    }

    public void createAsyncPublisherClientWithSAS() {
        EventGridPublisherAsyncClient egAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureSasCredential(key))
            .buildAsyncClient();
    }

    public void sendEventGridEventsToTopic() {
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", user, "1"));
        egClient.sendEventGridEvents(events);
    }

    public void sendCloudEventsToTopic() {
        List<CloudEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new CloudEvent("https://source.example.com", "Com.Example.ExampleEventType", user));
        egClient.sendCloudEvents(events);
    }

    public void deserializeEventGridEvent() {
        List<EventGridEvent> events = EventGridEvent.fromString(jsonData);
        for (EventGridEvent event : events) {
            if (event.getEventType().equals(SystemEventNames.EVENT_GRID_SUBSCRIPTION_VALIDATION)) {
                SubscriptionValidationEventData validationData = event.getData()
                    .toObject(TypeReference.createInstance(SubscriptionValidationEventData.class));
                System.out.println(validationData.getValidationCode());
            }
            else {
                // we can turn the data into the correct type by calling BinaryData.toString(), BinaryData.toObject(),
                // or BinaryData.toBytes(). This sample uses toString.
                BinaryData binaryData = event.getData();
                if (binaryData != null) {
                    System.out.println(binaryData.toString());
                }
            }
        }
    }

    public void deserializeCloudEvent() {
        List<CloudEvent> events = CloudEvent.fromString(jsonData);
        for (CloudEvent event : events) {
            if (event.getType().equals(SystemEventNames.EVENT_GRID_SUBSCRIPTION_VALIDATION)) {
                SubscriptionValidationEventData validationData = event.getData()
                    .toObject(TypeReference.createInstance(SubscriptionValidationEventData.class));
                System.out.println(validationData.getValidationCode());
            }
            else {
                // we can turn the data into the correct type by calling BinaryData.toString(), BinaryData.toObject(),
                // or BinaryData.toBytes(). This sample uses toString.
                BinaryData binaryData = event.getData();
                if (binaryData != null) {
                    System.out.println(binaryData.toString()); // "Example Data"
                }
            }
        }
    }

    public void createSharedAccessSignature() {
        OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
        String sasToken = EventGridSasGenerator
            .generateSas(endpoint, new AzureKeyCredential(key), expiration);
    }

    public void sendEventGridEventsToDomain() {
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(
            new EventGridEvent("com/example", "Com.Example.ExampleEventType", user, "1")
                .setTopic("yourtopic"));
        egClient.sendEventGridEvents(events);
    }

    public void systemEventDataSampleCode() {
        String eventGridEventJsonData = "Your event grid event Json data";
        List<EventGridEvent> events = EventGridEvent.fromString(eventGridEventJsonData);
        EventGridEvent event = events.get(0);

        // Look up the System Event data class
        Class<?> eventDataClazz = SystemEventNames.getSystemEventMappings().get(event.getEventType());

        // Deserialize the event data to an instance of a specific System Event data class type
        BinaryData data = event.getData();
        if (data != null) {
            StorageBlobCreatedEventData blobCreatedData = data.toObject(TypeReference.createInstance(StorageBlobCreatedEventData.class));
            System.out.println(blobCreatedData.getUrl());
        }
    }
}
