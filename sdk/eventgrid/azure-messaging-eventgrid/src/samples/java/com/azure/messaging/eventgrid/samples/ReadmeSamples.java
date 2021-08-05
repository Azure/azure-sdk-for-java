// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.core.models.CloudEvent;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.SystemEventNames;
import com.azure.messaging.eventgrid.samples.models.User;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueModifiedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the README.md
 */

public class ReadmeSamples {
    private final EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
        .buildEventGridEventPublisherClient();
    private final EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
        .buildCloudEventPublisherClient();
    private final EventGridPublisherClient<BinaryData> customEventClient = new EventGridPublisherClientBuilder()
        .buildCustomEventPublisherClient();
    private final String jsonData = "Json encoded event";

    public void createSharedAccessSignature() {
        OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
        String sasToken = EventGridPublisherClient
            .generateSas("<your event grid endpoint>", new AzureKeyCredential("<key for the endpoint>"), expiration);
    }

    public void createCloudEventPublisherClient() {
        // For CloudEvent
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCloudEventPublisherClient();
    }

    public void createEventGridEventPublisherClient() {
        // For EventGridEvent
        EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts EventGridEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildEventGridEventPublisherClient();
    }

    public void createCustomEventPublisherClient() {
        // For custom event
        EventGridPublisherClient<BinaryData> customEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts custom event schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCustomEventPublisherClient();
    }

    public void createCloudEventPublisherAsyncClient() {
        // For CloudEvent
        EventGridPublisherAsyncClient<CloudEvent> cloudEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCloudEventPublisherAsyncClient();
    }

    public void createEventGridEventPublisherAsyncClient() {
        // For EventGridEvent
        EventGridPublisherAsyncClient<EventGridEvent> eventGridEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts EventGridEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildEventGridEventPublisherAsyncClient();
    }

    public void createCustomEventPublisherAsyncClient() {
        // For custom event
        EventGridPublisherAsyncClient<BinaryData> customEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts custom event schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCustomEventPublisherAsyncClient();
    }

    public void createPublisherClientWithSas() {
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureSasCredential("<sas token that can access the endpoint>"))
            .buildCloudEventPublisherClient();
    }

    public void createPublisherClientWithSasAsync() {
        EventGridPublisherAsyncClient<CloudEvent> cloudEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpont of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureSasCredential("<sas token that can access the endpoint>"))
            .buildCloudEventPublisherAsyncClient();
    }

    public void sendCloudEventsToTopic() {
        // Make sure that the event grid topic or domain you're sending to accepts CloudEvent schema.
        List<CloudEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new CloudEvent("https://source.example.com", "Com.Example.ExampleEventType",
            BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json"));
        cloudEventClient.sendEvents(events);
    }

    public void sendEventGridEventsToTopic() {
        // Make sure that the event grid topic or domain you're sending to accepts EventGridEvent schema.
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", BinaryData.fromObject(user), "0.1"));
        eventGridEventClient.sendEvents(events);
    }

    public void sendEventGridEventsToDomain() {
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new EventGridEvent("com/example", "Com.Example.ExampleEventType", BinaryData.fromObject(user), "1")
            .setTopic("yourtopic"));
        eventGridEventClient.sendEvents(events);
    }

    public void sendCustomEventsToTopic() {
        // Make sure that the event grid topic or domain you're sending to accepts the custom event schema.
        List<BinaryData> events = new ArrayList<>();
        events.add(BinaryData.fromObject(new HashMap<String, String>() {
            {
                put("id", UUID.randomUUID().toString());
                put("time", OffsetDateTime.now().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
                put("data", "example data");
                put("dataVersion", "0.1");
            }
        }));
        customEventClient.sendEvents(events);
    }

    public void deserializeEvents() {
        // Deserialize an EventGridEvent
        String eventGridEventJsonData = "<your EventGridEvent json String>";
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventJsonData);

        // Deserialize a CloudEvent
        String cloudEventJsonData = "<your CloudEvent json String>";
        List<CloudEvent> cloudEvents = CloudEvent.fromString(cloudEventJsonData);
    }

    public void deserializeEventData(EventGridEvent eventGridEvent) {
        BinaryData eventData = eventGridEvent.getData();

        //Deserialize data to a model class
        User dataInModelClass = eventData.toObject(User.class);

        //Deserialize data to a Map
        Map<String, Object> dataMap = eventData.toObject(new TypeReference<Map<String, Object>>() {
        });

        //Deserialize Json String to a String
        String dataString = eventData.toObject(String.class);

        //Deserialize String data to a String
        String dataInJsonString = eventData.toString();

        //Deserialize data to byte array (byte[])
        byte[] dataInBytes = eventData.toBytes();
    }


    public void systemEventDataSampleCode() {
        String eventGridEventJsonData = "<Your event grid event Json data>";
        List<EventGridEvent> events = EventGridEvent.fromString(eventGridEventJsonData);
        EventGridEvent event = events.get(0);

        // Look up the System Event data class
        Class<?> eventDataClazz = SystemEventNames.getSystemEventMappings().get(event.getEventType());

        // Deserialize the event data to an instance of a specific System Event data class type
        BinaryData data = event.getData();
        if (data != null) {
            StorageBlobCreatedEventData blobCreatedData = data.toObject(StorageBlobCreatedEventData.class);
            System.out.println(blobCreatedData.getUrl());
        }
    }

    public void systemEventDifferentEventData() {
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString("<Your EventGridEvent Json String>");
        for (EventGridEvent eventGridEvent : eventGridEvents) {
            BinaryData binaryData = eventGridEvent.getData();
            switch (eventGridEvent.getEventType()) {
                case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_DELETED:
                    AppConfigurationKeyValueDeletedEventData keyValueDeletedEventData =
                        binaryData.toObject(TypeReference.createInstance(AppConfigurationKeyValueDeletedEventData.class));
                    System.out.println("Processing the AppConfigurationKeyValueDeletedEventData...");
                    System.out.printf("The key is: %s%n", keyValueDeletedEventData.getKey());
                    break;
                case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_MODIFIED:
                    AppConfigurationKeyValueModifiedEventData keyValueModifiedEventData =
                        binaryData.toObject(TypeReference.createInstance(AppConfigurationKeyValueModifiedEventData.class));
                    System.out.println("Processing the AppConfigurationKeyValueModifiedEventData...");
                    System.out.printf("The key is: %s%n", keyValueModifiedEventData.getKey());
                    break;
                default:
                    System.out.printf("%s isn't an AppConfiguration event data%n", eventGridEvent.getEventType());
                    break;
            }
        }
    }

    public void createPublisherClientWithTokenCredential() {
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildCloudEventPublisherClient();
    }

    public void createPublisherAsyncClientWithTokenCredential() {
        EventGridPublisherAsyncClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildCloudEventPublisherAsyncClient();
    }
}
