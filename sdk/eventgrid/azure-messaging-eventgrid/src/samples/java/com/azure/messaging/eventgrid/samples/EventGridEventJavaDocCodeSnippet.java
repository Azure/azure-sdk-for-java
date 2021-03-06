// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.samples.models.User;

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
}
