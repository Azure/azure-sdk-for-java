// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.models.AcsChatMessageReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.models.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.models.AppConfigurationKeyValueModifiedEventData;
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;
import java.util.List;

/**
 * Code samples for the README.md of azure-messaging-eventgrid-systemevents package.
 * This class demonstrates how to work with Azure Event Grid System Events using the models and utilities
 * provided by the azure-messaging-eventgrid-systemevents package.
 */
public final class ReadmeSamples {

    /**
     * Sample showing how to get system event type constants.
     */
    public void getSystemEventTypeConstants() {
        // BEGIN: readme-sample-getSystemEventTypeConstants
        // Access predefined event type constants
        String blobCreatedEventType = SystemEventNames.STORAGE_BLOB_CREATED;
        String keyVaultSecretExpiredEventType = SystemEventNames.KEY_VAULT_SECRET_NEAR_EXPIRY;
        // END: readme-sample-getSystemEventTypeConstants
    }

    /**
     * Sample showing how to look up event data model class for an event type.
     */
    public void lookupSystemEventClass() {
        String eventType = "Microsoft.Storage.BlobCreated";

        // BEGIN: readme-sample-lookupSystemEventClass
        // Find the appropriate model class for an event type
        Class<?> eventDataClass = SystemEventNames.getSystemEventMappings().get(eventType);
        if (eventDataClass != null) {
            System.out.println("Event data should be deserialized to: " + eventDataClass.getSimpleName());
        }
        // END: readme-sample-lookupSystemEventClass
    }

    /**
     * Sample showing how to deserialize system event data.
     * This assumes you have an EventGridEvent from the main EventGrid SDK.
     */
    public void deserializeSystemEventData() {
        // This would come from the main EventGrid SDK
        String eventJson = "{}"; // placeholder

        // BEGIN: readme-sample-deserializeSystemEventData
        // Assuming you have an EventGridEvent from the main EventGrid SDK
        EventGridEvent event = EventGridEvent.fromString(eventJson).get(0);

        if (SystemEventNames.STORAGE_BLOB_CREATED.equals(event.getEventType())) {
            BinaryData eventData = event.getData();
            StorageBlobCreatedEventData blobData = eventData.toObject(StorageBlobCreatedEventData.class);

            System.out.println("Blob URL: " + blobData.getUrl());
            System.out.println("Blob size: " + blobData.getContentLength());
            System.out.println("Content type: " + blobData.getContentType());
        }
        // END: readme-sample-deserializeSystemEventData
    }

    /**
     * Sample showing how to handle multiple event types.
     */
    public void handleMultipleEventTypes() {
        String eventsJson = "[]"; // placeholder

        // BEGIN: readme-sample-handleMultipleEventTypes
        List<EventGridEvent> events = EventGridEvent.fromString(eventsJson);

        for (EventGridEvent event : events) {
            BinaryData data = event.getData();

            switch (event.getEventType()) {
                case SystemEventNames.STORAGE_BLOB_CREATED:
                    StorageBlobCreatedEventData blobCreated = data.toObject(StorageBlobCreatedEventData.class);
                    System.out.println("New blob: " + blobCreated.getUrl());
                    break;

                case SystemEventNames.APP_CONFIGURATION_KEY_VALUE_MODIFIED:
                    AppConfigurationKeyValueModifiedEventData configModified =
                        data.toObject(AppConfigurationKeyValueModifiedEventData.class);
                    System.out.println("Config key modified: " + configModified.getKey());
                    break;

                case SystemEventNames.ACS_CHAT_MESSAGE_RECEIVED:
                    AcsChatMessageReceivedEventData chatMessage =
                        data.toObject(AcsChatMessageReceivedEventData.class);
                    System.out.println("Chat message: " + chatMessage.getMessageBody());
                    break;

                default:
                    System.out.println("Unhandled event type: " + event.getEventType());
                    break;
            }
        }
        // END: readme-sample-handleMultipleEventTypes
    }

    /**
     * Sample showing generic system event processing.
     */
    public void processSystemEventGenerically() {
        String eventJson = "{}"; // placeholder
        EventGridEvent event = EventGridEvent.fromString(eventJson).get(0);

        // BEGIN: readme-sample-processSystemEventGenerically
        // Process any system event generically
        String eventType = event.getEventType();
        Class<?> dataClass = SystemEventNames.getSystemEventMappings().get(eventType);

        if (dataClass != null) {
            // This is a known system event
            Object eventData = event.getData().toObject(dataClass);
            System.out.println("Processing " + eventType + " with data: " + eventData);
        } else {
            // Custom event or unknown system event
            System.out.println("Unknown event type: " + eventType);
        }
        // END: readme-sample-processSystemEventGenerically
    }

    /**
     * Sample from the original EventGrid main package showing system event processing.
     * This demonstrates compatibility with the main EventGrid SDK patterns.
     */
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

    /**
     * Sample showing how to deserialize event data to a specific system event type.
     * This is the legacy pattern from the main EventGrid package.
     */
    public void deserializeToSystemEventType() {
        String eventJson = "{}"; // placeholder
        EventGridEvent event = EventGridEvent.fromString(eventJson).get(0);

        // BEGIN: readme-sample-deserializeToSystemEventType
        // Deserialize the event data to an instance of a specific System Event data class type
        BinaryData data = event.getData();
        if (data != null) {
            StorageBlobCreatedEventData blobCreatedData = data.toObject(StorageBlobCreatedEventData.class);
            System.out.println(blobCreatedData.getUrl());
        }
        // END: readme-sample-deserializeToSystemEventType
    }
}
