// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid.customization;

import com.microsoft.azure.management.apigeneration.Beta;

/**
 *  Represents the names of the various event types for the system events published to Azure Event Grid.
 */
@Beta
public class SystemEventTypes {
    // Keep this sorted by the name of the service publishing the events.

    // AppConfiguration events.
    /**
     * indicate an event of KeyValueDeleted in AppConfiguration.
     */
    public static final String APP_CONFIGURATION_KEY_VALUE_DELETED_EVENT = "Microsoft.AppConfiguration.KeyValueDeleted";
    /**
     * indicate an event of KeyValueModified in AppConfiguration.
     */
    public static final String APP_CONFIGURATION_KEY_VALUE_MODIFIED_EVENT = "Microsoft.AppConfiguration.KeyValueModified";

    // ContainerRegistry events.
    /**
     * indicate an event of pushing an image to container registry.
     */
    public static final String CONTAINER_REGISTRY_IMAGE_PUSHED_EVENT = "Microsoft.ContainerRegistry.ImagePushed";
    /**
     * indicate an event of deleting an image from container registry.
     */
    public static final String CONTAINER_REGISTRY_IMAGE_DELETED_EVENT = "Microsoft.ContainerRegistry.ImageDeleted";
    /**
     * indicate an event of chart deletion in container registry.
     */
    public static final String CONTAINER_REGISTRY_CHART_DELETED_EVENT = "Microsoft.ContainerRegistry.ChartDeleted";
    /**
     * indicate an event of chart pushed in container registry.
     */
    public static final String CONTAINER_REGISTRY_CHART_PUSHED_EVENT = "Microsoft.ContainerRegistry.ChartPushed";

    // Device events.
    /**
     * indicate an event of creating an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_CREATED_EVENT = "Microsoft.Devices.DeviceCreated";
    /**
     * indicate an event of deleting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_DELETED_EVENT = "Microsoft.Devices.DeviceDeleted";
    /**
     * indicate an event of connecting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_CONNECTED_EVENT = "Microsoft.Devices.DeviceConnected";
    /**
     * indicate an event of disconnecting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_DISCONNECTED_EVENT = "Microsoft.Devices.DeviceDisconnected";
    /**
     * indicate an event of telemetry from an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_TELEMETRY_EVENT = "Microsoft.Devices.DeviceTelemetry";

    // EventGrid events.
    /**
     * indicate an event of validating eventgrid subscription.
     */
    public static final String EVENT_GRID_SUBSCRIPTION_VALIDATION_EVENT = "Microsoft.EventGrid.SubscriptionValidationEvent";
    /**
     * indicate an event of deleting eventgrid subscription.
     */
    public static final String EVENT_GRID_SUBSCRIPTION_DELETED_EVENT = "Microsoft.EventGrid.SubscriptionDeletedEvent";

    // Event Hub Events.
    /**
     * indicate an event of creation of capture file in eventhub.
     */
    public static final String EVENT_HUB_CAPTURE_FILE_CREATED_EVENT = "Microsoft.EventHub.CaptureFileCreated";

    // Maps Events.
    /**
     * Maps GeoFence Entered Event.
     */
    public static final String MAPS_GEOFENCE_ENTERED = "Microsoft.Maps.GeofenceEntered";

    /**
     * Maps GeoFence Exited Event.
     */
    public static final String MAPS_GEOFENCE_EXITED = "Microsoft.Maps.GeofenceExited";

    /**
     * Maps GeoFence Result Event.
     */
    public static final String MAPS_GEOFENCE_RESULT = "Microsoft.Maps.GeofenceResult";

    // Media Services events.
    /**
     * Media Services Job Canceled Event.
     */
    public static final String MEDIA_JOB_CANCELED_EVENT = "Microsoft.Media.JobCanceled";

    /**
     * Media Services Job Canceling Event.
     */
    public static final String MEDIA_JOB_CANCELING_EVENT = "Microsoft.Media.JobCanceling";

    /**
     * Media Services Job Errored event.
     */
    public static final String MEDIA_JOB_ERRORED_EVENT = "Microsoft.Media.JobErrored";

    /**
     * Media Services Job Finished event.
     */
    public static final String MEDIA_JOB_FINISHED_EVENT = "Microsoft.Media.JobFinished";

    /**
     * Media Services Job Ouput Canceled event.
     */
    public static final String MEDIA_JOB_OUTPUT_CANCELED_EVENT = "Microsoft.Media.JobOutputCanceled";

    /**
     * Media Services Job Output Canceling event.
     */
    public static final String MEDIA_JOB_OUTPUT_CANCELING_EVENT = "Microsoft.Media.JobOutputCanceling";

    /**
     * Media Services Job Output Errored event.
     */
    public static final String MEDIA_JOB_OUTPUT_ERRORED_EVENT = "Microsoft.Media.JobOutputErrored";

    /**
     * Media Services Job Output Finished event.
     */
    public static final String MEDIA_JOB_OUTPUT_FINISHED_EVENT = "Microsoft.Media.JobOutputFinished";

    /**
     * Media Services Job Output Processing event.
     */
    public static final String MEDIA_JOB_OUTPUT_PROCESSING_EVENT = "Microsoft.Media.JobOutputProcessing";

    /**
     * Media Services Job Output Progress event.
     */
    public static final String MEDIA_JOB_OUTPUT_PROGRESS_EVENT = "Microsoft.Media.JobOutputProgress";

    /**
     * Media Services Job Output Scheduled event.
     */
    public static final String MEDIA_JOB_OUTPUT_SCHEDULED_EVENT = "Microsoft.Media.JobOutputScheduled";

    /**
     * Media Services Job Output State Change event.
     */
    public static final String MEDIA_JOB_OUTPUT_STATE_CHANGE_EVENT = "Microsoft.Media.JobOutputStateChange";

    /**
     * Media Services Job Processing event.
     */
    public static final String MEDIA_JOB_PROCESSING_EVENT = "Microsoft.Media.JobProcessing";

    /**
     * Media Services Job Scheduled event.
     */
    public static final String MEDIA_JOB_SCHEDULED_EVENT = "Microsoft.Media.JobScheduled";

    /**
     * Media Services Job State Change event.
     */
    public static final String MEDIA_JOB_STATE_CHANGE_EVENT = "Microsoft.Media.JobStateChange";

    /**
     * Media Services Live Event Connection Rejected event.
     */
    public static final String MEDIA_LIVE_EVENT_CONNECTION_REJECTED_EVENT = "Microsoft.Media.LiveEventConnectionRejected";

    /**
     * Media Services Live Event Encoder Connected event.
     */
    public static final String MEDIA_LIVE_EVENT_ENCODER_CONNECTED_EVENT = "Microsoft.Media.LiveEventEncoderConnected";

    /**
     * Media Services Live Event Encoder Disconnected event.
     */
    public static final String MEDIA_LIVE_EVENT_ENCODER_DISCONNECTED_EVENT = "Microsoft.Media.LiveEventEncoderDisconnected";

    /**
     * Media Services Live Event Incoming Data Chunk Dropped event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_DATA_CHUNK_DROPPED_EVENT = "Microsoft.Media.LiveEventIncomingDataChunkDropped";

    /**
     * Media Services Live Event Incoming Stream Received event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_STREAM_RECEIVED_EVENT = "Microsoft.Media.LiveEventIncomingStreamReceived";

    /**
     * Media Services Live Event Incoming Streams OutofSync event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC_EVENT = "Microsoft.Media.LiveEventIncomingStreamsOutOfSync";

    /**
     * Media Services Live Event Incoming Video Streams OutOfSync event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC_EVENT = "Microsoft.Media.LiveEventIncomingVideoStreamsOutOfSync";

    /**
     * Media Services Live Event Ingest Heartbeat event.
     */
    public static final String MEDIA_LIVE_EVENT_INGEST_HEARTBEAT_EVENT = "Microsoft.Media.LiveEventIngestHeartbeat";

    /**
     * Media Services Live Event Track Discontinuity Detected event.
     */
    public static final String MEDIA_LIVE_EVENT_TRACK_DISCONTINUITY_DETECTED_EVENT = "Microsoft.Media.LiveEventTrackDiscontinuityDetected";


    // Resource Manager (Azure Subscription/Resource Group) events
    /**
     * indicate an event of successful write of a resource.
     */
    public static final String RESOURCE_WRITE_SUCCESS_EVENT = "Microsoft.Resources.ResourceWriteSuccess";
    /**
     * indicate an event of write failure of a resource.
     */
    public static final String RESOURCE_WRITE_FAILURE_EVENT = "Microsoft.Resources.ResourceWriteFailure";
    /**
     * indicate an event of write cancellation of a resource.
     */
    public static final String RESOURCE_WRITE_CANCEL_EVENT = "Microsoft.Resources.ResourceWriteCancel";
    /**
     * indicate an event of successful deletion of a resource.
     */
    public static final String RESOURCE_DELETE_SUCCESS_EVENT = "Microsoft.Resources.ResourceDeleteSuccess";
    /**
     * indicate an event of failure in deleting a resource.
     */
    public static final String RESOURCE_DELETE_FAILURE_EVENT = "Microsoft.Resources.ResourceDeleteFailure";
    /**
     * indicate an event of cancellation of resource deletion.
     */
    public static final String RESOURCE_DELETE_CANCEL_EVENT = "Microsoft.Resources.ResourceDeleteCancel";
    /**
     * indicate an event of successful action on a resource.
     */
    public static final String RESOURCE_ACTION_SUCCESS_EVENT = "Microsoft.Resources.ResourceActionSuccess";
    /**
     * indicate an event of failure in performing an action on a resource.
     */
    public static final String RESOURCE_ACTION_FAILURE_EVENT = "Microsoft.Resources.ResourceActionFailure";
    /**
     * indicate an event of cancellation of resource action.
     */
    public static final String RESOURCE_ACTION_CANCEL_EVENT = "Microsoft.Resources.ResourceActionCancel";

    // ServiceBus events.
    /**
     * indicate an event of active messages with no listener for them.
     */
    public static final String SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS_EVENT = "Microsoft.ServiceBus.ActiveMessagesAvailableWithNoListeners";
    /**
     * indicate an event of deadletter messages with no listener for them.
     */
    public static final String SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER_EVENT = "Microsoft.ServiceBus.DeadletterMessagesAvailableWithNoListener";

    // Storage events.
    /**
     * indicates an event of blob creation.
     */
    public static final String STORAGE_BLOB_CREATED_EVENT = "Microsoft.Storage.BlobCreated";
    /**
     * indicates an event of blob deletion.
     */
    public static final String STORAGE_BLOB_DELETED_EVENT = "Microsoft.Storage.BlobDeleted";
}
