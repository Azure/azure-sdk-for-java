/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.eventgrid.customization;

import com.microsoft.azure.eventgrid.models.ContainerRegistryImageDeletedEventData;
import com.microsoft.azure.eventgrid.models.ContainerRegistryImagePushedEventData;
import com.microsoft.azure.eventgrid.models.EventHubCaptureFileCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceDeletedEventData;
import com.microsoft.azure.eventgrid.models.ResourceDeleteCancelData;
import com.microsoft.azure.eventgrid.models.ResourceDeleteFailureData;
import com.microsoft.azure.eventgrid.models.ResourceDeleteSuccessData;
import com.microsoft.azure.eventgrid.models.ResourceWriteCancelData;
import com.microsoft.azure.eventgrid.models.ResourceWriteFailureData;
import com.microsoft.azure.eventgrid.models.ResourceWriteSuccessData;
import com.microsoft.azure.eventgrid.models.ServiceBusActiveMessagesAvailableWithNoListenersEventData;
import com.microsoft.azure.eventgrid.models.ServiceBusDeadletterMessagesAvailableWithNoListenersEventData;
import com.microsoft.azure.eventgrid.models.StorageBlobCreatedEventData;
import com.microsoft.azure.eventgrid.models.StorageBlobDeletedEventData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of system event type name to corresponding type of the Java model.
 */
final class SystemEventTypeMappings {
    /**
     * The map containing system eventType to Java model type mapping.
     */
    private static Map<String, Type> systemEventMappings;

    static {
        systemEventMappings = new HashMap<>(); // key: eventType, Value:eventDataType
        // ContainerRegistry events
        systemEventMappings.put(SystemEventTypes.CONTAINER_REGISTRY_IMAGE_PUSHED_EVENT.toLowerCase(), ContainerRegistryImagePushedEventData.class);
        systemEventMappings.put(SystemEventTypes.CONTAINER_REGISTRY_IMAGE_DELETED_EVENT.toLowerCase(), ContainerRegistryImageDeletedEventData.class);
        // Device events
        systemEventMappings.put(SystemEventTypes.IOT_HUB_DEVICE_CREATED_EVENT.toLowerCase(), IotHubDeviceCreatedEventData.class);
        systemEventMappings.put(SystemEventTypes.IOT_HUB_DEVICE_DELETED_EVENT.toLowerCase(), IotHubDeviceDeletedEventData.class);
        // EventGrid events
        // TODO: Enable this once SDK is refreshed
        // systemEventMappings.put(SystemEventTypes.EVENT_GRID_SUBSCRIPTION_VALIDATION_EVENT.toLowerCase(), SubscriptionValidationEventData.class);
        // systemEventMappings.put(SystemEventTypes.EVENT_GRID_SUBSCRIPTION_DELETED_EVENT.toLowerCase(), SubscriptionDeletedEventData.class);
        // Event Hub Events
        systemEventMappings.put(SystemEventTypes.EVENT_HUB_CAPTURE_FILE_CREATED_EVENT.toLowerCase(), EventHubCaptureFileCreatedEventData.class);
        // Media Services events
        // TODO: Enable this once SDK is refreshed
        // systemEventMappings.put(SystemEventTypes.MEDIA_JOB_STATE_CHANGE_EVENT.toLowerCase(), MediaJobStateChangeEventData.class);
        // Resource Manager (Azure Subscription/Resource Group) events
        systemEventMappings.put(SystemEventTypes.RESOURCE_WRITE_SUCCESS_EVENT.toLowerCase(), ResourceWriteSuccessData.class);
        systemEventMappings.put(SystemEventTypes.RESOURCE_WRITE_FAILURE_EVENT.toLowerCase(), ResourceWriteFailureData.class);
        systemEventMappings.put(SystemEventTypes.RESOURCE_WRITE_CANCEL_EVENT.toLowerCase(), ResourceWriteCancelData.class);
        systemEventMappings.put(SystemEventTypes.RESOURCE_DELETE_SUCCESS_EVENT.toLowerCase(), ResourceDeleteSuccessData.class);
        systemEventMappings.put(SystemEventTypes.RESOURCE_DELETE_FAILURE_EVENT.toLowerCase(), ResourceDeleteFailureData.class);
        systemEventMappings.put(SystemEventTypes.RESOURCE_DELETE_CANCEL_EVENT.toLowerCase(), ResourceDeleteCancelData.class);
        // ServiceBus events
        systemEventMappings.put(SystemEventTypes.SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS_EVENT.toLowerCase(), ServiceBusActiveMessagesAvailableWithNoListenersEventData.class);
        systemEventMappings.put(SystemEventTypes.SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER_EVENT.toLowerCase(), ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class);
        // Storage events
        systemEventMappings.put(SystemEventTypes.STORAGE_BLOB_CREATED_EVENT.toLowerCase(), StorageBlobCreatedEventData.class);
        systemEventMappings.put(SystemEventTypes.STORAGE_BLOB_DELETED_EVENT.toLowerCase(), StorageBlobDeletedEventData.class);
    }

    /**
     * Checks a mapping exists for the given type.
     *
     * @param eventType the event type
     * @return true if mapping exists, false otherwise.
     */
    public static boolean mappingExists(final String eventType) {
        return systemEventMappings.containsKey(eventType.toLowerCase());
    }

    /**
     * Get Java model type for the given event type.
     *
     * @param eventType the event type
     * @return the Java model type if mapping exists, null otherwise.
     */
    public static Type getMapping(final String eventType) {
        if (!systemEventMappings.containsKey(eventType.toLowerCase())) {
            return null;
        }
        return systemEventMappings.get(eventType.toLowerCase());
    }
}
