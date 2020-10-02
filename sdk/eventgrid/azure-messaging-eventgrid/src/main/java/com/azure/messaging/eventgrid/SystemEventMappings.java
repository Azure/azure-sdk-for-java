// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.systemevents.AcsChatMemberAddedToThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMemberRemovedFromThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageEditedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadCreatedWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadPropertiesUpdatedPerUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadWithUserDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsSmsDeliveryReportReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsSmsReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueModifiedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartPushedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImageDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImagePushedEventData;
import com.azure.messaging.eventgrid.systemevents.EventHubCaptureFileCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceConnectedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceTelemetryEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultAccessPolicyChangedEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultCertificateExpiredEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultCertificateNearExpiryEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultCertificateNewVersionCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultKeyExpiredEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultKeyNearExpiryEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultKeyNewVersionCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultSecretExpiredEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultSecretNearExpiryEventData;
import com.azure.messaging.eventgrid.systemevents.KeyVaultSecretNewVersionCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.MachineLearningServicesDatasetDriftDetectedEventData;
import com.azure.messaging.eventgrid.systemevents.MachineLearningServicesModelDeployedEventData;
import com.azure.messaging.eventgrid.systemevents.MachineLearningServicesModelRegisteredEventData;
import com.azure.messaging.eventgrid.systemevents.MachineLearningServicesRunCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.MachineLearningServicesRunStatusChangedEventData;
import com.azure.messaging.eventgrid.systemevents.MapsGeofenceEnteredEventData;
import com.azure.messaging.eventgrid.systemevents.MapsGeofenceExitedEventData;
import com.azure.messaging.eventgrid.systemevents.MapsGeofenceResultEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobCanceledEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobCancelingEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobErroredEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobFinishedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputCanceledEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputCancelingEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputErroredEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputFinishedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputProcessingEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputProgressEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputScheduledEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputStateChangeEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobProcessingEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobScheduledEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobStateChangeEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventConnectionRejectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventEncoderConnectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventEncoderDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingDataChunkDroppedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingStreamReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingStreamsOutOfSyncEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingVideoStreamsOutOfSyncEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIngestHeartbeatEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventTrackDiscontinuityDetectedEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionCancelData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionFailureData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionSuccessData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteCancelData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteFailureData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteSuccessData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteCancelData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteFailureData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteSuccessData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusActiveMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusDeadletterMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.WebAppServicePlanUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.WebAppUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.WebBackupOperationCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.WebBackupOperationFailedEventData;
import com.azure.messaging.eventgrid.systemevents.WebBackupOperationStartedEventData;
import com.azure.messaging.eventgrid.systemevents.WebRestoreOperationCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.WebRestoreOperationFailedEventData;
import com.azure.messaging.eventgrid.systemevents.WebRestoreOperationStartedEventData;
import com.azure.messaging.eventgrid.systemevents.WebSlotSwapCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.WebSlotSwapFailedEventData;
import com.azure.messaging.eventgrid.systemevents.WebSlotSwapStartedEventData;
import com.azure.messaging.eventgrid.systemevents.WebSlotSwapWithPreviewCancelledEventData;
import com.azure.messaging.eventgrid.systemevents.WebSlotSwapWithPreviewStartedEventData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class contains a number of constants that correspond to the value of {@code eventType} of {@link EventGridEvent}s
 * and {@code type} of {@link CloudEvent}s, when the event originated from an Azure service. This list should be
 * updated with all the service event strings. It also contains a mapping from each service event string to the
 * model class that the event string corresponds to in the {@code data} field, which is used to automatically deserialize
 * system events by their known string.
 */
public final class SystemEventMappings {
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

    // Communication Services events.
    public static final String COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER =
        "Microsoft.Communication.ChatMemberAddedToThreadWithUser";
    public static final String COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER =
        "Microsoft.Communication.ChatMemberRemovedFromThreadWithUser";
    public static final String COMMUNICATION_CHAT_MESSAGE_DELETED =
        "Microsoft.Communication.ChatMessageDeleted";
    public static final String COMMUNICATION_CHAT_MESSAGE_EDITED =
        "Microsoft.Communication.ChatMessageEdited";
    public static final String COMMUNICATION_CHAT_MESSAGE_RECEIVED =
        "Microsoft.Communication.ChatMessageReceived";
    public static final String COMMUNICATION_CHAT_THREAD_CREATED_WITH_USER =
        "Microsoft.Communication.ChatThreadCreatedWithUser";
    public static final String COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED_PER_USER =
        "Microsoft.Communication.ChatThreadPropertiesUpdatedPerUser";
    public static final String COMMUNICATION_CHAT_THREAD_WITH_USER_DELETED =
        "Microsoft.Communication.ChatThreadWithUserDeleted";
    public static final String COMMUNICATION_SMS_DELIVERY_REPORT_RECEIVED =
        "Microsoft.Communication.SMSDeliveryReportReceived";
    public static final String COMMUNICATION_SMS_RECEIVED =
        "Microsoft.Communication.SMSReceived";

    // Web
    public static final String WEB_APP_UPDATED = "Microsoft.Web.AppUpdated";
    public static final String WEB_BACKUP_OPERATION_STARTED = "Microsoft.Web.BackupOperationStarted";
    public static final String WEB_BACKUP_OPERATION_COMPLETED = "Microsoft.Web.BackupOperationCompleted";
    public static final String WEB_BACKUP_OPERATION_FAILED = "Microsoft.Web.BackupOperationFailed";
    public static final String WEB_RESTORE_OPERATION_STARTED = "Microsoft.Web.RestoreOperationStarted";
    public static final String WEB_RESTORE_OPERATION_COMPLETED = "Microsoft.Web.RestoreOperationCompleted";
    public static final String WEB_RESTORE_OPERATION_FAILED = "Microsoft.Web.RestoreOperationFailed";
    public static final String WEB_SLOT_SWAP_STARTED = "Microsoft.Web.SlotSwapStarted";
    public static final String WEB_SLOT_SWAP_COMPLETED = "Microsoft.Web.SlotSwapCompleted";
    public static final String WEB_SLOT_SWAP_FAILED = "Microsoft.Web.SlotSwapFailed";
    public static final String WEB_SLOT_SWAP_WITH_PREVIEW_STARTED = "Microsoft.Web.SlotSwapWithPreviewStarted";
    public static final String WEB_SLOT_SWAP_WITH_PREVIEW_CANCELLED = "Microsoft.Web.SlotSwapWithPreviewCancelled";
    public static final String WEB_APP_SERVICE_PLAN_UPDATED = "Microsoft.Web.AppServicePlanUpdated";

    // Machine Learning
    public static final String MACHINE_LEARNING_DATASET_DRIFT_DETECTED = "Microsoft.MachineLearningServices.DatasetDriftDetected";
    public static final String MACHINE_LEARNING_MODEL_DEPLOYED = "Microsoft.MachineLearningServices.ModelDeployed";
    public static final String MACHINE_LEARNING_MODEL_REGISTERED = "Microsoft.MachineLearningServices.ModelRegistered";
    public static final String MACHINE_LEARNING_RUN_COMPLETED = "Microsoft.MachineLearningServices.RunCompleted";
    public static final String MACHINE_LEARNING_RUN_STATUS_CHANGED = "Microsoft.MachineLearningServices.RunStatusChanged";

    // Key Vault
    public static final String KEY_VAULT_CERTIFICATE_NEW_VERSION_CREATED = "Microsoft.KeyVault.CertificateNewVersionCreated";
    public static final String KEY_VAULT_CERTIFICATE_NEAR_EXPIRY = "Microsoft.KeyVault.CertificateNearExpiry";
    public static final String KEY_VAULT_CERTIFICATE_EXPIRED = "Microsoft.KeyVault.CertificateExpired";
    public static final String KEY_VAULT_KEY_NEW_VERSION_CREATED = "Microsoft.KeyVault.KeyNewVersionCreated";
    public static final String KEY_VAULT_KEY_NEAR_EXPIRY = "Microsoft.KeyVault.KeyNearExpiry";
    public static final String KEY_VAULT_KEY_EXPIRED = "Microsoft.KeyVault.KeyExpired";
    public static final String KEY_VAULT_SECRET_NEW_VERSION_CREATED = "Microsoft.KeyVault.SecretNewVersionCreated";
    public static final String KEY_VAULT_SECRET_NEAR_EXPIRY = "Microsoft.KeyVault.SecretNearExpiry";
    public static final String KEY_VAULT_SECRET_EXPIRED = "Microsoft.KeyVault.SecretExpired";
    public static final String KEY_VAULT_VAULT_ACCESS_POLICY_CHANGED = "Microsoft.KeyVault.VaultAccessPolicyChanged";

    //TODO: When a new service adds an event, add a constant above and a mapping to the corresponding data class below.

    private static final Map<String, Class<?>> systemEventMappings = new HashMap<String, Class<?>>() {{
        //
        // AppConfiguration events.
        put(canonicalizeEventType(APP_CONFIGURATION_KEY_VALUE_DELETED_EVENT), AppConfigurationKeyValueDeletedEventData.class);
        put(canonicalizeEventType(APP_CONFIGURATION_KEY_VALUE_MODIFIED_EVENT), AppConfigurationKeyValueModifiedEventData.class);
        //
        // ContainerRegistry events.
        put(canonicalizeEventType(CONTAINER_REGISTRY_IMAGE_PUSHED_EVENT), ContainerRegistryImagePushedEventData.class);
        put(canonicalizeEventType(CONTAINER_REGISTRY_IMAGE_DELETED_EVENT), ContainerRegistryImageDeletedEventData.class);
        put(canonicalizeEventType(CONTAINER_REGISTRY_CHART_DELETED_EVENT), ContainerRegistryChartDeletedEventData.class);
        put(canonicalizeEventType(CONTAINER_REGISTRY_CHART_PUSHED_EVENT), ContainerRegistryChartPushedEventData.class);
        //
        // Device events.
        put(canonicalizeEventType(IOT_HUB_DEVICE_CREATED_EVENT), IotHubDeviceCreatedEventData.class);
        put(canonicalizeEventType(IOT_HUB_DEVICE_DELETED_EVENT), IotHubDeviceDeletedEventData.class);
        put(canonicalizeEventType(IOT_HUB_DEVICE_CONNECTED_EVENT), IotHubDeviceConnectedEventData.class);
        put(canonicalizeEventType(IOT_HUB_DEVICE_DISCONNECTED_EVENT), IotHubDeviceDisconnectedEventData.class);
        put(canonicalizeEventType(IOT_HUB_DEVICE_TELEMETRY_EVENT), IotHubDeviceTelemetryEventData.class);
        //
        // EventGrid events.
        put(canonicalizeEventType(EVENT_GRID_SUBSCRIPTION_VALIDATION_EVENT), SubscriptionValidationEventData.class);
        put(canonicalizeEventType(EVENT_GRID_SUBSCRIPTION_DELETED_EVENT), SubscriptionDeletedEventData.class);
        //
        // Event Hub Events.
        put(canonicalizeEventType(EVENT_HUB_CAPTURE_FILE_CREATED_EVENT), EventHubCaptureFileCreatedEventData.class);
        // Maps events
        put(canonicalizeEventType(MAPS_GEOFENCE_ENTERED), MapsGeofenceEnteredEventData.class);
        put(canonicalizeEventType(MAPS_GEOFENCE_EXITED), MapsGeofenceExitedEventData.class);
        put(canonicalizeEventType(MAPS_GEOFENCE_RESULT), MapsGeofenceResultEventData.class);
        //
        // Media Services events.
        put(canonicalizeEventType(MEDIA_JOB_CANCELED_EVENT), MediaJobCanceledEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_CANCELING_EVENT), MediaJobCancelingEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_ERRORED_EVENT), MediaJobErroredEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_FINISHED_EVENT), MediaJobFinishedEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_CANCELED_EVENT), MediaJobOutputCanceledEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_CANCELING_EVENT), MediaJobOutputCancelingEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_ERRORED_EVENT), MediaJobOutputErroredEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_FINISHED_EVENT), MediaJobOutputFinishedEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_PROCESSING_EVENT), MediaJobOutputProcessingEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_PROGRESS_EVENT), MediaJobOutputProgressEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_SCHEDULED_EVENT), MediaJobOutputScheduledEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_OUTPUT_STATE_CHANGE_EVENT), MediaJobOutputStateChangeEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_PROCESSING_EVENT), MediaJobProcessingEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_SCHEDULED_EVENT), MediaJobScheduledEventData.class);
        put(canonicalizeEventType(MEDIA_JOB_STATE_CHANGE_EVENT), MediaJobStateChangeEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_CONNECTION_REJECTED_EVENT), MediaLiveEventConnectionRejectedEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_ENCODER_CONNECTED_EVENT), MediaLiveEventEncoderConnectedEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_ENCODER_DISCONNECTED_EVENT), MediaLiveEventEncoderDisconnectedEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_INCOMING_DATA_CHUNK_DROPPED_EVENT), MediaLiveEventIncomingDataChunkDroppedEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC_EVENT), MediaLiveEventIncomingStreamsOutOfSyncEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_INCOMING_STREAM_RECEIVED_EVENT), MediaLiveEventIncomingStreamReceivedEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC_EVENT), MediaLiveEventIncomingVideoStreamsOutOfSyncEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_INGEST_HEARTBEAT_EVENT), MediaLiveEventIngestHeartbeatEventData.class);
        put(canonicalizeEventType(MEDIA_LIVE_EVENT_TRACK_DISCONTINUITY_DETECTED_EVENT), MediaLiveEventTrackDiscontinuityDetectedEventData.class);
        //
        // Resource Manager (Azure Subscription/Resource Group) events.
        put(canonicalizeEventType(RESOURCE_WRITE_SUCCESS_EVENT), ResourceWriteSuccessData.class);
        put(canonicalizeEventType(RESOURCE_WRITE_FAILURE_EVENT), ResourceWriteFailureData.class);
        put(canonicalizeEventType(RESOURCE_WRITE_CANCEL_EVENT), ResourceWriteCancelData.class);
        put(canonicalizeEventType(RESOURCE_DELETE_SUCCESS_EVENT), ResourceDeleteSuccessData.class);
        put(canonicalizeEventType(RESOURCE_DELETE_FAILURE_EVENT), ResourceDeleteFailureData.class);
        put(canonicalizeEventType(RESOURCE_DELETE_CANCEL_EVENT), ResourceDeleteCancelData.class);
        put(canonicalizeEventType(RESOURCE_ACTION_SUCCESS_EVENT), ResourceActionSuccessData.class);
        put(canonicalizeEventType(RESOURCE_ACTION_FAILURE_EVENT), ResourceActionFailureData.class);
        put(canonicalizeEventType(RESOURCE_ACTION_CANCEL_EVENT), ResourceActionCancelData.class);
        //
        // ServiceBus events.
        put(canonicalizeEventType(SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS_EVENT), ServiceBusActiveMessagesAvailableWithNoListenersEventData.class);
        put(canonicalizeEventType(SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER_EVENT), ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class);
        //
        // Storage events.
        put(canonicalizeEventType(STORAGE_BLOB_CREATED_EVENT), StorageBlobCreatedEventData.class);
        put(canonicalizeEventType(STORAGE_BLOB_DELETED_EVENT), StorageBlobDeletedEventData.class);

        // Communication service events.
        put(canonicalizeEventType(COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER), AcsChatMemberAddedToThreadWithUserEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER), AcsChatMemberRemovedFromThreadWithUserEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_MESSAGE_DELETED), AcsChatMessageDeletedEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_MESSAGE_EDITED), AcsChatMessageEditedEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_MESSAGE_RECEIVED), AcsChatMessageReceivedEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_THREAD_CREATED_WITH_USER), AcsChatThreadCreatedWithUserEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED_PER_USER), AcsChatThreadPropertiesUpdatedPerUserEventData.class);
        put(canonicalizeEventType(COMMUNICATION_CHAT_THREAD_WITH_USER_DELETED), AcsChatThreadWithUserDeletedEventData.class);
        put(canonicalizeEventType(COMMUNICATION_SMS_DELIVERY_REPORT_RECEIVED), AcsSmsDeliveryReportReceivedEventData.class);
        put(canonicalizeEventType(COMMUNICATION_SMS_RECEIVED), AcsSmsReceivedEventData.class);

        // Web events
        put(canonicalizeEventType(WEB_APP_UPDATED), WebAppUpdatedEventData.class);
        put(canonicalizeEventType(WEB_BACKUP_OPERATION_STARTED), WebBackupOperationStartedEventData.class);
        put(canonicalizeEventType(WEB_BACKUP_OPERATION_COMPLETED), WebBackupOperationCompletedEventData.class);
        put(canonicalizeEventType(WEB_BACKUP_OPERATION_FAILED), WebBackupOperationFailedEventData.class);
        put(canonicalizeEventType(WEB_RESTORE_OPERATION_STARTED), WebRestoreOperationStartedEventData.class);
        put(canonicalizeEventType(WEB_RESTORE_OPERATION_COMPLETED), WebRestoreOperationCompletedEventData.class);
        put(canonicalizeEventType(WEB_RESTORE_OPERATION_FAILED), WebRestoreOperationFailedEventData.class);
        put(canonicalizeEventType(WEB_SLOT_SWAP_STARTED), WebSlotSwapStartedEventData.class);
        put(canonicalizeEventType(WEB_SLOT_SWAP_COMPLETED), WebSlotSwapCompletedEventData.class);
        put(canonicalizeEventType(WEB_SLOT_SWAP_FAILED), WebSlotSwapFailedEventData.class);
        put(canonicalizeEventType(WEB_SLOT_SWAP_WITH_PREVIEW_STARTED), WebSlotSwapWithPreviewStartedEventData.class);
        put(canonicalizeEventType(WEB_SLOT_SWAP_WITH_PREVIEW_CANCELLED), WebSlotSwapWithPreviewCancelledEventData.class);
        put(canonicalizeEventType(WEB_APP_SERVICE_PLAN_UPDATED), WebAppServicePlanUpdatedEventData.class);

        // Machine Learning events
        put(canonicalizeEventType(MACHINE_LEARNING_DATASET_DRIFT_DETECTED), MachineLearningServicesDatasetDriftDetectedEventData.class);
        put(canonicalizeEventType(MACHINE_LEARNING_MODEL_DEPLOYED), MachineLearningServicesModelDeployedEventData.class);
        put(canonicalizeEventType(MACHINE_LEARNING_MODEL_REGISTERED), MachineLearningServicesModelRegisteredEventData.class);
        put(canonicalizeEventType(MACHINE_LEARNING_RUN_COMPLETED), MachineLearningServicesRunCompletedEventData.class);
        put(canonicalizeEventType(MACHINE_LEARNING_RUN_STATUS_CHANGED), MachineLearningServicesRunStatusChangedEventData.class);

        // Key Vault events
        put(canonicalizeEventType(KEY_VAULT_CERTIFICATE_NEW_VERSION_CREATED), KeyVaultCertificateNewVersionCreatedEventData.class);
        put(canonicalizeEventType(KEY_VAULT_CERTIFICATE_NEAR_EXPIRY), KeyVaultCertificateNearExpiryEventData.class);
        put(canonicalizeEventType(KEY_VAULT_CERTIFICATE_EXPIRED), KeyVaultCertificateExpiredEventData.class);
        put(canonicalizeEventType(KEY_VAULT_KEY_NEW_VERSION_CREATED), KeyVaultKeyNewVersionCreatedEventData.class);
        put(canonicalizeEventType(KEY_VAULT_KEY_NEAR_EXPIRY), KeyVaultKeyNearExpiryEventData.class);
        put(canonicalizeEventType(KEY_VAULT_KEY_EXPIRED), KeyVaultKeyExpiredEventData.class);
        put(canonicalizeEventType(KEY_VAULT_SECRET_NEW_VERSION_CREATED), KeyVaultSecretNewVersionCreatedEventData.class);
        put(canonicalizeEventType(KEY_VAULT_SECRET_NEAR_EXPIRY), KeyVaultSecretNearExpiryEventData.class);
        put(canonicalizeEventType(KEY_VAULT_SECRET_EXPIRED), KeyVaultSecretExpiredEventData.class);
        put(canonicalizeEventType(KEY_VAULT_VAULT_ACCESS_POLICY_CHANGED), KeyVaultAccessPolicyChangedEventData.class);
    }};

    /**
     * Turn a given event type string into it's canonical string, used to convert strings
     * when they may have been changed to upper/lower case.
     * @param eventType the string to canonicalize.
     *
     * @return the canonicalized version.
     */
    public static String canonicalizeEventType(String eventType) {
        if (eventType == null) {
            return null;
        } else {
            return eventType.toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * Get a mapping of all the system event type strings to their respective class. This is used by default in
     * the {@link EventGridEvent} and {@link CloudEvent} classes.
     * @return a mapping of all the system event strings to system event objects.
     */
    public static Map<String, Class<?>> getSystemEventMappings() {
        return Collections.unmodifiableMap(systemEventMappings);
    }

    private SystemEventMappings() {
    }
}
