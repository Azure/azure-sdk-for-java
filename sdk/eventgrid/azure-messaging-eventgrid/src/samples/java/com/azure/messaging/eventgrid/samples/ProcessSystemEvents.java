// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.SystemEventNames;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueModifiedEventData;

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

        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridJsonString);

        for (EventGridEvent eventGridEvent : eventGridEvents) {
            processEventsUsingEventType(eventGridEvent);

            // Alternatively, this also works.
            processEventsUsingInstanceOf(eventGridEvent);
        }

    }

    private static void processEventsUsingEventType(EventGridEvent eventGridEvent) {
        BinaryData data = eventGridEvent.getData();
        switch (eventGridEvent.getEventType()) {
            case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_DELETED:
                AppConfigurationKeyValueDeletedEventData keyValueDeletedEventData =
                    data.toObject(TypeReference.createInstance(AppConfigurationKeyValueDeletedEventData.class));
                System.out.println("Processing the AppConfigurationKeyValueDeletedEventData...");
                System.out.printf("The key is: %s%n", keyValueDeletedEventData.getKey());
                break;

            case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_MODIFIED:
                AppConfigurationKeyValueModifiedEventData keyValueModifiedEventData =
                    data.toObject(TypeReference.createInstance(AppConfigurationKeyValueModifiedEventData.class));
                System.out.println("Processing the AppConfigurationKeyValueModifiedEventData...");
                System.out.printf("The key is: %s%n", keyValueModifiedEventData.getKey());
                break;
            default:
                System.out.printf("%s isn't an AppConfiguration event data%n", eventGridEvent.getEventType());
                break;
        }
    }

    private static void processEventsUsingInstanceOf(EventGridEvent eventGridEvent) {
        if (eventGridEvent.isSystemEvent()) {
            Object systemEventData = eventGridEvent.asSystemEventData();
            if (systemEventData instanceof AppConfigurationKeyValueDeletedEventData) {
                //   This code is for Java 8+. With Java 14+, using instanceof will not need type cast.
                AppConfigurationKeyValueDeletedEventData keyValueDeletedEventData =
                    (AppConfigurationKeyValueDeletedEventData) systemEventData;
                System.out.println("Processing the AppConfigurationKeyValueDeletedEventData...");
                System.out.printf("The key is: %s%n", keyValueDeletedEventData.getKey());
            } else if (systemEventData instanceof AppConfigurationKeyValueModifiedEventData) {
                AppConfigurationKeyValueModifiedEventData keyValueModifiedEventData =
                    (AppConfigurationKeyValueModifiedEventData) systemEventData;
                System.out.println("Processing the AppConfigurationKeyValueModifiedEventData...");
                System.out.printf("The key is: %s%n", keyValueModifiedEventData.getKey());
            } else {
                System.out.printf("%s isn't an AppConfiguration event data%n", eventGridEvent.getEventType());
            }
        } else {
            System.out.printf("%s isn't a system event%n", eventGridEvent.getEventType());
            BinaryData data = eventGridEvent.getData();
            // process the data. Refer to other samples that parse events from a string and process BinaryData.
        }
    }
}
