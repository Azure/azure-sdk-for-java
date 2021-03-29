// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.samples.models.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DeserializeEventsFromString {

    private static void deserializeCloudEventsFromJsonString() {
        System.out.println("Parsing a Cloud Event string");

        String cloudEventStringJsonData = "{"
            + "\"id\":\"f47d7b0a-4cc7-4108-aaff-f1122d87807c\","
            + "\"source\":\"https://com.example.myapp\","
            + "\"data\":{\"firstName\":\"John1\",\"lastName\":\"James\"},"
            + "\"type\":\"User.Created.Object\","
            + "\"specversion\":\"1.0\","
            + "\"datacontenttype\":\"application/json\""
            + "}";
        List<CloudEvent> cloudEvents = CloudEvent.fromString(cloudEventStringJsonData);

        CloudEvent cloudEvent = cloudEvents.get(0);

        BinaryData data = cloudEvent.getData();
        if (data != null) {
            deserializeData(data);
        }
    }

    private static void deserializeEventGridEventsFromJsonString() {
        System.out.println("Parsing an Event Grid Event string");

        String eventGridEventStringJsonData = "{\"id\":\"3b07dc21-08af-4558-9e94-822a20c48a0b\","
            + "\"subject\":\"example user\","
            + "\"data\":{\"firstName\":\"John2\",\"lastName\":\"James\"},"
            + "\"eventType\":\"User.Created.Object\","
            + "\"dataVersion\":\"0.1\","
            + "\"metadataVersion\":\"1\","
            + "\"eventTime\":\"2021-01-12T22:23:38.756238Z\","
            + "\"topic\":\"/exampleTopic\"}\n";
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventStringJsonData);
        EventGridEvent eventGridEvent = eventGridEvents.get(0);
        BinaryData data = eventGridEvent.getData();
        if (data != null) {
            deserializeData(data);
        }
    }

    private static void deserializeData(BinaryData eventData) {
        System.out.println("Deserialize data to a model class:");
        User dataInModelClass = eventData.toObject(User.class);
        System.out.println(dataInModelClass);
        System.out.println();

        System.out.println("Deserialize data to a Map:");
        Map<String, Object> dataMap = eventData.toObject(new TypeReference<Map<String, Object>>() {
        });
        System.out.println(dataMap);
        System.out.println();

        System.out.println("Deserialize data to a String in Json literal:");
        String dataInJsonString = eventData.toString();
        System.out.println(dataInJsonString);
        System.out.println();

        System.out.println("Deserialize data to byte[]");
        byte[] dataInBytes = eventData.toBytes();
        System.out.println(Arrays.toString(dataInBytes));
        System.out.println();
    }

    public static void main(String[] args) {
        deserializeCloudEventsFromJsonString();
        deserializeEventGridEventsFromJsonString();
    }
}
