// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid.customization;

import com.microsoft.azure.eventgrid.models.AppConfigurationKeyValueDeletedEventData;
import com.microsoft.azure.eventgrid.models.AppConfigurationKeyValueModifiedEventData;
import com.microsoft.azure.eventgrid.models.ContainerRegistryChartDeletedEventData;
import com.microsoft.azure.eventgrid.models.ContainerRegistryChartPushedEventData;
import com.microsoft.azure.eventgrid.models.ContainerRegistryImageDeletedEventData;
import com.microsoft.azure.eventgrid.models.ContainerRegistryImagePushedEventData;
import com.microsoft.azure.eventgrid.models.EventHubCaptureFileCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceConnectedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceDeletedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceDisconnectedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceTelemetryEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceEnteredEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceResultEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceExitedEventData;
import com.microsoft.azure.eventgrid.models.MediaJobCanceledEventData;
import com.microsoft.azure.eventgrid.models.MediaJobCancelingEventData;
import com.microsoft.azure.eventgrid.models.MediaJobErroredEventData;
import com.microsoft.azure.eventgrid.models.MediaJobFinishedEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputCanceledEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputCancelingEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputErroredEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputFinishedEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputProcessingEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputProgressEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputScheduledEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputStateChangeEventData;
import com.microsoft.azure.eventgrid.models.MediaJobProcessingEventData;
import com.microsoft.azure.eventgrid.models.MediaJobScheduledEventData;
import com.microsoft.azure.eventgrid.models.MediaJobStateChangeEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventConnectionRejectedEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventEncoderConnectedEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventEncoderDisconnectedEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventIncomingDataChunkDroppedEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventIncomingStreamReceivedEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventIncomingStreamsOutOfSyncEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventIncomingVideoStreamsOutOfSyncEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventIngestHeartbeatEventData;
import com.microsoft.azure.eventgrid.models.MediaLiveEventTrackDiscontinuityDetectedEventData;
import com.microsoft.azure.eventgrid.models.ResourceActionCancelData;
import com.microsoft.azure.eventgrid.models.ResourceActionFailureData;
import com.microsoft.azure.eventgrid.models.ResourceActionSuccessData;
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
import com.microsoft.azure.eventgrid.models.SubscriptionDeletedEventData;
import com.microsoft.azure.eventgrid.models.SubscriptionValidationEventData;
import com.microsoft.azure.management.apigeneration.Beta;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mapping of system event type name to corresponding type of the Java model.
 */
@Beta
final class SystemEventTypeMappings {
    /**
     * The map containing system eventType to Java model type mapping.
     */
    private static Map<String, Type> systemEventMappings;

    static {
        systemEventMappings = new HashMap<>(); // key: eventType, Value:eventDataType

        //
        // AppConfiguration events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.APP_CONFIGURATION_KEY_VALUE_DELETED_EVENT), AppConfigurationKeyValueDeletedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.APP_CONFIGURATION_KEY_VALUE_MODIFIED_EVENT), AppConfigurationKeyValueModifiedEventData.class);
        //
        // ContainerRegistry events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.CONTAINER_REGISTRY_IMAGE_PUSHED_EVENT), ContainerRegistryImagePushedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.CONTAINER_REGISTRY_IMAGE_DELETED_EVENT), ContainerRegistryImageDeletedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.CONTAINER_REGISTRY_CHART_DELETED_EVENT), ContainerRegistryChartDeletedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.CONTAINER_REGISTRY_CHART_PUSHED_EVENT), ContainerRegistryChartPushedEventData.class);
        //
        // Device events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.IOT_HUB_DEVICE_CREATED_EVENT), IotHubDeviceCreatedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.IOT_HUB_DEVICE_DELETED_EVENT), IotHubDeviceDeletedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.IOT_HUB_DEVICE_CONNECTED_EVENT), IotHubDeviceConnectedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.IOT_HUB_DEVICE_DISCONNECTED_EVENT), IotHubDeviceDisconnectedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.IOT_HUB_DEVICE_TELEMETRY_EVENT), IotHubDeviceTelemetryEventData.class);
        //
        // EventGrid events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.EVENT_GRID_SUBSCRIPTION_VALIDATION_EVENT), SubscriptionValidationEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.EVENT_GRID_SUBSCRIPTION_DELETED_EVENT), SubscriptionDeletedEventData.class);
        //
        // Event Hub Events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.EVENT_HUB_CAPTURE_FILE_CREATED_EVENT), EventHubCaptureFileCreatedEventData.class);
        // Maps events
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MAPS_GEOFENCE_ENTERED), MapsGeofenceEnteredEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MAPS_GEOFENCE_EXITED), MapsGeofenceExitedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MAPS_GEOFENCE_RESULT), MapsGeofenceResultEventData.class);
        //
        // Media Services events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_CANCELED_EVENT), MediaJobCanceledEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_CANCELING_EVENT), MediaJobCancelingEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_ERRORED_EVENT), MediaJobErroredEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_FINISHED_EVENT), MediaJobFinishedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_CANCELED_EVENT), MediaJobOutputCanceledEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_CANCELING_EVENT), MediaJobOutputCancelingEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_ERRORED_EVENT), MediaJobOutputErroredEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_FINISHED_EVENT), MediaJobOutputFinishedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_PROCESSING_EVENT), MediaJobOutputProcessingEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_PROGRESS_EVENT), MediaJobOutputProgressEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_SCHEDULED_EVENT), MediaJobOutputScheduledEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_OUTPUT_STATE_CHANGE_EVENT), MediaJobOutputStateChangeEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_PROCESSING_EVENT), MediaJobProcessingEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_SCHEDULED_EVENT), MediaJobScheduledEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_JOB_STATE_CHANGE_EVENT), MediaJobStateChangeEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_CONNECTION_REJECTED_EVENT), MediaLiveEventConnectionRejectedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_ENCODER_CONNECTED_EVENT), MediaLiveEventEncoderConnectedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_ENCODER_DISCONNECTED_EVENT), MediaLiveEventEncoderDisconnectedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_INCOMING_DATA_CHUNK_DROPPED_EVENT), MediaLiveEventIncomingDataChunkDroppedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC_EVENT), MediaLiveEventIncomingStreamsOutOfSyncEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_INCOMING_STREAM_RECEIVED_EVENT), MediaLiveEventIncomingStreamReceivedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC_EVENT), MediaLiveEventIncomingVideoStreamsOutOfSyncEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_INGEST_HEARTBEAT_EVENT), MediaLiveEventIngestHeartbeatEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.MEDIA_LIVE_EVENT_TRACK_DISCONTINUITY_DETECTED_EVENT), MediaLiveEventTrackDiscontinuityDetectedEventData.class);
        //
        // Resource Manager (Azure Subscription/Resource Group) events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_WRITE_SUCCESS_EVENT), ResourceWriteSuccessData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_WRITE_FAILURE_EVENT), ResourceWriteFailureData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_WRITE_CANCEL_EVENT), ResourceWriteCancelData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_DELETE_SUCCESS_EVENT), ResourceDeleteSuccessData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_DELETE_FAILURE_EVENT), ResourceDeleteFailureData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_DELETE_CANCEL_EVENT), ResourceDeleteCancelData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_ACTION_SUCCESS_EVENT), ResourceActionSuccessData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_ACTION_FAILURE_EVENT), ResourceActionFailureData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.RESOURCE_ACTION_CANCEL_EVENT), ResourceActionCancelData.class);
        //
        // ServiceBus events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS_EVENT), ServiceBusActiveMessagesAvailableWithNoListenersEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER_EVENT), ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class);
        //
        // Storage events.
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.STORAGE_BLOB_CREATED_EVENT), StorageBlobCreatedEventData.class);
        systemEventMappings.put(canonicalizeEventType(SystemEventTypes.STORAGE_BLOB_DELETED_EVENT), StorageBlobDeletedEventData.class);
    }

    /**
     * Checks if a mapping exists for the given type.
     *
     * @param eventType the event type.
     * @return true if mapping exists, false otherwise.
     */
    @Beta
    public static boolean containsMappingFor(final String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return false;
        } else {
            return systemEventMappings.containsKey(canonicalizeEventType(eventType));
        }
    }

    /**
     * Get Java model type for the given event type.
     *
     * @param eventType the event type.
     * @return the Java model type if mapping exists, null otherwise.
     */
    @Beta
    public static Type getMapping(final String eventType) {
        if (!containsMappingFor(eventType)) {
            return null;
        } else {
            return systemEventMappings.get(canonicalizeEventType(eventType));
        }
    }

    private static String canonicalizeEventType(final String eventType) {
        if (eventType == null) {
            return null;
        } else {
            return eventType.toLowerCase(Locale.ROOT);
        }
    }
}
