// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;
import java.io.IOException;

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
    public void deserializeSystemEventData() throws IOException {

        // BEGIN: readme-sample-deserializeSystemEventData
        // Assuming you have an EventGridEvent from the main EventGrid SDK and the event is Storage Blob Created event
        StorageBlobCreatedEventData storageBlobCreatedEventData
            = StorageBlobCreatedEventData.fromJson(JsonProviders.createReader("payload"));
        BinaryData data = storageBlobCreatedEventData.getStorageDiagnostics().get("batchId");

        System.out.println("Blob URL: " + storageBlobCreatedEventData.getUrl());
        System.out.println("Blob size: " + storageBlobCreatedEventData.getContentLength());
        System.out.println("Content type: " + storageBlobCreatedEventData.getContentType());

        // END: readme-sample-deserializeSystemEventData
    }
}
