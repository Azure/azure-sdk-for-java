package com.microsoft.azure.eventgrid.customization;

import com.microsoft.azure.management.apigeneration.Beta;

/**
 *  Represents the names of the various event types for the system events published to Azure Event Grid.
 */
@Beta
public class SystemEventTypes {
    // Keep this sorted by the name of the service publishing the events.

    // ContainerRegistry events.
    /**
     * indicate an event of pushing an image to container registry.
     */
    public final static String CONTAINER_REGISTRY_IMAGE_PUSHED_EVENT = "Microsoft.ContainerRegistry.ImagePushed";
    /**
     * indicate an event of deleting an image to container registry.
     */
    public final static String CONTAINER_REGISTRY_IMAGE_DELETED_EVENT = "Microsoft.ContainerRegistry.ImageDeleted";

    // Device events.
    /**
     * indicate an event of creating an IoT hub device.
     */
    public final static String IOT_HUB_DEVICE_CREATED_EVENT = "Microsoft.Devices.DeviceCreated";
    /**
     * indicate an event of deleting an IoT hub device.
     */
    public final static String IOT_HUB_DEVICE_DELETED_EVENT = "Microsoft.Devices.DeviceDeleted";
    /**
     * indicate an event of connecting an IoT hub device.
     */
    public final static String IOT_HUB_DEVICE_CONNECTED_EVENT = "Microsoft.Devices.DeviceConnected";
    /**
     * indicate an event of disconnecting an IoT hub device.
     */
    public final static String IOT_HUB_DEVICE_DISCONNECTED_EVENT = "Microsoft.Devices.DeviceDisconnected";

    // EventGrid events.
    /**
     * indicate an event of validating eventgrid subscription.
     */
    public final static String EVENT_GRID_SUBSCRIPTION_VALIDATION_EVENT = "Microsoft.EventGrid.SubscriptionValidationEvent";
    /**
     * indicate an event of deleting eventgrid subscription.
     */
    public final static String EVENT_GRID_SUBSCRIPTION_DELETED_EVENT = "Microsoft.EventGrid.SubscriptionDeletedEvent";

    // Event Hub Events.
    /**
     * indicate an event of creation of capture file in eventhub.
     */
    public final static String EVENT_HUB_CAPTURE_FILE_CREATED_EVENT = "Microsoft.EventHub.CaptureFileCreated";

    // Media Services events.
    /**
     * indicate an event of change of media service job state.
     */
    public final static String MEDIA_JOB_STATE_CHANGE_EVENT = "Microsoft.Media.JobStateChange";

    // Resource Manager (Azure Subscription/Resource Group) events
    /**
     * indicate an event of successful write of a resource.
     */
    public final static String RESOURCE_WRITE_SUCCESS_EVENT = "Microsoft.Resources.ResourceWriteSuccess";
    /**
     * indicate an event of write failure of a resource.
     */
    public final static String RESOURCE_WRITE_FAILURE_EVENT = "Microsoft.Resources.ResourceWriteFailure";
    /**
     * indicate an event of write cancellation of a resource.
     */
    public final static String RESOURCE_WRITE_CANCEL_EVENT = "Microsoft.Resources.ResourceWriteCancel";
    /**
     * indicate an event of successful deletion of a resource.
     */
    public final static String RESOURCE_DELETE_SUCCESS_EVENT = "Microsoft.Resources.ResourceDeleteSuccess";
    /**
     * indicate an event of failure in deleting a resource.
     */
    public final static String RESOURCE_DELETE_FAILURE_EVENT = "Microsoft.Resources.ResourceDeleteFailure";
    /**
     * indicate an event of cancellation of resource deletion.
     */
    public final static String RESOURCE_DELETE_CANCEL_EVENT = "Microsoft.Resources.ResourceDeleteCancel";
    /**
     * indicate an event of successful action on a resource.
     */
    public final static String RESOURCE_ACTION_SUCCESS_EVENT = "Microsoft.Resources.ResourceActionSuccess";
    /**
     * indicate an event of failure in performing an action on a resource.
     */
    public final static String RESOURCE_ACTION_FAILURE_EVENT = "Microsoft.Resources.ResourceActionFailure";
    /**
     * indicate an event of cancellation of resource action.
     */
    public final static String RESOURCE_ACTION_CANCEL_EVENT = "Microsoft.Resources.ResourceActionCancel";

    // ServiceBus events.
    /**
     * indicate an event of active messages with no listener for them.
     */
    public final static String SERVICE_BUS_ACTIVE_MESSAGES_AVAILABLE_WITH_NO_LISTENERS_EVENT = "Microsoft.ServiceBus.ActiveMessagesAvailableWithNoListeners";
    /**
     * indicate an event of deadletter messages with no listener for them.
     */
    public final static String SERVICE_BUS_DEADLETTER_MESSAGES_AVAILABLE_WITH_NO_LISTENER_EVENT = "Microsoft.ServiceBus.DeadletterMessagesAvailableWithNoListener";

    // Storage events.
    /**
     * indicates an event of blob creation.
     */
    public final static String STORAGE_BLOB_CREATED_EVENT = "Microsoft.Storage.BlobCreated";
    /**
     * indicates an event of blob deletion.
     */
    public final static String STORAGE_BLOB_DELETED_EVENT = "Microsoft.Storage.BlobDeleted";
}
