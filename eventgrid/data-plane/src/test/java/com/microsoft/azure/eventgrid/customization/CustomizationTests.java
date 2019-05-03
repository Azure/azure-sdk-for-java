/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.eventgrid.customization;

import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.eventgrid.customization.models.ContosoItemReceivedEventData;
import com.microsoft.azure.eventgrid.customization.models.ContosoItemSentEventData;
import com.microsoft.azure.eventgrid.customization.models.DroneShippingInfo;
import com.microsoft.azure.eventgrid.customization.models.RocketShippingInfo;
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
import org.junit.Assert;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class CustomizationTests {

    @Test
    public void consumeStorageBlobDeletedEventWithExtraProperty() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEventWithExtraProperty.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData)events[0].data();
        Assert.assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.url());
    }

    @Test
    public void ConsumeCustomEvents() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEvents.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", ContosoItemSentEventData.class);
        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", ContosoItemReceivedEventData.class);

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.length);
        Assert.assertTrue(events[0].data() instanceof ContosoItemReceivedEventData);
        ContosoItemReceivedEventData eventData = (ContosoItemReceivedEventData) events[0].data();
        Assert.assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.itemSku());
    }

    @Test
    public void consumeCustomEventWithArrayData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithArrayData.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();

        ContosoItemReceivedEventData[] arr = { new ContosoItemReceivedEventData() };
        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", arr.getClass());

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.length);
        Assert.assertTrue(events[0].data() instanceof ContosoItemReceivedEventData[]);
        ContosoItemReceivedEventData[] eventData = (ContosoItemReceivedEventData[]) events[0].data();
        Assert.assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", (eventData[0]).itemSku());
    }

    @Test
    public void consumeCustomEventWithBooleanData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithBooleanData.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();

        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", Boolean.class);

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.length);
        Assert.assertTrue(events[0].data() instanceof Boolean);
        Boolean eventData = (Boolean) events[0].data();
        Assert.assertTrue(eventData);
    }

    @Test
    public void consumeCustomEventWithStringData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithStringData.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.length);
        Assert.assertTrue(events[0].data() instanceof String);
        String eventData = (String) events[0].data();
        Assert.assertEquals("stringdata", eventData);
    }

    @Test
    public void consumeCustomEventWithPolymorphicData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithPolymorphicData.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();

        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemSent", ContosoItemSentEventData.class);

        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.length);
        Assert.assertTrue(events[0].data() instanceof ContosoItemSentEventData);
        Assert.assertTrue(events[1].data() instanceof ContosoItemSentEventData);
        ContosoItemSentEventData eventData0 = (ContosoItemSentEventData)events[0].data();
        Assert.assertTrue(eventData0.shippingInfo() instanceof DroneShippingInfo);
        ContosoItemSentEventData eventData1 = (ContosoItemSentEventData)events[1].data();
        Assert.assertTrue(eventData1.shippingInfo() instanceof RocketShippingInfo);
    }

    @Test
    public void testCustomEventMappings() {
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemSent", ContosoItemSentEventData.class);
        eventGridSubscriber.putCustomEventMapping("Contoso.Items.ItemReceived", ContosoItemReceivedEventData.class);

        Set<Map.Entry<String, Type>> mappings = eventGridSubscriber.getAllCustomEventMappings();

        Assert.assertEquals(2, mappings.size());

        Type mapping = eventGridSubscriber.getCustomEventMapping("Contoso.Items.ItemSent");
        Assert.assertNotNull(mapping);
        // Ensure lookup is case-insensitive
        mapping = eventGridSubscriber.getCustomEventMapping("contoso.Items.Itemsent");
        Assert.assertNotNull(mapping);

        mapping = eventGridSubscriber.getCustomEventMapping("Contoso.Items.NotExists");
        Assert.assertNull(mapping);

        boolean removed = eventGridSubscriber.removeCustomEventMapping("Contoso.Items.ItemReceived");
        Assert.assertTrue(removed);
        Assert.assertEquals(1, mappings.size());

       boolean contains = eventGridSubscriber.containsCustomEventMappingFor("Contoso.Items.ItemSent");
        Assert.assertTrue(contains);
    }

    @Test
    public void consumeMultipleEventsInSameBatch() throws IOException {
        String jsonData = getTestPayloadFromFile("MultipleEventsInSameBatch.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertEquals(4, events.length);
        Assert.assertTrue(events[0].data() instanceof StorageBlobCreatedEventData);
        Assert.assertTrue(events[1].data() instanceof StorageBlobDeletedEventData);
        Assert.assertTrue(events[2].data() instanceof StorageBlobDeletedEventData);
        Assert.assertTrue(events[3].data() instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData)events[2].data();
        Assert.assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.url());
    }

    // AppConfiguration events
    @Test
    public void consumeAppConfigurationKeyValueDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueDeleted.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof AppConfigurationKeyValueDeletedEventData);
        AppConfigurationKeyValueDeletedEventData eventData = (AppConfigurationKeyValueDeletedEventData)events[0].data();
        Assert.assertEquals("key1", eventData.key());
    }

    @Test
    public void consumeAppConfigurationKeyValueModifiedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueModified.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof AppConfigurationKeyValueModifiedEventData);
        AppConfigurationKeyValueModifiedEventData eventData = (AppConfigurationKeyValueModifiedEventData)events[0].data();
        Assert.assertEquals("key1", eventData.key());
    }

    // ContainerRegistry events
    @Test
    public void consumeContainerRegistryImagePushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImagePushedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ContainerRegistryImagePushedEventData);
        ContainerRegistryImagePushedEventData eventData = (ContainerRegistryImagePushedEventData)events[0].data();
        Assert.assertEquals("127.0.0.1", eventData.request().addr());
    }

    @Test
    public void consumeContainerRegistryImageDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImageDeletedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ContainerRegistryImageDeletedEventData);
        ContainerRegistryImageDeletedEventData eventData = (ContainerRegistryImageDeletedEventData)events[0].data();
        Assert.assertEquals("testactor", eventData.actor().name());
    }

    @Test
    public void consumeContainerRegistryChartDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartDeletedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ContainerRegistryChartDeletedEventData);
        ContainerRegistryChartDeletedEventData eventData = (ContainerRegistryChartDeletedEventData)events[0].data();
        Assert.assertEquals("mediatype1", eventData.target().mediaType());
    }

    @Test
    public void consumeContainerRegistryChartPushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartPushedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ContainerRegistryChartPushedEventData);
        ContainerRegistryChartPushedEventData eventData = (ContainerRegistryChartPushedEventData)events[0].data();
        Assert.assertEquals("mediatype1", eventData.target().mediaType());
    }

    // IoTHub Device events
    @Test
    public void consumeIoTHubDeviceCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceCreatedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof IotHubDeviceCreatedEventData);
        IotHubDeviceCreatedEventData eventData = (IotHubDeviceCreatedEventData)events[0].data();
        Assert.assertEquals("enabled", eventData.twin().status());
    }

    @Test
    public void consumeIoTHubDeviceDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDeletedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof IotHubDeviceDeletedEventData);
        IotHubDeviceDeletedEventData eventData = (IotHubDeviceDeletedEventData)events[0].data();
        Assert.assertEquals("AAAAAAAAAAE=", eventData.twin().etag());
    }

    @Test
    public void consumeIoTHubDeviceConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceConnectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof IotHubDeviceConnectedEventData);
        IotHubDeviceConnectedEventData eventData = (IotHubDeviceConnectedEventData)events[0].data();
        Assert.assertEquals("EGTESTHUB1", eventData.hubName());
    }

    @Test
    public void consumeIoTHubDeviceDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDisconnectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof IotHubDeviceDisconnectedEventData);
        IotHubDeviceDisconnectedEventData eventData = (IotHubDeviceDisconnectedEventData)events[0].data();
        Assert.assertEquals("000000000000000001D4132452F67CE200000002000000000000000000000002", eventData.deviceConnectionStateEventInfo().sequenceNumber());
    }

    @Test
    public void consumeIoTHubDeviceTelemetryEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceTelemetryEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof IotHubDeviceTelemetryEventData);
        IotHubDeviceTelemetryEventData eventData = (IotHubDeviceTelemetryEventData)events[0].data();
        Assert.assertEquals("Active", eventData.properties().get("Status"));
    }

    // EventGrid events
    @Test
    public void ConsumeEventGridSubscriptionValidationEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionValidationEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof SubscriptionValidationEventData);
        SubscriptionValidationEventData eventData = (SubscriptionValidationEventData)events[0].data();
        Assert.assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.validationCode());
    }

    @Test
    public void consumeEventGridSubscriptionDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionDeletedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof SubscriptionDeletedEventData);
        SubscriptionDeletedEventData eventData = (SubscriptionDeletedEventData)events[0].data();
        Assert.assertEquals("/subscriptions/id/resourceGroups/rg/providers/Microsoft.EventGrid/topics/topic1/providers/Microsoft.EventGrid/eventSubscriptions/eventsubscription1", eventData.eventSubscriptionId());
    }

    // Event Hub Events
    @Test
    public void consumeEventHubCaptureFileCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventHubCaptureFileCreatedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof EventHubCaptureFileCreatedEventData);
        EventHubCaptureFileCreatedEventData eventData = (EventHubCaptureFileCreatedEventData)events[0].data();
        Assert.assertEquals("AzureBlockBlob", eventData.fileType());
    }

    // Maps events
    @Test
    public void consumeMapsGeoFenceEnteredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceEnteredEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MapsGeofenceEnteredEventData);
        MapsGeofenceEnteredEventData eventData = (MapsGeofenceEnteredEventData)events[0].data();
        Assert.assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceExitedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceExitedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MapsGeofenceExitedEventData);
        MapsGeofenceExitedEventData eventData = (MapsGeofenceExitedEventData)events[0].data();
        Assert.assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceResultEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceResultEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MapsGeofenceResultEventData);
        MapsGeofenceResultEventData eventData = (MapsGeofenceResultEventData)events[0].data();
        Assert.assertEquals(true, eventData.isEventPublished());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteFailureEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceWriteFailureData);
        ResourceWriteFailureData eventData = (ResourceWriteFailureData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceWriteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteCancelEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceWriteCancelData);
        ResourceWriteCancelData eventData = (ResourceWriteCancelData) events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceDeleteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteSuccessEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceDeleteSuccessData);
        ResourceDeleteSuccessData eventData = (ResourceDeleteSuccessData) events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceDeleteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteFailureEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceDeleteFailureData);
        ResourceDeleteFailureData eventData = (ResourceDeleteFailureData) events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceDeleteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteCancelEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceDeleteCancelData);
        ResourceDeleteCancelData eventData = (ResourceDeleteCancelData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceActionSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionSuccessEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceActionSuccessData);
        ResourceActionSuccessData eventData = (ResourceActionSuccessData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceActionFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionFailureEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceActionFailureData);
        ResourceActionFailureData eventData = (ResourceActionFailureData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    @Test
    public void consumeResourceActionCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionCancelEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceActionCancelData);
        ResourceActionCancelData eventData = (ResourceActionCancelData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    // ServiceBus events
        @Test
    public void consumeServiceBusActiveMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusActiveMessagesAvailableWithNoListenersEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ServiceBusActiveMessagesAvailableWithNoListenersEventData);
        ServiceBusActiveMessagesAvailableWithNoListenersEventData eventData = (ServiceBusActiveMessagesAvailableWithNoListenersEventData) events[0].data();
        Assert.assertEquals("testns1", eventData.namespaceName());
    }

        @Test
    public void consumeServiceBusDeadletterMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusDeadletterMessagesAvailableWithNoListenersEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
        ServiceBusDeadletterMessagesAvailableWithNoListenersEventData eventData = (ServiceBusDeadletterMessagesAvailableWithNoListenersEventData)events[0].data();
        Assert.assertEquals("testns1", eventData.namespaceName());
    }

    // Storage events
        @Test
    public void consumeStorageBlobCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobCreatedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof StorageBlobCreatedEventData);
        StorageBlobCreatedEventData eventData = (StorageBlobCreatedEventData) events[0].data();
        Assert.assertEquals("https://myaccount.blob.core.windows.net/testcontainer/file1.txt", eventData.url());
    }

        @Test
    public void consumeStorageBlobDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData)events[0].data();
        Assert.assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.url());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteSuccessEvent() throws IOException  {
        String jsonData = getTestPayloadFromFile("ResourceWriteSuccessEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceWriteSuccessData);
        ResourceWriteSuccessData eventData = (ResourceWriteSuccessData)events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    // TODO: When new event types are introduced, add one test here for each event type

    private String getTestPayloadFromFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            byte[] bytes = IOUtils.readFully(classLoader.getResourceAsStream("customization\\" + fileName), -1, true);
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
