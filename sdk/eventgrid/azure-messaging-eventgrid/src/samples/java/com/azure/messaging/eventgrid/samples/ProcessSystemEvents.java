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
        String eventGridJsonString = "[\n"
            + "  {\n"
            + "    \"id\": \"56afc886-767b-d359-d59e-0da7877166b2\",\n"
            + "    \"topic\": \"/SUBSCRIPTIONS/ID/RESOURCEGROUPS/rg/PROVIDERS/MICROSOFT.ContainerRegistry/test1\",\n"
            + "    \"subject\": \"test1\",\n"
            + "    \"eventType\": \"Microsoft.AppConfiguration.KeyValueDeleted\",\n"
            + "    \"eventTime\": \"2018-01-02T19:17:44.4383997Z\",\n"
            + "    \"data\": {\n"
            + "        \"key\":\"key1\",\n"
            + "        \"label\":\"label1\",\n"
            + "        \"etag\":\"etag1\"\n"
            + "    },\n"
            + "    \"dataVersion\": \"\",\n"
            + "    \"metadataVersion\": \"1\"\n"
            + "  }\n"
            + "]\n";

        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridJsonString);

        for (EventGridEvent eventGridEvent : eventGridEvents) {
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
    }
}
