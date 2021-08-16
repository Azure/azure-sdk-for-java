// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.BinaryData;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CloudEventJavaDocCodeSnippet {
    private static class User {
        private final String firstName;
        private final String lastName;

        User(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    public void createCloudEvent() {
        // BEGIN: com.azure.core.model.CloudEvent#constructor
        // Use BinaryData.fromBytes() to create data in format CloudEventDataFormat.BYTES
        byte[] exampleBytes = "Hello World".getBytes(StandardCharsets.UTF_8);
        CloudEvent cloudEvent = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromBytes(exampleBytes), CloudEventDataFormat.BYTES, "application/octet-stream");

        // Use BinaryData.fromObject() to create CloudEvent data in format CloudEventDataFormat.JSON
        // From a model class
        User user = new User("Stephen", "James");
        CloudEvent cloudEventDataObject = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(user), CloudEventDataFormat.JSON, "application/json");

        // From a String
        CloudEvent cloudEventDataStr = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject("Hello World"), CloudEventDataFormat.JSON, "text/plain");

        // From an Integer
        CloudEvent cloudEventDataInt = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(1), CloudEventDataFormat.JSON, "int");

        // From a Boolean
        CloudEvent cloudEventDataBool = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(true), CloudEventDataFormat.JSON, "bool");

        // From null
        CloudEvent cloudEventDataNull = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromObject(null), CloudEventDataFormat.JSON, "null");

        // Use BinaryData.fromString() if you have a Json String for the CloudEvent data.
        String jsonStringForData = "\"Hello World\"";  // A json String.
        CloudEvent cloudEventDataJsonStr = new CloudEvent("/cloudevents/example/source", "Example.EventType",
            BinaryData.fromString(jsonStringForData), CloudEventDataFormat.JSON, "text/plain");
        // END: com.azure.core.model.CloudEvent#constructor
    }

    public void fromJsonStringWithDataJson() {
        String cloudEventJsonString = "<A CloudEvent Json String>";

        // BEGIN: com.azure.core.model.CloudEvent.fromString
        List<CloudEvent> cloudEventList = CloudEvent.fromString(cloudEventJsonString);
        CloudEvent cloudEvent = cloudEventList.get(0);
        BinaryData cloudEventData = cloudEvent.getData();

        byte[] bytesValue = cloudEventData.toBytes();  // If data payload is in bytes (data_base64 is not null).
        User objectValue = cloudEventData.toObject(User.class);  // If data payload is a User object.
        int intValue = cloudEventData.toObject(Integer.class);  // If data payload is an int.
        boolean boolValue = cloudEventData.toObject(Boolean.class);  // If data payload is boolean.
        String stringValue = cloudEventData.toObject(String.class);  // If data payload is String.
        String jsonStringValue = cloudEventData.toString();  // The data payload represented in Json String.
        // END: com.azure.core.model.CloudEvent.fromString
    }
}
