// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.samples.models.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventGridEventJavaDocCodeSnippet {

    public void createEventGridEvent() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridEvent#constructor
        // Use BinaryData.fromObject() to create EventGridEvent data
        // From a model class
        User user = new User("Stephen", "James");
        EventGridEvent eventGridEventDataObject = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(user), "0.1");

        // From a String
        EventGridEvent eventGridEventDataStr = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject("Hello World"), "0.1");

        // From an Integer
        EventGridEvent eventGridEventDataInt = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(1), "0.1");

        // From a Boolean
        EventGridEvent eventGridEventDataBool = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(true), "0.1");

        // From null
        EventGridEvent eventGridEventDataNull = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(null), "0.1");

        // Use BinaryData.fromString() if you have a Json String for the EventGridEvent data.
        String jsonStringForData = "\"Hello World\"";  // A json String.
        EventGridEvent eventGridEventDataDataJsonStr = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromString(jsonStringForData), "0.1");
        // END: com.azure.messaging.eventgrid.EventGridEvent#constructor
    }

    public void fromJsonStringWithDataJson() {
        String eventGridEventJsonString = "<A EventGridEvent Json String>";

        // BEGIN: com.azure.messaging.eventgrid.EventGridEvent.fromString
        List<EventGridEvent> eventGridEventList = EventGridEvent.fromString(eventGridEventJsonString);
        EventGridEvent eventGridEvent = eventGridEventList.get(0);
        BinaryData eventGridEventData = eventGridEvent.getData();

        User objectValue = eventGridEventData.toObject(User.class);  // If data payload is a User object.
        int intValue = eventGridEventData.toObject(Integer.class);  // If data payload is an int.
        boolean boolValue = eventGridEventData.toObject(Boolean.class);  // If data payload is boolean.
        String stringValue = eventGridEventData.toObject(String.class);  // If data payload is String.
        String jsonStringValue = eventGridEventData.toString();  // The data payload represented in Json String.
        // END: com.azure.messaging.eventgrid.EventGridEvent.fromString
    }

    public void sendCloudEventsAsync() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient
        // Create a client to send events of CloudEvent schema (com.azure.core.models.CloudEvent)
        EventGridPublisherAsyncClient<CloudEvent> cloudEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
            .buildCloudEventPublisherAsyncClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent
        // Create a com.azure.models.CloudEvent.
        User user = new User("Stephen", "James");
        CloudEvent cloudEventDataObject = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");

        // Send a single CloudEvent
        cloudEventPublisherClient.sendEvent(cloudEventDataObject).block();

        // Send a list of CloudEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        cloudEventPublisherClient.sendEvents(Arrays.asList(
            cloudEventDataObject
            // add more CloudEvents objects
        )).block();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent
    }
    public void sendEventGridEventsAsync() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient
        // Create a client to send events of EventGridEvent schema
        EventGridPublisherAsyncClient<EventGridEvent> eventGridEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")))
            .buildEventGridEventPublisherAsyncClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent
        // Create an EventGridEvent
        User user = new User("John", "James");
        EventGridEvent eventGridEvent = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(user), "0.1");

        // Send a single EventGridEvent
        eventGridEventPublisherClient.sendEvent(eventGridEvent).block();

        // Send a list of EventGridEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        eventGridEventPublisherClient.sendEvents(Arrays.asList(
            eventGridEvent
            // add more EventGridEvents objects
        )).block();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent
    }

    public void sendCustomEventsAsync() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCustomEventClient
        // Create a client to send events of custom event
        EventGridPublisherAsyncClient<BinaryData> customEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_CUSTOM_EVENT_ENDPOINT"))  // make sure it accepts custom events
            .credential(new AzureKeyCredential(System.getenv("AZURE_CUSTOM_EVENT_KEY")))
            .buildCustomEventPublisherAsyncClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCustomEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCustomEvent
        // Create an custom event object (both POJO and Map work)
        Map<String, Object> customEvent = new HashMap<String, Object>() {
            {
                put("id", UUID.randomUUID().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
                put("data", 100.0);
                put("dataVersion", "0.1");
            }
        };

        // Send a single custom event
        customEventPublisherClient.sendEvent(BinaryData.fromObject(customEvent)).block();

        // Send a list of EventGridEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        customEventPublisherClient.sendEvents(Arrays.asList(
            BinaryData.fromObject(customEvent)
            // add more custom events in BinaryData
        )).block();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCustomEvent
    }

    public void sendCloudEvents() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCloudEventClient
        // Create a client to send events of CloudEvent schema (com.azure.core.models.CloudEvent)
        EventGridPublisherClient<CloudEvent> cloudEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")))
            .buildCloudEventPublisherClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCloudEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent
        // Create a com.azure.models.CloudEvent.
        User user = new User("Stephen", "James");
        CloudEvent cloudEventDataObject = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");

        // Send a single CloudEvent
        cloudEventPublisherClient.sendEvent(cloudEventDataObject);

        // Send a list of CloudEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        cloudEventPublisherClient.sendEvents(Arrays.asList(
            cloudEventDataObject
            // add more CloudEvents objects
        ));
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent
    }

    public void sendEventGridEvents() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient
        // Create a client to send events of EventGridEvent schema
        EventGridPublisherClient<EventGridEvent> eventGridEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")))
            .buildEventGridEventPublisherClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent
        // Create an EventGridEvent
        User user = new User("John", "James");
        EventGridEvent eventGridEvent = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(user), "0.1");

        // Send a single EventGridEvent
        eventGridEventPublisherClient.sendEvent(eventGridEvent);

        // Send a list of EventGridEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        eventGridEventPublisherClient.sendEvents(Arrays.asList(
            eventGridEvent
            // add more EventGridEvents objects
        ));
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent
    }

    public void sendCustomEvents() {
        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCustomEventClient
        // Create a client to send events of custom event
        EventGridPublisherClient<BinaryData> customEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_CUSTOM_EVENT_ENDPOINT"))  // make sure it accepts custom events
            .credential(new AzureKeyCredential(System.getenv("AZURE_CUSTOM_EVENT_KEY")))
            .buildCustomEventPublisherClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCustomEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#SendCustomEvent
        // Create an custom event object
        Map<String, Object> customEvent = new HashMap<String, Object>() {
            {
                put("id", UUID.randomUUID().toString());
                put("subject", "Test");
                put("foo", "bar");
                put("type", "Microsoft.MockPublisher.TestEvent");
                put("data", 100.0);
                put("dataVersion", "0.1");
            }
        };

        // Send a single custom event
        customEventPublisherClient.sendEvent(BinaryData.fromObject(customEvent));

        // Send a list of custom events to the EventGrid service altogether.
        // This has better performance than sending one by one.
        customEventPublisherClient.sendEvents(Arrays.asList(
            BinaryData.fromObject(customEvent)
            // add more custom events in BinaryData
        ));
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#SendCustomEvent
    }
}
