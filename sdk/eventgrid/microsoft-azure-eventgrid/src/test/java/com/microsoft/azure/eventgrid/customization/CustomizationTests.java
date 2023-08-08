// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid.customization;

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
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.eventgrid.models.EventHubCaptureFileCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceConnectedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceCreatedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceDeletedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceDisconnectedEventData;
import com.microsoft.azure.eventgrid.models.IotHubDeviceTelemetryEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceEnteredEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceExitedEventData;
import com.microsoft.azure.eventgrid.models.MapsGeofenceResultEventData;
import com.microsoft.azure.eventgrid.models.MediaJobCanceledEventData;
import com.microsoft.azure.eventgrid.models.MediaJobCancelingEventData;
import com.microsoft.azure.eventgrid.models.MediaJobErrorCategory;
import com.microsoft.azure.eventgrid.models.MediaJobErrorCode;
import com.microsoft.azure.eventgrid.models.MediaJobErroredEventData;
import com.microsoft.azure.eventgrid.models.MediaJobFinishedEventData;
import com.microsoft.azure.eventgrid.models.MediaJobOutputAsset;
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
import com.microsoft.azure.eventgrid.models.MediaJobState;
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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

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
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[0].data();
        Assert.assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.url());
    }

    @Test
    public void consumeCustomEvents() throws IOException {
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

        ContosoItemReceivedEventData[] arr = {new ContosoItemReceivedEventData()};
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
        ContosoItemSentEventData eventData0 = (ContosoItemSentEventData) events[0].data();
        Assert.assertTrue(eventData0.shippingInfo() instanceof DroneShippingInfo);
        ContosoItemSentEventData eventData1 = (ContosoItemSentEventData) events[1].data();
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
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[2].data();
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
        AppConfigurationKeyValueDeletedEventData eventData = (AppConfigurationKeyValueDeletedEventData) events[0].data();
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
        AppConfigurationKeyValueModifiedEventData eventData = (AppConfigurationKeyValueModifiedEventData) events[0].data();
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
        ContainerRegistryImagePushedEventData eventData = (ContainerRegistryImagePushedEventData) events[0].data();
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
        ContainerRegistryImageDeletedEventData eventData = (ContainerRegistryImageDeletedEventData) events[0].data();
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
        ContainerRegistryChartDeletedEventData eventData = (ContainerRegistryChartDeletedEventData) events[0].data();
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
        ContainerRegistryChartPushedEventData eventData = (ContainerRegistryChartPushedEventData) events[0].data();
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
        IotHubDeviceCreatedEventData eventData = (IotHubDeviceCreatedEventData) events[0].data();
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
        IotHubDeviceDeletedEventData eventData = (IotHubDeviceDeletedEventData) events[0].data();
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
        IotHubDeviceConnectedEventData eventData = (IotHubDeviceConnectedEventData) events[0].data();
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
        IotHubDeviceDisconnectedEventData eventData = (IotHubDeviceDisconnectedEventData) events[0].data();
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
        IotHubDeviceTelemetryEventData eventData = (IotHubDeviceTelemetryEventData) events[0].data();
        Assert.assertEquals("Active", eventData.properties().get("Status"));
    }

    // EventGrid events
    @Test
    public void consumeEventGridSubscriptionValidationEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionValidationEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof SubscriptionValidationEventData);
        SubscriptionValidationEventData eventData = (SubscriptionValidationEventData) events[0].data();
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
        SubscriptionDeletedEventData eventData = (SubscriptionDeletedEventData) events[0].data();
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
        EventHubCaptureFileCreatedEventData eventData = (EventHubCaptureFileCreatedEventData) events[0].data();
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
        MapsGeofenceEnteredEventData eventData = (MapsGeofenceEnteredEventData) events[0].data();
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
        MapsGeofenceExitedEventData eventData = (MapsGeofenceExitedEventData) events[0].data();
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
        MapsGeofenceResultEventData eventData = (MapsGeofenceResultEventData) events[0].data();
        Assert.assertEquals(true, eventData.isEventPublished());
    }

    // Media Services events
    @Test
    public void consumeMediaJobCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobCanceledEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobCanceledEventData);
        MediaJobCanceledEventData eventData = (MediaJobCanceledEventData) events[0].data();
        Assert.assertEquals(MediaJobState.CANCELING, eventData.previousState());
        Assert.assertEquals(MediaJobState.CANCELED, eventData.state());
        Assert.assertEquals(1, eventData.outputs().size());
        Assert.assertTrue(eventData.outputs().get(0) instanceof MediaJobOutputAsset);

        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.outputs().get(0);

        Assert.assertEquals(MediaJobState.CANCELED, outputAsset.state());
        Assert.assertNull(outputAsset.error());
        Assert.assertNotEquals(100, outputAsset.progress());
        Assert.assertEquals("output-7a8215f9-0f8d-48a6-82ed-1ead772bc221", outputAsset.assetName());
    }

    @Test
    public void consumeMediaJobCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobCancelingEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobCancelingEventData);
        MediaJobCancelingEventData eventData = (MediaJobCancelingEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.CANCELING, eventData.state());
    }

    @Test
    public void consumeMediaJobProcessingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobProcessingEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobProcessingEventData);
        MediaJobProcessingEventData eventData = (MediaJobProcessingEventData) events[0].data();
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.previousState());
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.state());
    }

    @Test
    public void consumeMediaJobFinishedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobFinishedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobFinishedEventData);
        MediaJobFinishedEventData eventData = (MediaJobFinishedEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.FINISHED, eventData.state());
        Assert.assertEquals(1, eventData.outputs().size());
        Assert.assertTrue(eventData.outputs().get(0) instanceof MediaJobOutputAsset);
        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.outputs().get(0);

        Assert.assertEquals(MediaJobState.FINISHED, outputAsset.state());
        Assert.assertNull(outputAsset.error());
        Assert.assertEquals(100, outputAsset.progress());
        Assert.assertEquals("output-298338bb-f8d1-4d0f-9fde-544e0ac4d983", outputAsset.assetName());
    }

    @Test
    public void consumeMediaJobErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobErroredEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobErroredEventData);
        MediaJobErroredEventData eventData = (MediaJobErroredEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.ERROR, eventData.state());
        Assert.assertEquals(1, eventData.outputs().size());
        Assert.assertTrue(eventData.outputs().get(0) instanceof MediaJobOutputAsset);

        Assert.assertEquals(MediaJobState.ERROR, eventData.outputs().get(0).state());
        Assert.assertNotNull(eventData.outputs().get(0).error());
        Assert.assertEquals(MediaJobErrorCategory.SERVICE, eventData.outputs().get(0).error().category());
        Assert.assertEquals(MediaJobErrorCode.SERVICE_ERROR, eventData.outputs().get(0).error().code());
    }

    @Test
    public void consumeMediaJobOutputStateChangeEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputStateChangeEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputStateChangeEventData);
        MediaJobOutputStateChangeEventData eventData = (MediaJobOutputStateChangeEventData) events[0].data();
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.previousState());
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.output();
        Assert.assertEquals("output-2ac2fe75-6557-4de5-ab25-5713b74a6901", outputAsset.assetName());
    }

    @Test
    public void consumeMediaJobScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobScheduledEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobScheduledEventData);
        MediaJobScheduledEventData eventData = (MediaJobScheduledEventData) events[0].data();
        Assert.assertEquals(MediaJobState.QUEUED, eventData.previousState());
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.state());
    }

    @Test
    public void consumeMediaJobOutputCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCanceledEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputCanceledEventData);
        MediaJobOutputCanceledEventData eventData = (MediaJobOutputCanceledEventData) events[0].data();
        Assert.assertEquals(MediaJobState.CANCELING, eventData.previousState());
        Assert.assertEquals(MediaJobState.CANCELED, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCancelingEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputCancelingEventData);
        MediaJobOutputCancelingEventData eventData = (MediaJobOutputCancelingEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.CANCELING, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputErroredEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputErroredEventData);
        MediaJobOutputErroredEventData eventData = (MediaJobOutputErroredEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.ERROR, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
        Assert.assertNotNull(eventData.output().error());
        Assert.assertEquals(MediaJobErrorCategory.SERVICE, eventData.output().error().category());
        Assert.assertEquals(MediaJobErrorCode.SERVICE_ERROR, eventData.output().error().code());
    }

    @Test
    public void consumeMediaJobOutputFinishedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputFinishedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputFinishedEventData);
        MediaJobOutputFinishedEventData eventData = (MediaJobOutputFinishedEventData) events[0].data();
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.previousState());
        Assert.assertEquals(MediaJobState.FINISHED, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
        Assert.assertEquals(100, eventData.output().progress());

        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.output();
        Assert.assertEquals("output-2ac2fe75-6557-4de5-ab25-5713b74a6901", outputAsset.assetName());
    }

    @Test
    public void consumeMediaJobOutputProcessingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProcessingEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputProcessingEventData);
        MediaJobOutputProcessingEventData eventData = (MediaJobOutputProcessingEventData) events[0].data();
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.previousState());
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputScheduledEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputScheduledEventData);
        MediaJobOutputScheduledEventData eventData = (MediaJobOutputScheduledEventData) events[0].data();
        Assert.assertEquals(MediaJobState.QUEUED, eventData.previousState());
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.output().state());
        Assert.assertTrue(eventData.output() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputProgressEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProgressEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobOutputProgressEventData);
        MediaJobOutputProgressEventData eventData = (MediaJobOutputProgressEventData) events[0].data();
        Assert.assertEquals("TestLabel", eventData.label());
        Assert.assertTrue(eventData.jobCorrelationData().containsKey("Field1"));
        Assert.assertEquals("test1", eventData.jobCorrelationData().get("Field1"));
        Assert.assertTrue(eventData.jobCorrelationData().containsKey("Field2"));
        Assert.assertEquals("test2", eventData.jobCorrelationData().get("Field2"));
    }

    @Test
    public void consumeMediaJobStateChangeEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobStateChangeEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaJobStateChangeEventData);
        MediaJobStateChangeEventData eventData = (MediaJobStateChangeEventData) events[0].data();
        Assert.assertEquals(MediaJobState.SCHEDULED, eventData.previousState());
        Assert.assertEquals(MediaJobState.PROCESSING, eventData.state());
    }

    @Test
    public void consumeMediaLiveEventEncoderConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderConnectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventEncoderConnectedEventData);
        MediaLiveEventEncoderConnectedEventData eventData = (MediaLiveEventEncoderConnectedEventData) events[0].data();
        Assert.assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.ingestUrl());
        Assert.assertEquals("Mystream1", eventData.streamId());
        Assert.assertEquals("<ip address>", eventData.encoderIp());
        Assert.assertEquals("3557", eventData.encoderPort());
    }

    @Test
    public void consumeMediaLiveEventConnectionRejectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventConnectionRejectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventConnectionRejectedEventData);
        MediaLiveEventConnectionRejectedEventData eventData = (MediaLiveEventConnectionRejectedEventData) events[0].data();
        Assert.assertEquals("Mystream1", eventData.streamId());
    }

    @Test
    public void consumeMediaLiveEventEncoderDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderDisconnectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventEncoderDisconnectedEventData);
        MediaLiveEventEncoderDisconnectedEventData eventData = (MediaLiveEventEncoderDisconnectedEventData) events[0].data();
        Assert.assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.ingestUrl());
        Assert.assertEquals("Mystream1", eventData.streamId());
        Assert.assertEquals("<ip address>", eventData.encoderIp());
        Assert.assertEquals("3557", eventData.encoderPort());
    }

    @Test
    public void consumeMediaLiveEventIncomingStreamReceivedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingStreamReceivedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventIncomingStreamReceivedEventData);
        MediaLiveEventIncomingStreamReceivedEventData eventData = (MediaLiveEventIncomingStreamReceivedEventData) events[0].data();
        Assert.assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.ingestUrl());
        Assert.assertEquals("<ip address>", eventData.encoderIp());
        Assert.assertEquals("3557", eventData.encoderPort());

        Assert.assertEquals("audio", eventData.trackType());
        Assert.assertEquals("audio_160000", eventData.trackName());
        Assert.assertEquals("66", eventData.timestamp());
        Assert.assertEquals("1950", eventData.duration());
        Assert.assertEquals("1000", eventData.timescale());
    }

    @Test
    public void consumeMediaLiveEventIncomingStreamsOutOfSyncEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingStreamsOutOfSyncEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventIncomingStreamsOutOfSyncEventData);
        MediaLiveEventIncomingStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingStreamsOutOfSyncEventData) events[0].data();
        Assert.assertEquals("10999", eventData.minLastTimestamp());
        Assert.assertEquals("video", eventData.typeOfStreamWithMinLastTimestamp());
        Assert.assertEquals("100999", eventData.maxLastTimestamp());
        Assert.assertEquals("audio", eventData.typeOfStreamWithMaxLastTimestamp());
        Assert.assertEquals("1000", eventData.timescaleOfMinLastTimestamp());
        Assert.assertEquals("1000", eventData.timescaleOfMaxLastTimestamp());
    }

    @Test
    public void consumeMediaLiveEventIncomingVideoStreamsOutOfSyncEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingVideoStreamsOutOfSyncEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventIncomingVideoStreamsOutOfSyncEventData);
        MediaLiveEventIncomingVideoStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingVideoStreamsOutOfSyncEventData) events[0].data();
        Assert.assertEquals("10999", eventData.firstTimestamp());
        Assert.assertEquals("2000", eventData.firstDuration());
        Assert.assertEquals("100999", eventData.secondTimestamp());
        Assert.assertEquals("2000", eventData.secondDuration());
        Assert.assertEquals("1000", eventData.timescale());
    }

    @Test
    public void consumeMediaLiveEventIncomingDataChunkDroppedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingDataChunkDroppedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventIncomingDataChunkDroppedEventData);
        MediaLiveEventIncomingDataChunkDroppedEventData eventData = (MediaLiveEventIncomingDataChunkDroppedEventData) events[0].data();
        Assert.assertEquals("8999", eventData.timestamp());
        Assert.assertEquals("video", eventData.trackType());
        Assert.assertEquals("video1", eventData.trackName());
        Assert.assertEquals("1000", eventData.timescale());
        Assert.assertEquals("FragmentDrop_OverlapTimestamp", eventData.resultCode());
    }

    @Test
    public void consumeMediaLiveEventIngestHeartbeatEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIngestHeartbeatEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventIngestHeartbeatEventData);
        MediaLiveEventIngestHeartbeatEventData eventData = (MediaLiveEventIngestHeartbeatEventData) events[0].data();
        Assert.assertEquals("video", eventData.trackType());
        Assert.assertEquals("video", eventData.trackName());
        Assert.assertEquals("11999", eventData.lastTimestamp());
        Assert.assertEquals("1000", eventData.timescale());
        Assert.assertTrue(eventData.unexpectedBitrate());
        Assert.assertEquals("Running", eventData.state());
        Assert.assertFalse(eventData.healthy());
    }

    @Test
    public void consumeMediaLiveEventTrackDiscontinuityDetectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventTrackDiscontinuityDetectedEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof MediaLiveEventTrackDiscontinuityDetectedEventData);
        MediaLiveEventTrackDiscontinuityDetectedEventData eventData = (MediaLiveEventTrackDiscontinuityDetectedEventData) events[0].data();
        Assert.assertEquals("video", eventData.trackType());
        Assert.assertEquals("video", eventData.trackName());
        Assert.assertEquals("10999", eventData.previousTimestamp());
        Assert.assertEquals("14999", eventData.newTimestamp());
        Assert.assertEquals("1000", eventData.timescale());
        Assert.assertEquals("4000", eventData.discontinuityGap());
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
        ResourceWriteFailureData eventData = (ResourceWriteFailureData) events[0].data();
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
        ResourceDeleteCancelData eventData = (ResourceDeleteCancelData) events[0].data();
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
        ResourceActionSuccessData eventData = (ResourceActionSuccessData) events[0].data();
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
        ResourceActionFailureData eventData = (ResourceActionFailureData) events[0].data();
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
        ResourceActionCancelData eventData = (ResourceActionCancelData) events[0].data();
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
        ServiceBusDeadletterMessagesAvailableWithNoListenersEventData eventData = (ServiceBusDeadletterMessagesAvailableWithNoListenersEventData) events[0].data();
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
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[0].data();
        Assert.assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.url());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteSuccessEvent.json");
        //
        EventGridSubscriber eventGridSubscriber = new EventGridSubscriber();
        EventGridEvent[] events = eventGridSubscriber.deserializeEventGridEvents(jsonData);

        Assert.assertNotNull(events);
        Assert.assertTrue(events[0].data() instanceof ResourceWriteSuccessData);
        ResourceWriteSuccessData eventData = (ResourceWriteSuccessData) events[0].data();
        Assert.assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.tenantId());
    }

    // TODO: When new event types are introduced, add one test here for each event type

    private String getTestPayloadFromFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            byte[] bytes = IOUtils.toByteArray(classLoader.getResourceAsStream("customization/" + fileName));
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
