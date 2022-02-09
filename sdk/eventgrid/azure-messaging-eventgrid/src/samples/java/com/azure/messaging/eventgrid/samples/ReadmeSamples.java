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
        // BEGIN: readme-sample-createSharedAccessSignature
        OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(20);
        String sasToken = EventGridPublisherClient
            .generateSas("<your event grid endpoint>", new AzureKeyCredential("<key for the endpoint>"), expiration);
        // END: readme-sample-createSharedAccessSignature
    }

    public void createCloudEventPublisherClient() {
        // BEGIN: readme-sample-createCloudEventPublisherClient
        // For CloudEvent
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCloudEventPublisherClient();
        // END: readme-sample-createCloudEventPublisherClient
    }

    public void createEventGridEventPublisherClient() {
        // BEGIN: readme-sample-createEventGridEventPublisherClient
        // For EventGridEvent
        EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts EventGridEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildEventGridEventPublisherClient();
        // END: readme-sample-createEventGridEventPublisherClient
    }

    public void createCustomEventPublisherClient() {
        // BEGIN: readme-sample-createCustomEventPublisherClient
        // For custom event
        EventGridPublisherClient<BinaryData> customEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts custom event schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCustomEventPublisherClient();
        // END: readme-sample-createCustomEventPublisherClient
    }

    public void createCloudEventPublisherAsyncClient() {
        // BEGIN: readme-sample-createCloudEventPublisherAsyncClient
        // For CloudEvent
        EventGridPublisherAsyncClient<CloudEvent> cloudEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCloudEventPublisherAsyncClient();
        // END: readme-sample-createCloudEventPublisherAsyncClient
    }

    public void createEventGridEventPublisherAsyncClient() {
        // BEGIN: readme-sample-createEventGridEventPublisherAsyncClient
        // For EventGridEvent
        EventGridPublisherAsyncClient<EventGridEvent> eventGridEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts EventGridEvent schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildEventGridEventPublisherAsyncClient();
        // END: readme-sample-createEventGridEventPublisherAsyncClient
    }

    public void createCustomEventPublisherAsyncClient() {
        // BEGIN: readme-sample-createCustomEventPublisherAsyncClient
        // For custom event
        EventGridPublisherAsyncClient<BinaryData> customEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts custom event schema>")
            .credential(new AzureKeyCredential("<key for the endpoint>"))
            .buildCustomEventPublisherAsyncClient();
        // END: readme-sample-createCustomEventPublisherAsyncClient
    }

    public void createPublisherClientWithSas() {
        // BEGIN: readme-sample-createPublisherClientWithSas
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureSasCredential("<sas token that can access the endpoint>"))
            .buildCloudEventPublisherClient();
        // END: readme-sample-createPublisherClientWithSas
    }

    public void createPublisherClientWithSasAsync() {
        // BEGIN: readme-sample-createPublisherClientWithSasAsync
        EventGridPublisherAsyncClient<CloudEvent> cloudEventAsyncClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new AzureSasCredential("<sas token that can access the endpoint>"))
            .buildCloudEventPublisherAsyncClient();
        // END: readme-sample-createPublisherClientWithSasAsync
    }

    public void sendCloudEventsToTopic() {
        // BEGIN: readme-sample-sendCloudEventsToTopic
        // Make sure that the event grid topic or domain you're sending to is able to accept the CloudEvent schema.
        List<CloudEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new CloudEvent("https://source.example.com", "Com.Example.ExampleEventType",
            BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json"));
        cloudEventClient.sendEvents(events);
        // END: readme-sample-sendCloudEventsToTopic
    }

    public void sendEventGridEventsToTopic() {
        // BEGIN: readme-sample-sendEventGridEventsToTopic
        // Make sure that the event grid topic or domain you're sending to is able to accept the EventGridEvent schema.
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new EventGridEvent("exampleSubject", "Com.Example.ExampleEventType", BinaryData.fromObject(user), "0.1"));
        eventGridEventClient.sendEvents(events);
        // END: readme-sample-sendEventGridEventsToTopic
    }

    public void sendEventGridEventsToDomain() {
        // BEGIN: readme-sample-sendEventGridEventsToDomain
        List<EventGridEvent> events = new ArrayList<>();
        User user = new User("John", "James");
        events.add(new EventGridEvent("com/example", "Com.Example.ExampleEventType", BinaryData.fromObject(user), "1")
            .setTopic("yourtopic"));
        eventGridEventClient.sendEvents(events);
        // END: readme-sample-sendEventGridEventsToDomain
    }

    public void sendCustomEventsToTopic() {
        // BEGIN: readme-sample-sendCustomEventsToTopic
        // Make sure that the event grid topic or domain you're sending to is able to accept the custom event schema.
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
        // END: readme-sample-sendCustomEventsToTopic
    }

    public void deserializeEvents() {
        // BEGIN: readme-sample-deserializeEvents
        // Deserialize an EventGridEvent
        String eventGridEventJsonData = "<your EventGridEvent json String>";
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventJsonData);

        // Deserialize a CloudEvent
        String cloudEventJsonData = "<your CloudEvent json String>";
        List<CloudEvent> cloudEvents = CloudEvent.fromString(cloudEventJsonData);
        // END: readme-sample-deserializeEvents
    }

    public void deserializeEventData(EventGridEvent eventGridEvent) {
        // BEGIN: readme-sample-deserializeEventData
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
        // END: readme-sample-deserializeEventData
    }


    public void systemEventDataSampleCode() {
        String eventGridEventJsonData = "<Your event grid event Json data>";
        List<EventGridEvent> events = EventGridEvent.fromString(eventGridEventJsonData);
        EventGridEvent event = events.get(0);

        // BEGIN: readme-sample-lookupSystemEventClass
        // Look up the System Event data class
        Class<?> eventDataClazz = SystemEventNames.getSystemEventMappings().get(event.getEventType());
        // END: readme-sample-lookupSystemEventClass

        // BEGIN: readme-sample-deserializeToSystemEventType
        // Deserialize the event data to an instance of a specific System Event data class type
        BinaryData data = event.getData();
        if (data != null) {
            StorageBlobCreatedEventData blobCreatedData = data.toObject(StorageBlobCreatedEventData.class);
            System.out.println(blobCreatedData.getUrl());
        }
        // END: readme-sample-deserializeToSystemEventType
    }

    public void systemEventDifferentEventData() {
        // BEGIN: readme-sample-systemEventDifferentEventData
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
        // END: readme-sample-systemEventDifferentEventData
    }

    public void createPublisherClientWithTokenCredential() {
        // BEGIN: readme-sample-createPublisherClientWithTokenCredential
        EventGridPublisherClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildCloudEventPublisherClient();
        // END: readme-sample-createPublisherClientWithTokenCredential
    }

    public void createPublisherAsyncClientWithTokenCredential() {
        // BEGIN: readme-sample-createPublisherAsyncClientWithTokenCredential
        EventGridPublisherAsyncClient<CloudEvent> cloudEventClient = new EventGridPublisherClientBuilder()
            .endpoint("<endpoint of your event grid topic/domain that accepts CloudEvent schema>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildCloudEventPublisherAsyncClient();
        // END: readme-sample-createPublisherAsyncClientWithTokenCredential
    }
}
