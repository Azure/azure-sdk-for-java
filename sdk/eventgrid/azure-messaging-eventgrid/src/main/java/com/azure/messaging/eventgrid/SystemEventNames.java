// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.messaging.eventgrid.systemevents.AcsChatMemberAddedToThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMemberRemovedFromThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageDeletedInThreadEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageEditedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageEditedInThreadEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatMessageReceivedInThreadEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatParticipantAddedToThreadEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatParticipantAddedToThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatParticipantRemovedFromThreadEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatParticipantRemovedFromThreadWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadCreatedWithUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadPropertiesUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadPropertiesUpdatedPerUserEventData;
import com.azure.messaging.eventgrid.systemevents.AcsChatThreadWithUserDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsRecordingFileStatusUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsSmsDeliveryReportReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsSmsReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.AcsUserDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiReleaseCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiReleaseDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiReleaseUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementApiUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementProductCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementProductDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementProductUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementSubscriptionCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementSubscriptionDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementSubscriptionUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementUserCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementUserDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ApiManagementUserUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueModifiedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartPushedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImageDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImagePushedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerServiceNewKubernetesVersionAvailableEventData;
import com.azure.messaging.eventgrid.systemevents.EventHubCaptureFileCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceUpdatedEventData;
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
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventChannelArchiveHeartbeatEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventConnectionRejectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventEncoderConnectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventEncoderDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingDataChunkDroppedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingStreamReceivedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingStreamsOutOfSyncEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIncomingVideoStreamsOutOfSyncEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventIngestHeartbeatEventData;
import com.azure.messaging.eventgrid.systemevents.MediaLiveEventTrackDiscontinuityDetectedEventData;
import com.azure.messaging.eventgrid.systemevents.PolicyInsightsPolicyStateChangedEventData;
import com.azure.messaging.eventgrid.systemevents.PolicyInsightsPolicyStateCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.PolicyInsightsPolicyStateDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.RedisExportRdbCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.RedisImportRdbCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.RedisPatchingCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.RedisScalingCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusActiveMessagesAvailablePeriodicNotificationsEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusActiveMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusDeadletterMessagesAvailablePeriodicNotificationsEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusDeadletterMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.SignalRServiceClientConnectionConnectedEventData;
import com.azure.messaging.eventgrid.systemevents.SignalRServiceClientConnectionDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageAsyncOperationInitiatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobInventoryPolicyCompletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobRenamedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobTierChangedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryRenamedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageLifecyclePolicyCompletedEventData;
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
import java.util.Map;
import com.azure.core.models.CloudEvent;

/**
 * This class contains a number of constants that correspond to the value of {@code eventType} of {@link EventGridEvent}s
 * and {@code type} of {@link CloudEvent}s, when the event originated from an Azure service. This list should be
 * updated with all the service event strings. It also contains a mapping from each service event string to the
 * model class that the event string corresponds to in the {@code data} field, which is used to automatically deserialize
 * system events by their known string.
 */
public final class SystemEventNames {
    // Keep this sorted by the name of the service publishing the events.

    // ApiManagement events.
    /**
     * indicate an event of ApiCreated in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_CREATED = "Microsoft.ApiManagement.ApiCreated";
    /**
     * indicate an event of ApiDeleted in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_DELETED = "Microsoft.ApiManagement.ApiDeleted";
    /**
     * indicate an event of ApiReleaseCreated in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_RELEASE_CREATED = "Microsoft.ApiManagement.ApiReleaseCreated";
    /**
     * indicate an event of ApiReleaseDeleted in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_RELEASE_DELETED = "Microsoft.ApiManagement.ApiReleaseDeleted";
    /**
     * indicate an event of ApiReleaseUpdated in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_RELEASE_UPDATED = "Microsoft.ApiManagement.ApiReleaseUpdated";
    /**
     * indicate an event of ApiUpdated in ApiManagement.
     */
    public static final String API_MANAGEMENT_API_UPDATED = "Microsoft.ApiManagement.ApiUpdated";
    /**
     * indicate an event of ProductCreated in ApiManagement.
     */
    public static final String API_MANAGEMENT_PRODUCT_CREATED = "Microsoft.ApiManagement.ProductCreated";
    /**
     * indicate an event of ProductDeleted in ApiManagement.
     */
    public static final String API_MANAGEMENT_PRODUCT_DELETED = "Microsoft.ApiManagement.ProductDeleted";
    /**
     * indicate an event of ProductUpdated in ApiManagement.
     */
    public static final String API_MANAGEMENT_PRODUCT_UPDATED = "Microsoft.ApiManagement.ProductUpdated";
    /**
     * indicate an event of SubscriptionCreated in ApiManagement.
     */
    public static final String API_MANAGEMENT_SUBSCRIPTION_CREATED = "Microsoft.ApiManagement.SubscriptionCreated";
    /**
     * indicate an event of SubscriptionDeleted in ApiManagement.
     */
    public static final String API_MANAGEMENT_SUBSCRIPTION_DELETED = "Microsoft.ApiManagement.SubscriptionDeleted";
    /**
     * indicate an event of SubscriptionUpdated in ApiManagement.
     */
    public static final String API_MANAGEMENT_SUBSCRIPTION_UPDATED = "Microsoft.ApiManagement.SubscriptionUpdated";
    /**
     * indicate an event of UserCreated in ApiManagement.
     */
    public static final String API_MANAGEMENT_USER_CREATED = "Microsoft.ApiManagement.UserCreated";
    /**
     * indicate an event of UserDeleted in ApiManagement.
     */
    public static final String API_MANAGEMENT_USER_DELETED = "Microsoft.ApiManagement.UserDeleted";
    /**
     * indicate an event of UserUpdated in ApiManagement.
     */
    public static final String API_MANAGEMENT_USER_UPDATED = "Microsoft.ApiManagement.UserUpdated";

    // AppConfiguration events.
    /**
     * indicate an event of KeyValueDeleted in AppConfiguration.
     */
    public static final String APP_CONFIGURATION_KEY_VALUE_DELETED = "Microsoft.AppConfiguration.KeyValueDeleted";
    /**
     * indicate an event of KeyValueModified in AppConfiguration.
     */
    public static final String APP_CONFIGURATION_KEY_VALUE_MODIFIED = "Microsoft.AppConfiguration.KeyValueModified";

    // ContainerRegistry events.
    /**
     * indicate an event of pushing an image to container registry.
     */
    public static final String CONTAINER_REGISTRY_IMAGE_PUSHED = "Microsoft.ContainerRegistry.ImagePushed";
    /**
     * indicate an event of deleting an image from container registry.
     */
    public static final String CONTAINER_REGISTRY_IMAGE_DELETED = "Microsoft.ContainerRegistry.ImageDeleted";
    /**
     * indicate an event of chart deletion in container registry.
     */
    public static final String CONTAINER_REGISTRY_CHART_DELETED = "Microsoft.ContainerRegistry.ChartDeleted";
    /**
     * indicate an event of chart pushed in container registry.
     */
    public static final String CONTAINER_REGISTRY_CHART_PUSHED = "Microsoft.ContainerRegistry.ChartPushed";

    // Healthcare Events
    /** Schema of the Data property of an EventGridEvent for a Microsoft.HealthcareApis.FhirResourceCreated event. */
    public static final String HEALTHCARE_FHIR_RESOURCE_CREATED = "Microsoft.HealthcareApis.FhirResourceCreated";

    /** Schema of the Data property of an EventGridEvent for a Microsoft.HealthcareApis.FhirResourceDeleted event. */
    public static final String HEALTHCARE_FHIR_RESOURCE_DELETED = "Microsoft.HealthcareApis.FhirResourceDeleted";

    /** Schema of the Data property of an EventGridEvent for a Microsoft.HealthcareApis.FhirResourceUpdated event. */
    public static final String HEALTHCARE_FHIR_RESOURCE_UPDATED = "Microsoft.HealthcareApis.FhirResourceUpdated";

    // Device events.
    /**
     * indicate an event of creating an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_CREATED = "Microsoft.Devices.DeviceCreated";
    /**
     * indicate an event of deleting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_DELETED = "Microsoft.Devices.DeviceDeleted";
    /**
     * indicate an event of connecting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_CONNECTED = "Microsoft.Devices.DeviceConnected";
    /**
     * indicate an event of disconnecting an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_DISCONNECTED = "Microsoft.Devices.DeviceDisconnected";
    /**
     * indicate an event of telemetry from an IoT hub device.
     */
    public static final String IOT_HUB_DEVICE_TELEMETRY = "Microsoft.Devices.DeviceTelemetry";

    // EventGrid events.
    /**
     * indicate an event of validating eventgrid subscription.
     */
    public static final String EVENT_GRID_SUBSCRIPTION_VALIDATION = "Microsoft.EventGrid.SubscriptionValidationEvent";
    /**
     * indicate an event of deleting eventgrid subscription.
     */
    public static final String EVENT_GRID_SUBSCRIPTION_DELETED = "Microsoft.EventGrid.SubscriptionDeletedEvent";

    // Event Hub Events.
    /**
     * indicate an event of creation of capture file in eventhub.
     */
    public static final String EVENT_HUB_CAPTURE_FILE_CREATED = "Microsoft.EventHub.CaptureFileCreated";

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
    public static final String MEDIA_JOB_CANCELED = "Microsoft.Media.JobCanceled";

    /**
     * Media Services Job Canceling Event.
     */
    public static final String MEDIA_JOB_CANCELING = "Microsoft.Media.JobCanceling";

    /**
     * Media Services Job Errored event.
     */
    public static final String MEDIA_JOB_ERRORED = "Microsoft.Media.JobErrored";

    /**
     * Media Services Job Finished event.
     */
    public static final String MEDIA_JOB_FINISHED = "Microsoft.Media.JobFinished";

    /**
     * Media Services Job Ouput Canceled event.
     */
    public static final String MEDIA_JOB_OUTPUT_CANCELED = "Microsoft.Media.JobOutputCanceled";

    /**
     * Media Services Job Output Canceling event.
     */
    public static final String MEDIA_JOB_OUTPUT_CANCELING = "Microsoft.Media.JobOutputCanceling";

    /**
     * Media Services Job Output Errored event.
     */
    public static final String MEDIA_JOB_OUTPUT_ERRORED = "Microsoft.Media.JobOutputErrored";

    /**
     * Media Services Job Output Finished event.
     */
    public static final String MEDIA_JOB_OUTPUT_FINISHED = "Microsoft.Media.JobOutputFinished";

    /**
     * Media Services Job Output Processing event.
     */
    public static final String MEDIA_JOB_OUTPUT_PROCESSING = "Microsoft.Media.JobOutputProcessing";

    /**
     * Media Services Job Output Progress event.
     */
    public static final String MEDIA_JOB_OUTPUT_PROGRESS = "Microsoft.Media.JobOutputProgress";

    /**
     * Media Services Job Output Scheduled event.
     */
    public static final String MEDIA_JOB_OUTPUT_SCHEDULED = "Microsoft.Media.JobOutputScheduled";

    /**
     * Media Services Job Output State Change event.
     */
    public static final String MEDIA_JOB_OUTPUT_STATE_CHANGE = "Microsoft.Media.JobOutputStateChange";

    /**
     * Media Services Job Processing event.
     */
    public static final String MEDIA_JOB_PROCESSING = "Microsoft.Media.JobProcessing";

    /**
     * Media Services Job Scheduled event.
     */
    public static final String MEDIA_JOB_SCHEDULED = "Microsoft.Media.JobScheduled";

    /**
     * Media Services Job State Change event.
     */
    public static final String MEDIA_JOB_STATE_CHANGE = "Microsoft.Media.JobStateChange";

    /**
     * Media Services Live Event Channel Archive Heartbeat event.
     */
    public static final String MEDIA_LIVE_EVENT_CHANNEL_ARCHIVE_HEARTBEAT =
        "Microsoft.Media.LiveEventChannelArchiveHeartbeat";

    /**
     * Media Services Live Event Connection Rejected event.
     */
    public static final String MEDIA_LIVE_EVENT_CONNECTION_REJECTED = "Microsoft.Media.LiveEventConnectionRejected";

    /**
     * Media Services Live Event Encoder Connected event.
     */
    public static final String MEDIA_LIVE_EVENT_ENCODER_CONNECTED = "Microsoft.Media.LiveEventEncoderConnected";

    /**
     * Media Services Live Event Encoder Disconnected event.
     */
    public static final String MEDIA_LIVE_EVENT_ENCODER_DISCONNECTED = "Microsoft.Media.LiveEventEncoderDisconnected";

    /**
     * Media Services Live Event Incoming Data Chunk Dropped event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_DATA_CHUNK_DROPPED = "Microsoft.Media.LiveEventIncomingDataChunkDropped";

    /**
     * Media Services Live Event Incoming Stream Received event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_STREAM_RECEIVED = "Microsoft.Media.LiveEventIncomingStreamReceived";

    /**
     * Media Services Live Event Incoming Streams OutofSync event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC = "Microsoft.Media.LiveEventIncomingStreamsOutOfSync";

    /**
     * Media Services Live Event Incoming Video Streams OutOfSync event.
     */
    public static final String MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC = "Microsoft.Media.LiveEventIncomingVideoStreamsOutOfSync";

    /**
     * Media Services Live Event Ingest Heartbeat event.
     */
    public static final String MEDIA_LIVE_EVENT_INGEST_HEARTBEAT = "Microsoft.Media.LiveEventIngestHeartbeat";

    /**
     * Media Services Live Event Track Discontinuity Detected event.
     */
    public static final String MEDIA_LIVE_EVENT_TRACK_DISCONTINUITY_DETECTED = "Microsoft.Media.LiveEventTrackDiscontinuityDetected";

    // Resource Manager (Azure Subscription/Resource Group) events
    /**
     * indicate an event of successful write of a resource.
     */
    public static final String RESOURCE_WRITE_SUCCESS = "Microsoft.Resources.ResourceWriteSuccess";
    /**
     * indicate an event of write failure of a resource.
     */
    public static final String RESOURCE_WRITE_FAILURE = "Microsoft.Resources.ResourceWriteFailure";
    /**
     * indicate an event of write cancellation of a resource.
     */
    public static final String RESOURCE_WRITE_CANCEL = "Microsoft.Resources.ResourceWriteCancel";
    /**
     * indicate an event of successful deletion of a resource.
     */
    public static final String RESOURCE_DELETE_SUCCESS = "Microsoft.Resources.ResourceDeleteSuccess";
    /**
     * indicate an event of failure in deleting a resource.
     */
    public static final String RESOURCE_DELETE_FAILURE = "Microsoft.Resources.ResourceDeleteFailure";
    /**
     * indicate an event of cancellation of resource deletion.
     */
    public static final String RESOURCE_DELETE_CANCEL = "Microsoft.Resources.ResourceDeleteCancel";
    /**
     * indicate an event of successful action on a resource.
     */
    public static final String RESOURCE_ACTION_SUCCESS = "Microsoft.Resources.ResourceActionSuccess";
    /**
     * indicate an event of failure in performing an action on a resource.
     */
    public static final String RESOURCE_ACTION_FAILURE = "Microsoft.Resources.ResourceActionFailure";
    /**
     * indicate an event of cancellation of resource action.
     */
    public static final String RESOURCE_ACTION_CANCEL = "Microsoft.Resources.ResourceActionCancel";

    // ServiceBus events.
    /**
     * indicate an event of active messages with no listener for them.
     */
    public static final String SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS = "Microsoft.ServiceBus.ActiveMessagesAvailableWithNoListeners";
    /**
     * indicate an event of deadletter messages with no listener for them.
     */
    public static final String SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER = "Microsoft.ServiceBus.DeadletterMessagesAvailableWithNoListener";

    /**
     * Schema of the Data property of an EventGridEvent for a
     * Microsoft.ServiceBus.ActiveMessagesAvailablePeriodicNotifications event.
     */
    public static final String SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION = "Microsoft.ServiceBus.ActiveMessagesAvailablePeriodicNotifications";

    /**
     * Schema of the Data property of an EventGridEvent for a
     * Microsoft.ServiceBus.DeadletterMessagesAvailablePeriodicNotifications event.
     */
    public static final String SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION = "Microsoft.ServiceBus.DeadletterMessagesAvailablePeriodicNotifications";

    // Storage events.
    /**
     * indicates an event of blob creation.
     */
    public static final String STORAGE_BLOB_CREATED = "Microsoft.Storage.BlobCreated";
    /**
     * indicates an event of blob deletion.
     */
    public static final String STORAGE_BLOB_DELETED = "Microsoft.Storage.BlobDeleted";

    /** Schema of the Data property of an EventGridEvent for an Microsoft.Storage.BlobInventoryPolicyCompleted event. */
    public static final String STORAGE_BLOB_INVENTORY_POLICY_COMPLETED =
        "Microsoft.Storage.BlobInventoryPolicyCompleted";

    /**
     * indicates an event of blob renaming.
     */
    public static final String STORAGE_BLOB_RENAMED = "Microsoft.Storage.BlobRenamed";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.BlobTierChanged event. */
    public static final String STORAGE_BLOB_TIER_CHANGED = "Microsoft.Storage.BlobTierChanged";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.BlobCreated event. */
    public static final String STORAGE_DIRECTORY_CREATED = "Microsoft.Storage.DirectoryCreated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.BlobDeleted event. */
    public static final String STORAGE_DIRECTORY_DELETED = "Microsoft.Storage.DirectoryDeleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.DirectoryRenamed event. */
    public static final String STORAGE_DIRECTORY_RENAMED = "Microsoft.Storage.DirectoryRenamed";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.LifecyclePolicyCompleted event. */
    public static final String STORAGE_LIFECYCLE_POLICY_COMPLETED = "Microsoft.Storage.LifecyclePolicyCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Storage.AsyncOperationInitiated event. */
    public static final String STORAGE_ASYNC_OPERATION_INITIATED = "Microsoft.Storage.AsyncOperationInitiated";

    // Communication Services events.
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMemberAddedToThreadWithUser event.
     */
    public static final String COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER =
        "Microsoft.Communication.ChatMemberAddedToThreadWithUser";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMemberRemovedFromThreadWithUser event.
     */
    public static final String COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER =
        "Microsoft.Communication.ChatMemberRemovedFromThreadWithUser";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageDeleted event. */
    public static final String COMMUNICATION_CHAT_MESSAGE_DELETED =
        "Microsoft.Communication.ChatMessageDeleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageEdited event. */
    public static final String COMMUNICATION_CHAT_MESSAGE_EDITED =
        "Microsoft.Communication.ChatMessageEdited";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageReceived event. */
    public static final String COMMUNICATION_CHAT_MESSAGE_RECEIVED =
        "Microsoft.Communication.ChatMessageReceived";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadCreatedWithUser event.
     */
    public static final String COMMUNICATION_CHAT_THREAD_CREATED_WITH_USER =
        "Microsoft.Communication.ChatThreadCreatedWithUser";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadPropertiesUpdatedPerUser
     * event.
     */
    public static final String COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED_PER_USER =
        "Microsoft.Communication.ChatThreadPropertiesUpdatedPerUser";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadWithUserDeleted event.
     */
    public static final String COMMUNICATION_CHAT_THREAD_WITH_USER_DELETED =
        "Microsoft.Communication.ChatThreadWithUserDeleted";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.SMSDeliveryReportReceived event.
     */
    public static final String COMMUNICATION_SMS_DELIVERY_REPORT_RECEIVED =
        "Microsoft.Communication.SMSDeliveryReportReceived";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.SMSReceived event. */
    public static final String COMMUNICATION_SMS_RECEIVED =
        "Microsoft.Communication.SMSReceived";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageDeletedInThread event.
     */
    public static final String COMMUNICATION_CHAT_MESSAGE_DELETED_IN_THREAD =
        "Microsoft.Communication.ChatMessageDeletedInThread";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageEditedInThread event.
     */
    public static final String COMMUNICATION_CHAT_MESSAGE_EDITED_IN_THREAD =
        "Microsoft.Communication.ChatMessageEditedInThread";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatMessageReceivedInThread event.
     */
    public static final String COMMUNICATION_CHAT_MESSAGE_RECEIVED_IN_THREAD =
        "Microsoft.Communication.ChatMessageReceivedInThread";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadParticipantAdded event.
     */
    public static final String COMMUNICATION_CHAT_PARTICIPANT_ADDED_TO_THREAD =
        "Microsoft.Communication.ChatThreadParticipantAdded";
    /**
     * Schema of the Data property of an EventGridEvent for a
     * Microsoft.Communication.ChatParticipantAddedToThreadWithUser event.
     */
    public static final String COMMUNICATION_CHAT_PARTICIPANT_ADDED_TO_THREAD_WITH_USER =
        "Microsoft.Communication.ChatParticipantAddedToThreadWithUser";
    /** Schema of the Data property of an EventGridEvent for an Microsoft.Communication.UserDisconnected event. */
    public static final String COMMUNICATION_USER_DISCONNECTED =
        "Microsoft.Communication.UserDisconnected";

    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.ContainerService.NewKubernetesVersionAvailable
     * event.
     */
    public static final String CONTAINER_SERVICE_NEW_KUBERNETES_VERSION_AVAILABLE =
            "Microsoft.ContainerService.NewKubernetesVersionAvailable";

    /**
     * @deprecated As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD}.
     */
    @Deprecated
    public static final String COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD =
        "Microsoft.Communication.ChatThreadParticipantRemoved";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadParticipantRemoved
     * event.
     */
    public static final String COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD =
        "Microsoft.Communication.ChatThreadParticipantRemoved";

    /**
     * @deprecated @deprecated As of 4.1.0, replaced by {@link #COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER}.
     */
    @Deprecated
    public static final String COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER =
        "Microsoft.Communication.ChatParticipantRemovedFromThreadWithUser";
    /**
     * Schema of the Data property of an EventGridEvent for a
     * Microsoft.Communication.ChatParticipantRemovedFromThreadWithUser event.
     */
    public static final String COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER =
        "Microsoft.Communication.ChatParticipantRemovedFromThreadWithUser";

    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadCreated event. */
    public static final String COMMUNICATION_CHAT_THREAD_CREATED =
        "Microsoft.Communication.ChatThreadCreated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadDeleted event. */
    public static final String COMMUNICATION_CHAT_THREAD_DELETED =
        "Microsoft.Communication.ChatThreadDeleted";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.ChatThreadPropertiesUpdated event.
     */
    public static final String COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED =
        "Microsoft.Communication.ChatThreadPropertiesUpdated";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.RecordingFileStatusUpdated event.
     */
    public static final String COMMUNICATION_RECORDING_FILE_STATUS_UPDATED =
        "Microsoft.Communication.RecordingFileStatusUpdated";

    // Web
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.AppUpdated event. */
    public static final String WEB_APP_UPDATED = "Microsoft.Web.AppUpdated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.BackupOperationStarted event. */
    public static final String WEB_BACKUP_OPERATION_STARTED = "Microsoft.Web.BackupOperationStarted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.BackupOperationCompleted event. */
    public static final String WEB_BACKUP_OPERATION_COMPLETED = "Microsoft.Web.BackupOperationCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.BackupOperationFailed event. */
    public static final String WEB_BACKUP_OPERATION_FAILED = "Microsoft.Web.BackupOperationFailed";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.RestoreOperationStarted event. */
    public static final String WEB_RESTORE_OPERATION_STARTED = "Microsoft.Web.RestoreOperationStarted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.RestoreOperationCompleted event. */
    public static final String WEB_RESTORE_OPERATION_COMPLETED = "Microsoft.Web.RestoreOperationCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.RestoreOperationFailed event. */
    public static final String WEB_RESTORE_OPERATION_FAILED = "Microsoft.Web.RestoreOperationFailed";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.SlotSwapStarted event. */
    public static final String WEB_SLOT_SWAP_STARTED = "Microsoft.Web.SlotSwapStarted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.SlotSwapCompleted event. */
    public static final String WEB_SLOT_SWAP_COMPLETED = "Microsoft.Web.SlotSwapCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.SlotSwapFailed event. */
    public static final String WEB_SLOT_SWAP_FAILED = "Microsoft.Web.SlotSwapFailed";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.SlotSwapWithPreviewStarted event. */
    public static final String WEB_SLOT_SWAP_WITH_PREVIEW_STARTED = "Microsoft.Web.SlotSwapWithPreviewStarted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.SlotSwapWithPreviewCancelled event. */
    public static final String WEB_SLOT_SWAP_WITH_PREVIEW_CANCELLED = "Microsoft.Web.SlotSwapWithPreviewCancelled";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Web.AppServicePlanUpdated event. */
    public static final String WEB_APP_SERVICE_PLAN_UPDATED = "Microsoft.Web.AppServicePlanUpdated";

    // Machine Learning
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.MachineLearningServices.DatasetDriftDetected
     * event.
     */
    public static final String MACHINE_LEARNING_DATASET_DRIFT_DETECTED = "Microsoft.MachineLearningServices.DatasetDriftDetected";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.MachineLearningServices.ModelDeployed event. */
    public static final String MACHINE_LEARNING_MODEL_DEPLOYED = "Microsoft.MachineLearningServices.ModelDeployed";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.MachineLearningServices.ModelRegistered event.
     */
    public static final String MACHINE_LEARNING_MODEL_REGISTERED = "Microsoft.MachineLearningServices.ModelRegistered";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.MachineLearningServices.RunCompleted event. */
    public static final String MACHINE_LEARNING_RUN_COMPLETED = "Microsoft.MachineLearningServices.RunCompleted";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.MachineLearningServices.RunStatusChanged event.
     */
    public static final String MACHINE_LEARNING_RUN_STATUS_CHANGED = "Microsoft.MachineLearningServices.RunStatusChanged";

    // Key Vault
    /** /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.CertificateNewVersionCreated event. */
    public static final String KEY_VAULT_CERTIFICATE_NEW_VERSION_CREATED = "Microsoft.KeyVault.CertificateNewVersionCreated";
    /** /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.CertificateNearExpiry event. */
    public static final String KEY_VAULT_CERTIFICATE_NEAR_EXPIRY = "Microsoft.KeyVault.CertificateNearExpiry";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.CertificateExpired event. */
    public static final String KEY_VAULT_CERTIFICATE_EXPIRED = "Microsoft.KeyVault.CertificateExpired";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.KeyNewVersionCreated event. */
    public static final String KEY_VAULT_KEY_NEW_VERSION_CREATED = "Microsoft.KeyVault.KeyNewVersionCreated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.KeyNearExpiry event. */
    public static final String KEY_VAULT_KEY_NEAR_EXPIRY = "Microsoft.KeyVault.KeyNearExpiry";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.KeyExpired event. */
    public static final String KEY_VAULT_KEY_EXPIRED = "Microsoft.KeyVault.KeyExpired";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.SecretNewVersionCreated event. */
    public static final String KEY_VAULT_SECRET_NEW_VERSION_CREATED = "Microsoft.KeyVault.SecretNewVersionCreated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.SecretNearExpiry event. */
    public static final String KEY_VAULT_SECRET_NEAR_EXPIRY = "Microsoft.KeyVault.SecretNearExpiry";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.SecretExpired event. */
    public static final String KEY_VAULT_SECRET_EXPIRED = "Microsoft.KeyVault.SecretExpired";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.KeyVault.VaultAccessPolicyChanged event. */
    public static final String KEY_VAULT_VAULT_ACCESS_POLICY_CHANGED = "Microsoft.KeyVault.VaultAccessPolicyChanged";

    // Redis Cache
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Cache.PatchingCompleted event. */
    public static final String REDIS_PATCHING_COMPLETED = "Microsoft.Cache.PatchingCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Cache.ScalingCompleted event. */
    public static final String REDIS_SCALING_COMPLETED = "Microsoft.Cache.ScalingCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Cache.ExportRDBCompleted event. */
    public static final String REDIS_EXPORT_RDB_COMPLETED = "Microsoft.Cache.ExportRDBCompleted";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.Cache.ImportRDBCompleted event. */
    public static final String REDIS_IMPORT_RDB_COMPLETED = "Microsoft.Cache.ImportRDBCompleted";

    // Signal R Service
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.SignalRService.ClientConnectionConnected event.
     */
    public static final String SIGNAL_R_SERVICE_CLIENT_CONNECTION_CONNECTED = "Microsoft.SignalRService.ClientConnectionConnected";
    /**
     * Schema of the Data property of an EventGridEvent for a Microsoft.SignalRService.ClientConnectionDisconnected
     * event.
     */
    public static final String SIGNAL_R_SERVICE_CLIENT_CONNECTION_DISCONNECTED = "Microsoft.SignalRService.ClientConnectionDisconnected";

    // Policy Insights
    /** Schema of the Data property of an EventGridEvent for a Microsoft.PolicyInsights.PolicyStateCreated event. */
    public static final String POLICY_INSIGHTS_POLICY_STATE_CREATED = "Microsoft.PolicyInsights.PolicyStateCreated";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.PolicyInsights.PolicyStateChanged event. */
    public static final String POLICY_INSIGHTS_POLICY_STATE_CHANGED = "Microsoft.PolicyInsights.PolicyStateChanged";
    /** Schema of the Data property of an EventGridEvent for a Microsoft.PolicyInsights.PolicyStateDeleted event. */
    public static final String POLICY_INSIGHTS_POLICY_STATE_DELETED = "Microsoft.PolicyInsights.PolicyStateDeleted";

    private static final Map<String, Class<?>> SYSTEM_EVENT_MAPPINGS = new HashMap<String, Class<?>>() {
        {
            // API events.
            put(API_MANAGEMENT_API_CREATED, ApiManagementApiCreatedEventData.class);
            put(API_MANAGEMENT_API_DELETED, ApiManagementApiDeletedEventData.class);
            put(API_MANAGEMENT_API_RELEASE_CREATED, ApiManagementApiReleaseCreatedEventData.class);
            put(API_MANAGEMENT_API_RELEASE_DELETED, ApiManagementApiReleaseDeletedEventData.class);
            put(API_MANAGEMENT_API_RELEASE_UPDATED, ApiManagementApiReleaseUpdatedEventData.class);
            put(API_MANAGEMENT_API_UPDATED, ApiManagementApiUpdatedEventData.class);
            put(API_MANAGEMENT_PRODUCT_CREATED, ApiManagementProductCreatedEventData.class);
            put(API_MANAGEMENT_PRODUCT_DELETED, ApiManagementProductDeletedEventData.class);
            put(API_MANAGEMENT_PRODUCT_UPDATED, ApiManagementProductUpdatedEventData.class);
            put(API_MANAGEMENT_SUBSCRIPTION_CREATED, ApiManagementSubscriptionCreatedEventData.class);
            put(API_MANAGEMENT_SUBSCRIPTION_DELETED, ApiManagementSubscriptionDeletedEventData.class);
            put(API_MANAGEMENT_SUBSCRIPTION_UPDATED, ApiManagementSubscriptionUpdatedEventData.class);
            put(API_MANAGEMENT_USER_CREATED, ApiManagementUserCreatedEventData.class);
            put(API_MANAGEMENT_USER_DELETED, ApiManagementUserDeletedEventData.class);
            put(API_MANAGEMENT_USER_UPDATED, ApiManagementUserUpdatedEventData.class);

            // AppConfiguration events.
            put(APP_CONFIGURATION_KEY_VALUE_DELETED, AppConfigurationKeyValueDeletedEventData.class);
            put(APP_CONFIGURATION_KEY_VALUE_MODIFIED, AppConfigurationKeyValueModifiedEventData.class);

            // ContainerRegistry events.
            put(CONTAINER_REGISTRY_IMAGE_PUSHED, ContainerRegistryImagePushedEventData.class);
            put(CONTAINER_REGISTRY_IMAGE_DELETED, ContainerRegistryImageDeletedEventData.class);
            put(CONTAINER_REGISTRY_CHART_DELETED, ContainerRegistryChartDeletedEventData.class);
            put(CONTAINER_REGISTRY_CHART_PUSHED, ContainerRegistryChartPushedEventData.class);

            // Device events.
            put(IOT_HUB_DEVICE_CREATED, IotHubDeviceCreatedEventData.class);
            put(IOT_HUB_DEVICE_DELETED, IotHubDeviceDeletedEventData.class);
            put(IOT_HUB_DEVICE_CONNECTED, IotHubDeviceConnectedEventData.class);
            put(IOT_HUB_DEVICE_DISCONNECTED, IotHubDeviceDisconnectedEventData.class);
            put(IOT_HUB_DEVICE_TELEMETRY, IotHubDeviceTelemetryEventData.class);

            // EventGrid events.
            put(EVENT_GRID_SUBSCRIPTION_VALIDATION, SubscriptionValidationEventData.class);
            put(EVENT_GRID_SUBSCRIPTION_DELETED, SubscriptionDeletedEventData.class);

            // Event Hub Events.
            put(EVENT_HUB_CAPTURE_FILE_CREATED, EventHubCaptureFileCreatedEventData.class);

            // Healthcare events
            put(HEALTHCARE_FHIR_RESOURCE_CREATED, HealthcareFhirResourceCreatedEventData.class);
            put(HEALTHCARE_FHIR_RESOURCE_DELETED, HealthcareFhirResourceDeletedEventData.class);
            put(HEALTHCARE_FHIR_RESOURCE_UPDATED, HealthcareFhirResourceUpdatedEventData.class);

            // Maps events
            put(MAPS_GEOFENCE_ENTERED, MapsGeofenceEnteredEventData.class);
            put(MAPS_GEOFENCE_EXITED, MapsGeofenceExitedEventData.class);
            put(MAPS_GEOFENCE_RESULT, MapsGeofenceResultEventData.class);

            // Media Services events.
            put(MEDIA_JOB_CANCELED, MediaJobCanceledEventData.class);
            put(MEDIA_JOB_CANCELING, MediaJobCancelingEventData.class);
            put(MEDIA_JOB_ERRORED, MediaJobErroredEventData.class);
            put(MEDIA_JOB_FINISHED, MediaJobFinishedEventData.class);
            put(MEDIA_JOB_OUTPUT_CANCELED, MediaJobOutputCanceledEventData.class);
            put(MEDIA_JOB_OUTPUT_CANCELING, MediaJobOutputCancelingEventData.class);
            put(MEDIA_JOB_OUTPUT_ERRORED, MediaJobOutputErroredEventData.class);
            put(MEDIA_JOB_OUTPUT_FINISHED, MediaJobOutputFinishedEventData.class);
            put(MEDIA_JOB_OUTPUT_PROCESSING, MediaJobOutputProcessingEventData.class);
            put(MEDIA_JOB_OUTPUT_PROGRESS, MediaJobOutputProgressEventData.class);
            put(MEDIA_JOB_OUTPUT_SCHEDULED, MediaJobOutputScheduledEventData.class);
            put(MEDIA_JOB_OUTPUT_STATE_CHANGE, MediaJobOutputStateChangeEventData.class);
            put(MEDIA_JOB_PROCESSING, MediaJobProcessingEventData.class);
            put(MEDIA_JOB_SCHEDULED, MediaJobScheduledEventData.class);
            put(MEDIA_JOB_STATE_CHANGE, MediaJobStateChangeEventData.class);
            put(MEDIA_LIVE_EVENT_CHANNEL_ARCHIVE_HEARTBEAT, MediaLiveEventChannelArchiveHeartbeatEventData.class);
            put(MEDIA_LIVE_EVENT_CONNECTION_REJECTED, MediaLiveEventConnectionRejectedEventData.class);
            put(MEDIA_LIVE_EVENT_ENCODER_CONNECTED, MediaLiveEventEncoderConnectedEventData.class);
            put(MEDIA_LIVE_EVENT_ENCODER_DISCONNECTED, MediaLiveEventEncoderDisconnectedEventData.class);
            put(MEDIA_LIVE_EVENT_INCOMING_DATA_CHUNK_DROPPED, MediaLiveEventIncomingDataChunkDroppedEventData.class);
            put(MEDIA_LIVE_EVENT_INCOMING_STREAMS_OUTOFSYNC, MediaLiveEventIncomingStreamsOutOfSyncEventData.class);
            put(MEDIA_LIVE_EVENT_INCOMING_STREAM_RECEIVED, MediaLiveEventIncomingStreamReceivedEventData.class);
            put(MEDIA_LIVE_EVENT_INCOMING_VIDEO_STREAMS_OUTOFSYNC, MediaLiveEventIncomingVideoStreamsOutOfSyncEventData.class);
            put(MEDIA_LIVE_EVENT_INGEST_HEARTBEAT, MediaLiveEventIngestHeartbeatEventData.class);
            put(MEDIA_LIVE_EVENT_TRACK_DISCONTINUITY_DETECTED, MediaLiveEventTrackDiscontinuityDetectedEventData.class);

            // Resource Manager (Azure Subscription/Resource Group) events.
            put(RESOURCE_WRITE_SUCCESS, ResourceWriteSuccessEventData.class);
            put(RESOURCE_WRITE_FAILURE, ResourceWriteFailureEventData.class);
            put(RESOURCE_WRITE_CANCEL, ResourceWriteCancelEventData.class);
            put(RESOURCE_DELETE_SUCCESS, ResourceDeleteSuccessEventData.class);
            put(RESOURCE_DELETE_FAILURE, ResourceDeleteFailureEventData.class);
            put(RESOURCE_DELETE_CANCEL, ResourceDeleteCancelEventData.class);
            put(RESOURCE_ACTION_SUCCESS, ResourceActionSuccessEventData.class);
            put(RESOURCE_ACTION_FAILURE, ResourceActionFailureEventData.class);
            put(RESOURCE_ACTION_CANCEL, ResourceActionCancelEventData.class);

            // ServiceBus events.
            put(SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS, ServiceBusActiveMessagesAvailableWithNoListenersEventData.class);
            put(SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER, ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class);
            put(SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION, ServiceBusActiveMessagesAvailablePeriodicNotificationsEventData.class);
            put(SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_PERIODIC_NOTIFICATION, ServiceBusDeadletterMessagesAvailablePeriodicNotificationsEventData.class);

            // Storage events.
            put(STORAGE_BLOB_CREATED, StorageBlobCreatedEventData.class);
            put(STORAGE_BLOB_DELETED, StorageBlobDeletedEventData.class);
            put(STORAGE_BLOB_INVENTORY_POLICY_COMPLETED, StorageBlobInventoryPolicyCompletedEventData.class);
            put(STORAGE_BLOB_RENAMED, StorageBlobRenamedEventData.class);
            put(STORAGE_BLOB_TIER_CHANGED, StorageBlobTierChangedEventData.class);
            put(STORAGE_DIRECTORY_CREATED, StorageDirectoryCreatedEventData.class);
            put(STORAGE_DIRECTORY_DELETED, StorageDirectoryDeletedEventData.class);
            put(STORAGE_DIRECTORY_RENAMED, StorageDirectoryRenamedEventData.class);
            put(STORAGE_LIFECYCLE_POLICY_COMPLETED, StorageLifecyclePolicyCompletedEventData.class);
            put(STORAGE_ASYNC_OPERATION_INITIATED, StorageAsyncOperationInitiatedEventData.class);

            // Communication service events.
            put(COMMUNICATION_CHAT_MEMBER_ADDED_TO_THREAD_WITH_USER, AcsChatMemberAddedToThreadWithUserEventData.class);
            put(COMMUNICATION_CHAT_MEMBER_REMOVED_FROM_THREAD_WITH_USER, AcsChatMemberRemovedFromThreadWithUserEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_DELETED, AcsChatMessageDeletedEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_EDITED, AcsChatMessageEditedEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_RECEIVED, AcsChatMessageReceivedEventData.class);
            put(COMMUNICATION_CHAT_THREAD_CREATED_WITH_USER, AcsChatThreadCreatedWithUserEventData.class);
            put(COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED_PER_USER, AcsChatThreadPropertiesUpdatedPerUserEventData.class);
            put(COMMUNICATION_CHAT_THREAD_WITH_USER_DELETED, AcsChatThreadWithUserDeletedEventData.class);
            put(COMMUNICATION_SMS_DELIVERY_REPORT_RECEIVED, AcsSmsDeliveryReportReceivedEventData.class);
            put(COMMUNICATION_SMS_RECEIVED, AcsSmsReceivedEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_DELETED_IN_THREAD, AcsChatMessageDeletedInThreadEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_EDITED_IN_THREAD, AcsChatMessageEditedInThreadEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_RECEIVED_IN_THREAD, AcsChatMessageReceivedInThreadEventData.class);
            put(COMMUNICATION_CHAT_PARTICIPANT_ADDED_TO_THREAD, AcsChatParticipantAddedToThreadEventData.class);
            put(COMMUNICATION_CHAT_PARTICIPANT_ADDED_TO_THREAD_WITH_USER, AcsChatParticipantAddedToThreadWithUserEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD, AcsChatParticipantRemovedFromThreadEventData.class);
            put(COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD, AcsChatParticipantRemovedFromThreadEventData.class);
            put(COMMUNICATION_CHAT_MESSAGE_REMOVED_FROM_THREAD_WITH_USER, AcsChatParticipantRemovedFromThreadWithUserEventData.class);
            put(COMMUNICATION_CHAT_PARTICIPANT_REMOVED_FROM_THREAD_WITH_USER, AcsChatParticipantRemovedFromThreadWithUserEventData.class);
            put(COMMUNICATION_CHAT_THREAD_CREATED, AcsChatThreadCreatedEventData.class);
            put(COMMUNICATION_CHAT_THREAD_DELETED, AcsChatThreadDeletedEventData.class);
            put(COMMUNICATION_CHAT_THREAD_PROPERTIES_UPDATED, AcsChatThreadPropertiesUpdatedEventData.class);
            put(COMMUNICATION_RECORDING_FILE_STATUS_UPDATED, AcsRecordingFileStatusUpdatedEventData.class);
            put(COMMUNICATION_USER_DISCONNECTED, AcsUserDisconnectedEventData.class);

            // Web events
            put(WEB_APP_UPDATED, WebAppUpdatedEventData.class);
            put(WEB_BACKUP_OPERATION_STARTED, WebBackupOperationStartedEventData.class);
            put(WEB_BACKUP_OPERATION_COMPLETED, WebBackupOperationCompletedEventData.class);
            put(WEB_BACKUP_OPERATION_FAILED, WebBackupOperationFailedEventData.class);
            put(WEB_RESTORE_OPERATION_STARTED, WebRestoreOperationStartedEventData.class);
            put(WEB_RESTORE_OPERATION_COMPLETED, WebRestoreOperationCompletedEventData.class);
            put(WEB_RESTORE_OPERATION_FAILED, WebRestoreOperationFailedEventData.class);
            put(WEB_SLOT_SWAP_STARTED, WebSlotSwapStartedEventData.class);
            put(WEB_SLOT_SWAP_COMPLETED, WebSlotSwapCompletedEventData.class);
            put(WEB_SLOT_SWAP_FAILED, WebSlotSwapFailedEventData.class);
            put(WEB_SLOT_SWAP_WITH_PREVIEW_STARTED, WebSlotSwapWithPreviewStartedEventData.class);
            put(WEB_SLOT_SWAP_WITH_PREVIEW_CANCELLED, WebSlotSwapWithPreviewCancelledEventData.class);
            put(WEB_APP_SERVICE_PLAN_UPDATED, WebAppServicePlanUpdatedEventData.class);

            // Machine Learning events
            put(MACHINE_LEARNING_DATASET_DRIFT_DETECTED, MachineLearningServicesDatasetDriftDetectedEventData.class);
            put(MACHINE_LEARNING_MODEL_DEPLOYED, MachineLearningServicesModelDeployedEventData.class);
            put(MACHINE_LEARNING_MODEL_REGISTERED, MachineLearningServicesModelRegisteredEventData.class);
            put(MACHINE_LEARNING_RUN_COMPLETED, MachineLearningServicesRunCompletedEventData.class);
            put(MACHINE_LEARNING_RUN_STATUS_CHANGED, MachineLearningServicesRunStatusChangedEventData.class);

            // Key Vault events
            put(KEY_VAULT_CERTIFICATE_NEW_VERSION_CREATED, KeyVaultCertificateNewVersionCreatedEventData.class);
            put(KEY_VAULT_CERTIFICATE_NEAR_EXPIRY, KeyVaultCertificateNearExpiryEventData.class);
            put(KEY_VAULT_CERTIFICATE_EXPIRED, KeyVaultCertificateExpiredEventData.class);
            put(KEY_VAULT_KEY_NEW_VERSION_CREATED, KeyVaultKeyNewVersionCreatedEventData.class);
            put(KEY_VAULT_KEY_NEAR_EXPIRY, KeyVaultKeyNearExpiryEventData.class);
            put(KEY_VAULT_KEY_EXPIRED, KeyVaultKeyExpiredEventData.class);
            put(KEY_VAULT_SECRET_NEW_VERSION_CREATED, KeyVaultSecretNewVersionCreatedEventData.class);
            put(KEY_VAULT_SECRET_NEAR_EXPIRY, KeyVaultSecretNearExpiryEventData.class);
            put(KEY_VAULT_SECRET_EXPIRED, KeyVaultSecretExpiredEventData.class);
            put(KEY_VAULT_VAULT_ACCESS_POLICY_CHANGED, KeyVaultAccessPolicyChangedEventData.class);

            // Redis
            put(REDIS_EXPORT_RDB_COMPLETED, RedisExportRdbCompletedEventData.class);
            put(REDIS_PATCHING_COMPLETED, RedisPatchingCompletedEventData.class);
            put(REDIS_IMPORT_RDB_COMPLETED, RedisImportRdbCompletedEventData.class);
            put(REDIS_SCALING_COMPLETED, RedisScalingCompletedEventData.class);

            // Signal R Service
            put(SIGNAL_R_SERVICE_CLIENT_CONNECTION_CONNECTED, SignalRServiceClientConnectionConnectedEventData.class);
            put(SIGNAL_R_SERVICE_CLIENT_CONNECTION_DISCONNECTED, SignalRServiceClientConnectionDisconnectedEventData.class);

            // Policy Insights
            put(POLICY_INSIGHTS_POLICY_STATE_CREATED, PolicyInsightsPolicyStateCreatedEventData.class);
            put(POLICY_INSIGHTS_POLICY_STATE_CHANGED, PolicyInsightsPolicyStateChangedEventData.class);
            put(POLICY_INSIGHTS_POLICY_STATE_DELETED, PolicyInsightsPolicyStateDeletedEventData.class);

            // Container Service
            put(CONTAINER_SERVICE_NEW_KUBERNETES_VERSION_AVAILABLE,
                    ContainerServiceNewKubernetesVersionAvailableEventData.class);
        }
    };

    /**
     * Get a mapping of all the system event type strings to their respective class. This is used by default in
     * the {@link EventGridEvent} and {@link CloudEvent} classes.
     * @return a mapping of all the system event strings to system event objects.
     */
    public static Map<String, Class<?>> getSystemEventMappings() {
        return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS);
    }

    private SystemEventNames() {
    }
}
