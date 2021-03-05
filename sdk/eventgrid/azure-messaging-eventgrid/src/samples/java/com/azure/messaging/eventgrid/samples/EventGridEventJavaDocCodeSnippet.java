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

import java.util.ArrayList;
import java.util.List;

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

    public void sendEventsAsync() {
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
        cloudEventPublisherClient.sendEvents(new ArrayList<CloudEvent>() {
            {
                add(cloudEventDataObject);
                // add more CloudEvent objects
            }
        }).block();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient
        // Create a client to send events of EventGridEvent schema
        EventGridPublisherAsyncClient<EventGridEvent> eventGridEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")))
            .buildEventGridEventPublisherAsyncClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateEventGridEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent
        // Create an EventGridEvent
        User user2 = new User("John", "James");
        EventGridEvent eventGridEvent = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(user2), "0.1");

        // Send a single EventGridEvent
        eventGridEventPublisherClient.sendEvent(eventGridEvent).block();

        // Send a list of EventGridEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        eventGridEventPublisherClient.sendEvents(new ArrayList<EventGridEvent>() {
            {
                add(eventGridEvent);
                // add more EventGridEvents objects
            }
        }).block();
        // END: com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent
    }

    public void sendEvents() {
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
        cloudEventPublisherClient.sendEvents(new ArrayList<CloudEvent>() {
            {
                add(cloudEventDataObject);
                // add more CloudEvent objects
            }
        });
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient
        // Create a client to send events of EventGridEvent schema
        EventGridPublisherClient<EventGridEvent> eventGridEventPublisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")))
            .buildEventGridEventPublisherClient();
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient

        // BEGIN: com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent
        // Create an EventGridEvent
        User user2 = new User("John", "James");
        EventGridEvent eventGridEvent = new EventGridEvent("/EventGridEvents/example/source",
            "Example.EventType", BinaryData.fromObject(user2), "0.1");

        // Send a single EventGridEvent
        eventGridEventPublisherClient.sendEvent(eventGridEvent);

        // Send a list of EventGridEvents to the EventGrid service altogether.
        // This has better performance than sending one by one.
        eventGridEventPublisherClient.sendEvents(new ArrayList<EventGridEvent>() {
            {
                add(eventGridEvent);
                // add more EventGridEvents objects
            }
        });
        // END: com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent
    }
}
