// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridDeserializer;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;

import java.util.List;

public class ProcessSystemEvents {
    public static void main(String[] args) {
        String eventGridJsonString = "[\n" +
            "  {\n" +
            "    \"id\": \"56afc886-767b-d359-d59e-0da7877166b2\",\n" +
            "    \"topic\": \"/SUBSCRIPTIONS/ID/RESOURCEGROUPS/rg/PROVIDERS/MICROSOFT.ContainerRegistry/test1\",\n" +
            "    \"subject\": \"test1\",\n" +
            "    \"eventType\": \"Microsoft.AppConfiguration.KeyValueDeleted\",\n" +
            "    \"eventTime\": \"2018-01-02T19:17:44.4383997Z\",\n" +
            "    \"data\": {\n" +
            "        \"key\":\"key1\",\n" +
            "        \"label\":\"label1\",\n" +
            "        \"etag\":\"etag1\"\n" +
            "    },\n" +
            "    \"dataVersion\": \"\",\n" +
            "    \"metadataVersion\": \"1\"\n" +
            "  }\n" +
            "]\n";

        List<EventGridEvent> eventGridEvents = EventGridDeserializer.deserializeEventGridEvents(eventGridJsonString);

        for (EventGridEvent eventGridEvent : eventGridEvents) {
            if (eventGridEvent.isSystemEvent()) {
                Object systemEventData = eventGridEvent.asSystemEventData();
                if (systemEventData instanceof AppConfigurationKeyValueDeletedEventData) {
                    AppConfigurationKeyValueDeletedEventData keyValueDeletedEventData =
                        (AppConfigurationKeyValueDeletedEventData) systemEventData;
                    System.out.println("Processing the AppConfigurationKeyValueDeletedEventData...");
                    System.out.printf("The key is: %s%n", keyValueDeletedEventData.getKey());
                } else {
                    System.out.printf("%s isn't an AppConfigurationKeyValueDeletedEventData%n", eventGridEvent.getEventType());
                }
            } else {
                System.out.printf("%s isn't a system event%n", eventGridEvent.getEventType());
                BinaryData data = eventGridEvent.getData();
                // process the data. Refer to other samples that parse events from a string and process BinaryData.
            }
        }
    }
}
