// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.models.CloudEvent;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.messaging.eventgrid.implementation.models.ContosoItemReceivedEventData;
import com.azure.messaging.eventgrid.implementation.models.ContosoItemSentEventData;
import com.azure.messaging.eventgrid.implementation.models.DroneShippingInfo;
import com.azure.messaging.eventgrid.implementation.models.RocketShippingInfo;
import com.azure.messaging.eventgrid.systemevents.AcsRouterJobClassificationFailedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.AppConfigurationKeyValueModifiedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryChartPushedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImageDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.ContainerRegistryImagePushedEventData;
import com.azure.messaging.eventgrid.systemevents.EventHubCaptureFileCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceType;
import com.azure.messaging.eventgrid.systemevents.HealthcareFhirResourceUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceConnectedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceDisconnectedEventData;
import com.azure.messaging.eventgrid.systemevents.IotHubDeviceTelemetryEventData;
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
import com.azure.messaging.eventgrid.systemevents.MediaJobErrorCategory;
import com.azure.messaging.eventgrid.systemevents.MediaJobErrorCode;
import com.azure.messaging.eventgrid.systemevents.MediaJobErroredEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobFinishedEventData;
import com.azure.messaging.eventgrid.systemevents.MediaJobOutputAsset;
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
import com.azure.messaging.eventgrid.systemevents.MediaJobState;
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
import com.azure.messaging.eventgrid.systemevents.ResourceActionCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceActionSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceDeleteSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteCancelEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteFailureEventData;
import com.azure.messaging.eventgrid.systemevents.ResourceWriteSuccessEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusActiveMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.ServiceBusDeadletterMessagesAvailableWithNoListenersEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageBlobRenamedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryCreatedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryDeletedEventData;
import com.azure.messaging.eventgrid.systemevents.StorageDirectoryRenamedEventData;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeserializationTests {
    static <T> Object toSystemEventData(EventGridEvent event) {
        return getSystemEventData(event.getData(), event.getEventType());
    }
    static <T> Object toSystemEventData(CloudEvent event) {
        return getSystemEventData(event.getData(), event.getType());
    }

    static Object getSystemEventData(BinaryData data, String eventType) {
        if (SystemEventNames.getSystemEventMappings().containsKey(eventType)) {
            return data
                .toObject(TypeReference.createInstance(SystemEventNames.getSystemEventMappings().get(eventType)));
        }
        return null;
    }

    @ParameterizedTest
    @MethodSource("getObjectsForRoundTrip")
    public void testEventGridRoundTripStreamSerialization(BinaryData payload) {
        EventGridEvent eventGridEvent = new EventGridEvent("subject", "eventType", payload,
            "dataVersion");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            JsonWriter writer = JsonProviders.createWriter(stream);
            eventGridEvent.toJson(writer);
            writer.flush();
            try (JsonReader reader = JsonProviders.createReader(stream.toByteArray())) {
                EventGridEvent deserializedEvent = EventGridEvent.fromJson(reader);
                assertEquals(eventGridEvent.getSubject(), deserializedEvent.getSubject());
                assertEquals(eventGridEvent.getEventType(), deserializedEvent.getEventType());
                assertArrayEquals(eventGridEvent.getData().toBytes(), deserializedEvent.getData().toBytes());
                assertEquals(eventGridEvent.getDataVersion(), deserializedEvent.getDataVersion());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<Arguments> getObjectsForRoundTrip() {
        return Stream.of(
            Arguments.of(BinaryData.fromObject(1)),
            Arguments.of(BinaryData.fromObject("data")),
            Arguments.of(BinaryData.fromString("{\"data\":\"data\"}")),
            Arguments.of(BinaryData.fromObject(true))
        );
    }

    @Test
    public void consumeStorageBlobDeletedEventWithExtraProperty() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEventWithExtraProperty.json");
        //


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(StorageBlobDeletedEventData.class, toSystemEventData(events[0]));
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) toSystemEventData(events[0]);
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeEventGridEventWithoutArrayBrackets() throws IOException {
        // using a storageBlobDeletedEvent
        String jsonData = getTestPayloadFromFile("EventGridEventNoArray.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        assertInstanceOf(StorageBlobDeletedEventData.class, toSystemEventData(events[0]));
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) toSystemEventData(events[0]);
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeEventGridEventWithNullData() throws IOException {
        // using a storageBlobDeletedEvent
        String jsonData = getTestPayloadFromFile("EventGridNullData.json");
        //
        assertThrows(IllegalArgumentException.class, () -> {
            EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);
        });
    }

    @Test
    public void consumeCustomEvents() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEvents.json");


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        assertNotNull(events[0].getData().toObject(TypeReference.createInstance(ContosoItemReceivedEventData.class)));
        ContosoItemReceivedEventData eventData = events[0].getData().toObject(TypeReference.createInstance(ContosoItemReceivedEventData.class));
        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.getItemSku());
    }

    @Test
    public void consumeCustomEventWithArrayData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithArrayData.json");
        //


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        List<ContosoItemReceivedEventData> eventData = events[0].getData().toObject(new TypeReference<List<ContosoItemReceivedEventData>>() {});
        assertNotNull(eventData);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", (eventData.get(0)).getItemSku());
    }

    @Test
    public void consumeCustomEventWithBooleanData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithBooleanData.json");


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);

        Boolean eventData = events[0].getData().toObject(TypeReference.createInstance(Boolean.class));
        assertNotNull(eventData);

        assertTrue(eventData);
    }

    @Test
    public void consumeCustomEventWithStringData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithStringData.json");
        //


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);

        String eventData = events[0].getData().toObject(String.class);
        assertNotNull(eventData);

        assertEquals("stringdata", eventData);
    }

    @Test
    public void consumeCustomEventWithPolymorphicData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithPolymorphicData.json");


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(2, events.length);

        ContosoItemSentEventData eventData0 = events[0].getData().toObject(ContosoItemSentEventData.class);
        ContosoItemSentEventData eventData1 = events[1].getData().toObject(ContosoItemSentEventData.class);

        assertNotNull(eventData0);
        assertNotNull(eventData1);

        assertInstanceOf(DroneShippingInfo.class, eventData0.getShippingInfo());

        assertInstanceOf(RocketShippingInfo.class, eventData1.getShippingInfo());
    }


    @Test
    public void consumeMultipleEventsInSameBatch() throws IOException {
        String jsonData = getTestPayloadFromFile("MultipleEventsInSameBatch.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(4, events.length);
        assertInstanceOf(StorageBlobCreatedEventData.class, toSystemEventData(events[0]));
        assertInstanceOf(StorageBlobDeletedEventData.class, toSystemEventData(events[1]));
        assertInstanceOf(StorageBlobDeletedEventData.class, toSystemEventData(events[2]));
        assertInstanceOf(ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class, toSystemEventData(events[3]));
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) toSystemEventData(events[2]);
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    // AppConfiguration events
    @Test
    public void consumeAppConfigurationKeyValueDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueDeleted.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(AppConfigurationKeyValueDeletedEventData.class, toSystemEventData(events[0]));
        AppConfigurationKeyValueDeletedEventData eventData = (AppConfigurationKeyValueDeletedEventData) toSystemEventData(events[0]);
        assertEquals("key1", eventData.getKey());
    }

    @Test
    public void consumeAppConfigurationKeyValueModifiedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueModified.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(AppConfigurationKeyValueModifiedEventData.class, toSystemEventData(events[0]));
        AppConfigurationKeyValueModifiedEventData eventData = (AppConfigurationKeyValueModifiedEventData) toSystemEventData(events[0]);
        assertEquals("key1", eventData.getKey());
    }

    // ContainerRegistry events
    @Test
    public void consumeContainerRegistryImagePushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImagePushedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ContainerRegistryImagePushedEventData.class, toSystemEventData(events[0]));
        ContainerRegistryImagePushedEventData eventData = (ContainerRegistryImagePushedEventData) toSystemEventData(events[0]);
        assertEquals("127.0.0.1", eventData.getRequest().getAddr());
    }

    @Test
    public void consumeContainerRegistryImageDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImageDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ContainerRegistryImageDeletedEventData.class, toSystemEventData(events[0]));
        ContainerRegistryImageDeletedEventData eventData = (ContainerRegistryImageDeletedEventData) toSystemEventData(events[0]);
        assertEquals("testactor", eventData.getActor().getName());
    }

    @Test
    public void consumeContainerRegistryChartDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ContainerRegistryChartDeletedEventData.class, toSystemEventData(events[0]));
        ContainerRegistryChartDeletedEventData eventData = (ContainerRegistryChartDeletedEventData) toSystemEventData(events[0]);
        assertEquals("mediatype1", eventData.getTarget().getMediaType());
    }

    @Test
    public void consumeContainerRegistryChartPushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartPushedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ContainerRegistryChartPushedEventData.class, toSystemEventData(events[0]));
        ContainerRegistryChartPushedEventData eventData = (ContainerRegistryChartPushedEventData) toSystemEventData(events[0]);
        assertEquals("mediatype1", eventData.getTarget().getMediaType());
    }

    // IoTHub Device events
    @Test
    public void consumeIoTHubDeviceCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(IotHubDeviceCreatedEventData.class, toSystemEventData(events[0]));
        IotHubDeviceCreatedEventData eventData = (IotHubDeviceCreatedEventData) toSystemEventData(events[0]);
        assertEquals("enabled", eventData.getTwin().getStatus());
    }

    @Test
    public void consumeIoTHubDeviceDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(IotHubDeviceDeletedEventData.class, toSystemEventData(events[0]));
        IotHubDeviceDeletedEventData eventData = (IotHubDeviceDeletedEventData) toSystemEventData(events[0]);
        assertEquals("AAAAAAAAAAE=", eventData.getTwin().getEtag());
    }

    @Test
    public void consumeIoTHubDeviceConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceConnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(IotHubDeviceConnectedEventData.class, toSystemEventData(events[0]));
        IotHubDeviceConnectedEventData eventData = (IotHubDeviceConnectedEventData) toSystemEventData(events[0]);
        assertEquals("EGTESTHUB1", eventData.getHubName());
    }

    @Test
    public void consumeIoTHubDeviceDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(IotHubDeviceDisconnectedEventData.class, toSystemEventData(events[0]));
        IotHubDeviceDisconnectedEventData eventData = (IotHubDeviceDisconnectedEventData) toSystemEventData(events[0]);
        assertEquals("000000000000000001D4132452F67CE200000002000000000000000000000002", eventData.getDeviceConnectionStateEventInfo().getSequenceNumber());
    }

    @Test
    public void consumeIoTHubDeviceTelemetryEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceTelemetryEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(IotHubDeviceTelemetryEventData.class, toSystemEventData(events[0]));
        IotHubDeviceTelemetryEventData eventData = (IotHubDeviceTelemetryEventData) toSystemEventData(events[0]);
        assertEquals("Active", eventData.getProperties().get("Status"));
    }

    // EventGrid events
    @Test
    public void consumeEventGridSubscriptionValidationEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionValidationEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(SubscriptionValidationEventData.class, toSystemEventData(events[0]));
        SubscriptionValidationEventData eventData = (SubscriptionValidationEventData) toSystemEventData(events[0]);
        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.getValidationCode());
    }

    @Test
    public void consumeEventGridSubscriptionDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(SubscriptionDeletedEventData.class, toSystemEventData(events[0]));
        SubscriptionDeletedEventData eventData = (SubscriptionDeletedEventData) toSystemEventData(events[0]);
        assertEquals("/subscriptions/id/resourceGroups/rg/providers/Microsoft.EventGrid/topics/topic1/providers/Microsoft.EventGrid/eventSubscriptions/eventsubscription1", eventData.getEventSubscriptionId());
    }

    // Event Hub Events
    @Test
    public void consumeEventHubCaptureFileCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventHubCaptureFileCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(EventHubCaptureFileCreatedEventData.class, toSystemEventData(events[0]));
        EventHubCaptureFileCreatedEventData eventData = (EventHubCaptureFileCreatedEventData) toSystemEventData(events[0]);
        assertEquals("AzureBlockBlob", eventData.getFileType());
    }

    // Maps events
    @Test
    public void consumeMapsGeoFenceEnteredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceEnteredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MapsGeofenceEnteredEventData.class, toSystemEventData(events[0]));
        MapsGeofenceEnteredEventData eventData = (MapsGeofenceEnteredEventData) toSystemEventData(events[0]);
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceExitedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceExitedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MapsGeofenceExitedEventData.class, toSystemEventData(events[0]));
        MapsGeofenceExitedEventData eventData = (MapsGeofenceExitedEventData) toSystemEventData(events[0]);
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceResultEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceResultEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MapsGeofenceResultEventData.class, toSystemEventData(events[0]));
        MapsGeofenceResultEventData eventData = (MapsGeofenceResultEventData) toSystemEventData(events[0]);
        assertEquals(true, eventData.isEventPublished());
    }

    // Media Services events
    @Test
    public void consumeMediaJobCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobCanceledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobCanceledEventData.class, toSystemEventData(events[0]));
        MediaJobCanceledEventData eventData = (MediaJobCanceledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.CANCELING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELED, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutputs().get(0));

        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.getOutputs().get(0);

        assertEquals(MediaJobState.CANCELED, outputAsset.getState());
        assertNull(outputAsset.getError());
        assertNotEquals(100, outputAsset.getProgress());
        assertEquals("output-7a8215f9-0f8d-48a6-82ed-1ead772bc221", outputAsset.getAssetName());
    }

    @Test
    public void consumeMediaJobCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobCancelingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobCancelingEventData.class, toSystemEventData(events[0]));
        MediaJobCancelingEventData eventData = (MediaJobCancelingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELING, eventData.getState());
    }

    @Test
    public void consumeMediaJobProcessingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobProcessingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobProcessingEventData.class, toSystemEventData(events[0]));
        MediaJobProcessingEventData eventData = (MediaJobProcessingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getState());
    }

    @Test
    public void consumeMediaJobFinishedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobFinishedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);
        assertNotNull(events);
        assertInstanceOf(MediaJobFinishedEventData.class, toSystemEventData(events[0]));
        MediaJobFinishedEventData eventData = (MediaJobFinishedEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.FINISHED, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutputs().get(0));
        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.getOutputs().get(0);

        assertEquals(MediaJobState.FINISHED, outputAsset.getState());
        assertNull(outputAsset.getError());
        assertEquals(100, outputAsset.getProgress());
        assertEquals("output-298338bb-f8d1-4d0f-9fde-544e0ac4d983", outputAsset.getAssetName());
    }

    @Test
    public void consumeMediaJobErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobErroredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobErroredEventData.class, toSystemEventData(events[0]));
        MediaJobErroredEventData eventData = (MediaJobErroredEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.ERROR, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutputs().get(0));

        assertEquals(MediaJobState.ERROR, eventData.getOutputs().get(0).getState());
        assertNotNull(eventData.getOutputs().get(0).getError());
        assertEquals(MediaJobErrorCategory.SERVICE, eventData.getOutputs().get(0).getError().getCategory());
        assertEquals(MediaJobErrorCode.SERVICE_ERROR, eventData.getOutputs().get(0).getError().getCode());
    }

    @Test
    public void consumeMediaJobOutputStateChangeEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputStateChangeEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputStateChangeEventData.class, toSystemEventData(events[0]));
        MediaJobOutputStateChangeEventData eventData = (MediaJobOutputStateChangeEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.getOutput();
        assertEquals("output-2ac2fe75-6557-4de5-ab25-5713b74a6901", outputAsset.getAssetName());
    }

    @Test
    public void consumeMediaJobScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobScheduledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobScheduledEventData.class, toSystemEventData(events[0]));
        MediaJobScheduledEventData eventData = (MediaJobScheduledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.QUEUED, eventData.getPreviousState());
        assertEquals(MediaJobState.SCHEDULED, eventData.getState());
    }

    @Test
    public void consumeMediaJobOutputCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCanceledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputCanceledEventData.class, toSystemEventData(events[0]));
        MediaJobOutputCanceledEventData eventData = (MediaJobOutputCanceledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.CANCELING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELED, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
    }

    @Test
    public void consumeMediaJobOutputCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCancelingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputCancelingEventData.class, toSystemEventData(events[0]));
        MediaJobOutputCancelingEventData eventData = (MediaJobOutputCancelingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELING, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
    }

    @Test
    public void consumeMediaJobOutputErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputErroredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputErroredEventData.class, toSystemEventData(events[0]));
        MediaJobOutputErroredEventData eventData = (MediaJobOutputErroredEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.ERROR, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
        assertNotNull(eventData.getOutput().getError());
        assertEquals(MediaJobErrorCategory.SERVICE, eventData.getOutput().getError().getCategory());
        assertEquals(MediaJobErrorCode.SERVICE_ERROR, eventData.getOutput().getError().getCode());
    }

    @Test
    public void consumeMediaJobOutputFinishedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputFinishedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputFinishedEventData.class, toSystemEventData(events[0]));
        MediaJobOutputFinishedEventData eventData = (MediaJobOutputFinishedEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.FINISHED, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
        assertEquals(100, eventData.getOutput().getProgress());

        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.getOutput();
        assertEquals("output-2ac2fe75-6557-4de5-ab25-5713b74a6901", outputAsset.getAssetName());
    }

    @Test
    public void consumeMediaJobOutputProcessingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProcessingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputProcessingEventData.class, toSystemEventData(events[0]));
        MediaJobOutputProcessingEventData eventData = (MediaJobOutputProcessingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
    }

    @Test
    public void consumeMediaJobOutputScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputScheduledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputScheduledEventData.class, toSystemEventData(events[0]));
        MediaJobOutputScheduledEventData eventData = (MediaJobOutputScheduledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.QUEUED, eventData.getPreviousState());
        assertEquals(MediaJobState.SCHEDULED, eventData.getOutput().getState());
        assertInstanceOf(MediaJobOutputAsset.class, eventData.getOutput());
    }

    @Test
    public void consumeMediaJobOutputProgressEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProgressEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobOutputProgressEventData.class, toSystemEventData(events[0]));
        MediaJobOutputProgressEventData eventData = (MediaJobOutputProgressEventData) toSystemEventData(events[0]);
        assertEquals("TestLabel", eventData.getLabel());
        assertTrue(eventData.getJobCorrelationData().containsKey("Field1"));
        assertEquals("test1", eventData.getJobCorrelationData().get("Field1"));
        assertTrue(eventData.getJobCorrelationData().containsKey("Field2"));
        assertEquals("test2", eventData.getJobCorrelationData().get("Field2"));
    }

    @Test
    public void consumeMediaJobStateChangeEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobStateChangeEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaJobStateChangeEventData.class, toSystemEventData(events[0]));
        MediaJobStateChangeEventData eventData = (MediaJobStateChangeEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getState());
    }

    @Test
    public void consumeMediaLiveEventEncoderConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderConnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventEncoderConnectedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventEncoderConnectedEventData eventData = (MediaLiveEventEncoderConnectedEventData) toSystemEventData(events[0]);
        assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.getIngestUrl());
        assertEquals("Mystream1", eventData.getStreamId());
        assertEquals("<ip address>", eventData.getEncoderIp());
        assertEquals("3557", eventData.getEncoderPort());
    }

    @Test
    public void consumeMediaLiveEventConnectionRejectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventConnectionRejectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventConnectionRejectedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventConnectionRejectedEventData eventData = (MediaLiveEventConnectionRejectedEventData) toSystemEventData(events[0]);
        assertEquals("Mystream1", eventData.getStreamId());
    }

    @Test
    public void consumeMediaLiveEventEncoderDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventEncoderDisconnectedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventEncoderDisconnectedEventData eventData = (MediaLiveEventEncoderDisconnectedEventData) toSystemEventData(events[0]);
        assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.getIngestUrl());
        assertEquals("Mystream1", eventData.getStreamId());
        assertEquals("<ip address>", eventData.getEncoderIp());
        assertEquals("3557", eventData.getEncoderPort());
    }

    @Test
    public void consumeMediaLiveEventIncomingStreamReceivedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingStreamReceivedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventIncomingStreamReceivedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventIncomingStreamReceivedEventData eventData = (MediaLiveEventIncomingStreamReceivedEventData) toSystemEventData(events[0]);
        assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.getIngestUrl());
        assertEquals("<ip address>", eventData.getEncoderIp());
        assertEquals("3557", eventData.getEncoderPort());

        assertEquals("audio", eventData.getTrackType());
        assertEquals("audio_160000", eventData.getTrackName());
        assertEquals("66", eventData.getTimestamp());
        assertEquals("1950", eventData.getDuration());
        assertEquals("1000", eventData.getTimescale());
    }

    @Test
    public void consumeMediaLiveEventIncomingStreamsOutOfSyncEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingStreamsOutOfSyncEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventIncomingStreamsOutOfSyncEventData.class, toSystemEventData(events[0]));
        MediaLiveEventIncomingStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingStreamsOutOfSyncEventData) toSystemEventData(events[0]);
        assertEquals("10999", eventData.getMinLastTimestamp());
        assertEquals("video", eventData.getTypeOfStreamWithMinLastTimestamp());
        assertEquals("100999", eventData.getMaxLastTimestamp());
        assertEquals("audio", eventData.getTypeOfStreamWithMaxLastTimestamp());
        assertEquals("1000", eventData.getTimescaleOfMinLastTimestamp());
        assertEquals("1000", eventData.getTimescaleOfMaxLastTimestamp());
    }

    @Test
    public void consumeMediaLiveEventIncomingVideoStreamsOutOfSyncEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingVideoStreamsOutOfSyncEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventIncomingVideoStreamsOutOfSyncEventData.class, toSystemEventData(events[0]));
        MediaLiveEventIncomingVideoStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingVideoStreamsOutOfSyncEventData) toSystemEventData(events[0]);
        assertEquals("10999", eventData.getFirstTimestamp());
        assertEquals("2000", eventData.getFirstDuration());
        assertEquals("100999", eventData.getSecondTimestamp());
        assertEquals("2000", eventData.getSecondDuration());
        assertEquals("1000", eventData.getTimescale());
    }

    @Test
    public void consumeMediaLiveEventIncomingDataChunkDroppedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingDataChunkDroppedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventIncomingDataChunkDroppedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventIncomingDataChunkDroppedEventData eventData = (MediaLiveEventIncomingDataChunkDroppedEventData) toSystemEventData(events[0]);
        assertEquals("8999", eventData.getTimestamp());
        assertEquals("video", eventData.getTrackType());
        assertEquals("video1", eventData.getTrackName());
        assertEquals("1000", eventData.getTimescale());
        assertEquals("FragmentDrop_OverlapTimestamp", eventData.getResultCode());
    }

    @Test
    public void consumeMediaLiveEventIngestHeartbeatEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIngestHeartbeatEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventIngestHeartbeatEventData.class, toSystemEventData(events[0]));
        MediaLiveEventIngestHeartbeatEventData eventData = (MediaLiveEventIngestHeartbeatEventData) toSystemEventData(events[0]);
        assertEquals("video", eventData.getTrackType());
        assertEquals("video", eventData.getTrackName());
        assertEquals("11999", eventData.getLastTimestamp());
        assertEquals("1000", eventData.getTimescale());
        assertTrue(eventData.isUnexpectedBitrate());
        assertEquals("Running", eventData.getState());
        assertFalse(eventData.isHealthy());
        assertEquals(0, eventData.getIngestDriftValue());
        assertEquals(OffsetDateTime.parse("2021-05-14T23:50:00.00Z"), eventData.getLastFragmentArrivalTime());


        jsonData = "[{  \"topic\": \"/subscriptions/{subscription id}/resourceGroups/{resource group}/providers/Microsoft.Media/mediaservices/{account name}\",  \"subject\": \"liveEvent/liveevent-ec9d26a8\",  \"eventType\": \"Microsoft.Media.LiveEventIngestHeartbeat\",  \"eventTime\": \"2018-10-12T15:52:37.3710102\",  \"id\": \"d84727e2-d9c0-4a21-a66b-8d23f06b3e06\",  \"data\": {    \"trackType\": \"video\",    \"trackName\": \"video\",    \"bitrate\": 2500000,    \"incomingBitrate\": 500726,    \"lastTimestamp\": \"11999\",    \"timescale\": \"1000\",    \"overlapCount\": 0,    \"discontinuityCount\": 0,    \"nonincreasingCount\": 0,    \"unexpectedBitrate\": true,    \"state\": \"Running\",    \"healthy\": false,  \"lastFragmentArrivalTime\": \"2021-05-14T23:50:00.00\", \"ingestDriftValue\": \"n/a\"  },  \"dataVersion\": \"1.0\",  \"metadataVersion\": \"1\"}]";

        events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);

        assertInstanceOf(MediaLiveEventIngestHeartbeatEventData.class, toSystemEventData(events[0]));
        eventData = (MediaLiveEventIngestHeartbeatEventData) toSystemEventData(events[0]);
        // n/a should be translated to null IngestDriftValue
        assertNull(eventData.getIngestDriftValue());
    }

    @Test
    public void consumeMediaLiveEventTrackDiscontinuityDetectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventTrackDiscontinuityDetectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventTrackDiscontinuityDetectedEventData.class, toSystemEventData(events[0]));
        MediaLiveEventTrackDiscontinuityDetectedEventData eventData = (MediaLiveEventTrackDiscontinuityDetectedEventData) toSystemEventData(events[0]);
        assertEquals("video", eventData.getTrackType());
        assertEquals("video", eventData.getTrackName());
        assertEquals("10999", eventData.getPreviousTimestamp());
        assertEquals("14999", eventData.getNewTimestamp());
        assertEquals("1000", eventData.getTimescale());
        assertEquals("4000", eventData.getDiscontinuityGap());
    }

    @Test
    public void consumeMediaLiveEventChannelArchiveHeartbeatEvent() throws IOException {
        String jsonData = "[{  \"topic\": \"/subscriptions/{subscription id}/resourceGroups/{resource group}/providers/Microsoft.Media/mediaservices/{account name}\",  \"subject\": \"liveEvent/mle1\",  \"eventType\": \"Microsoft.Media.LiveEventChannelArchiveHeartbeat\",  \"eventTime\": \"2021-05-14T23:50:00.324\", \"id\": \"7f450938-491f-41e1-b06f-c6cd3965d786\",  \"data\": {    \"channelLatencyMs\": \"10\",    \"latencyResultCode\": \"S_OK\"},  \"dataVersion\": \"1.0\",  \"metadataVersion\": \"1\"}]";
        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventChannelArchiveHeartbeatEventData.class, toSystemEventData(events[0]));
        MediaLiveEventChannelArchiveHeartbeatEventData eventData = (MediaLiveEventChannelArchiveHeartbeatEventData) toSystemEventData(events[0]);

        assertEquals(Duration.ofMillis(10), eventData.getChannelLatency());
        assertEquals("S_OK", eventData.getLatencyResultCode());

        jsonData = "[{  \"topic\": \"/subscriptions/{subscription id}/resourceGroups/{resource group}/providers/Microsoft.Media/mediaservices/{account name}\",  \"subject\": \"liveEvent/mle1\",  \"eventType\": \"Microsoft.Media.LiveEventChannelArchiveHeartbeat\",  \"eventTime\": \"2021-05-14T23:50:00.324\", \"id\": \"7f450938-491f-41e1-b06f-c6cd3965d786\",  \"data\": {    \"channelLatencyMs\": \"n/a\",    \"latencyResultCode\": \"S_OK\"},  \"dataVersion\": \"1.0\",  \"metadataVersion\": \"1\"}]";

        events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(MediaLiveEventChannelArchiveHeartbeatEventData.class, toSystemEventData(events[0]));
        eventData = (MediaLiveEventChannelArchiveHeartbeatEventData) toSystemEventData(events[0]);

        // n/a should be translated to null ChannelLatency
        assertNull(eventData.getChannelLatency());
        assertEquals("S_OK", eventData.getLatencyResultCode());
    }
    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceWriteFailureEventData.class, toSystemEventData(events[0]));
        ResourceWriteFailureEventData eventData = (ResourceWriteFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceWriteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceWriteCancelEventData.class, toSystemEventData(events[0]));
        ResourceWriteCancelEventData eventData = (ResourceWriteCancelEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceDeleteSuccessEventData.class, toSystemEventData(events[0]));
        ResourceDeleteSuccessEventData eventData = (ResourceDeleteSuccessEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceDeleteFailureEventData.class, toSystemEventData(events[0]));
        ResourceDeleteFailureEventData eventData = (ResourceDeleteFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceDeleteCancelEventData.class, toSystemEventData(events[0]));
        ResourceDeleteCancelEventData eventData = (ResourceDeleteCancelEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceActionSuccessEventData.class, toSystemEventData(events[0]));
        ResourceActionSuccessEventData eventData = (ResourceActionSuccessEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceActionFailureEventData.class, toSystemEventData(events[0]));
        ResourceActionFailureEventData eventData = (ResourceActionFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceActionCancelEventData.class, toSystemEventData(events[0]));
        ResourceActionCancelEventData eventData = (ResourceActionCancelEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    // ServiceBus events
    @Test
    public void consumeServiceBusActiveMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusActiveMessagesAvailableWithNoListenersEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ServiceBusActiveMessagesAvailableWithNoListenersEventData.class, toSystemEventData(events[0]));
        ServiceBusActiveMessagesAvailableWithNoListenersEventData eventData = (ServiceBusActiveMessagesAvailableWithNoListenersEventData) toSystemEventData(events[0]);
        assertEquals("testns1", eventData.getNamespaceName());
    }

    @Test
    public void consumeServiceBusDeadletterMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusDeadletterMessagesAvailableWithNoListenersEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ServiceBusDeadletterMessagesAvailableWithNoListenersEventData.class, toSystemEventData(events[0]));
        ServiceBusDeadletterMessagesAvailableWithNoListenersEventData eventData = (ServiceBusDeadletterMessagesAvailableWithNoListenersEventData) toSystemEventData(events[0]);
        assertEquals("testns1", eventData.getNamespaceName());
    }

    // Storage events
    @Test
    public void consumeStorageBlobCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(StorageBlobCreatedEventData.class, toSystemEventData(events[0]));
        StorageBlobCreatedEventData eventData = (StorageBlobCreatedEventData) toSystemEventData(events[0]);
        assertEquals("https://myaccount.blob.core.windows.net/testcontainer/file1.txt", eventData.getUrl());
    }

    @Test
    public void consumeStorageBlobDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(StorageBlobDeletedEventData.class, toSystemEventData(events[0]));
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) toSystemEventData(events[0]);
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeCloudEventStorageBlobRenamedEvent() {
        String jsonData = "[ {  \"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Storage/storageAccounts/myaccount\",  \"subject\": \"/blobServices/default/containers/testcontainer/blobs/testfile.txt\",  \"type\": \"Microsoft.Storage.BlobRenamed\",  \"time\": \"2017-08-16T01:57:26.005121Z\",  \"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",  \"data\": {    \"api\": \"RenameFile\",    \"clientRequestId\": \"799304a4-bbc5-45b6-9849-ec2c66be800a\",    \"requestId\": \"602a88ef-0001-00e6-1233-164607000000\",    \"eTag\": \"0x8D4E44A24ABE7F1\",    \"destinationUrl\": \"https://myaccount.blob.core.windows.net/testcontainer/testfile.txt\",    \"sequencer\": \"00000000000000EB000000000000C65A\"  },  \"specversion\": \"1.0\"}]";

        CloudEvent[] events = CloudEvent.fromString(jsonData).toArray(new CloudEvent[0]);

        assertNotNull(events);
        assertInstanceOf(StorageBlobRenamedEventData.class, toSystemEventData(events[0]));
        StorageBlobRenamedEventData eventData = (StorageBlobRenamedEventData) toSystemEventData(events[0]);
        assertEquals("https://myaccount.blob.core.windows.net/testcontainer/testfile.txt", eventData.getDestinationUrl());
    }

    @Test
    public void consumeStorageDirectoryCreatedEvent() {
        String requestContent = "[ {  \"topic\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Storage/storageAccounts/myaccount\",  \"subject\": \"/blobServices/default/containers/testcontainer/blobs/testDir\",  \"eventType\": \"Microsoft.Storage.DirectoryCreated\",  \"eventTime\": \"2017-08-16T01:57:26.005121Z\",  \"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",  \"data\": {    \"api\": \"CreateDirectory\",    \"clientRequestId\": \"799304a4-bbc5-45b6-9849-ec2c66be800a\",    \"requestId\": \"602a88ef-0001-00e6-1233-164607000000\",    \"eTag\": \"0x8D4E44A24ABE7F1\",    \"url\": \"https://myaccount.blob.core.windows.net/testcontainer/testDir\",    \"sequencer\": \"00000000000000EB000000000000C65A\"  },  \"dataVersion\": \"2\",  \"metadataVersion\": \"1\"}]";

        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);

        assertNotNull(events);
        StorageDirectoryCreatedEventData eventData = (StorageDirectoryCreatedEventData) toSystemEventData(events.get(0));
        assertEquals("https://myaccount.blob.core.windows.net/testcontainer/testDir", eventData.getUrl());
    }

    @Test
    public void consumeStorageDirectoryDeletedEvent() {
        String requestContent = "[{   \"topic\": \"/subscriptions/id/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/xstoretestaccount\",  \"subject\": \"/blobServices/default/containers/testcontainer/blobs/testDir\",  \"eventType\": \"Microsoft.Storage.DirectoryDeleted\",  \"eventTime\": \"2017-11-07T20:09:22.5674003Z\",  \"id\": \"4c2359fe-001e-00ba-0e04-58586806d298\",  \"data\": {    \"api\": \"DeleteDirectory\",    \"requestId\": \"4c2359fe-001e-00ba-0e04-585868000000\",    \"url\": \"https://example.blob.core.windows.net/testcontainer/testDir\",    \"sequencer\": \"0000000000000281000000000002F5CA\",    \"storageDiagnostics\": {      \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\"    }  },  \"dataVersion\": \"1\",  \"metadataVersion\": \"1\"}]";

        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        StorageDirectoryDeletedEventData eventData = (StorageDirectoryDeletedEventData) toSystemEventData(events.get(0));
        assertEquals("https://example.blob.core.windows.net/testcontainer/testDir", eventData.getUrl());
    }

    @Test
    public void consumeStorageDirectoryRenamedEvent() {
        String requestContent = "[{   \"topic\": \"/subscriptions/id/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/xstoretestaccount\",  \"subject\": \"/blobServices/default/containers/testcontainer/blobs/testDir\",  \"eventType\": \"Microsoft.Storage.DirectoryRenamed\",  \"eventTime\": \"2017-11-07T20:09:22.5674003Z\",  \"id\": \"4c2359fe-001e-00ba-0e04-58586806d298\",  \"data\": {    \"api\": \"RenameDirectory\",    \"requestId\": \"4c2359fe-001e-00ba-0e04-585868000000\",    \"destinationUrl\": \"https://example.blob.core.windows.net/testcontainer/testDir\",    \"sequencer\": \"0000000000000281000000000002F5CA\",    \"storageDiagnostics\": {      \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\"    }  },  \"dataVersion\": \"1\",  \"metadataVersion\": \"1\"}]";

        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        StorageDirectoryRenamedEventData eventData = (StorageDirectoryRenamedEventData) toSystemEventData(events.get(0));
        assertEquals("https://example.blob.core.windows.net/testcontainer/testDir", eventData.getDestinationUrl());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertInstanceOf(ResourceWriteSuccessEventData.class, toSystemEventData(events[0]));
        ResourceWriteSuccessEventData eventData = (ResourceWriteSuccessEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    // Machine Learning Tests
    @Test
    public void consumeMachineLearningServicesModelRegisteredEvent() {
        String requestContent = "[{\"topic\":\"/subscriptions/a5fe3bc5-98f0-4c84-affc-a589f54d9b23/resourceGroups/jenns/providers/Microsoft.MachineLearningServices/workspaces/jenns-canary\",\"eventType\":\"Microsoft.MachineLearningServices.ModelRegistered\",\"subject\":\"models/sklearn_regression_model:3\",\"eventTime\":\"2019-10-17T22:23:57.5350054+00:00\",\"id\":\"3b73ee51-bbf4-480d-9112-cfc23b41bfdb\",\"data\":{\"modelName\":\"sklearn_regression_model\",\"modelVersion\":\"3\",\"modelTags\":{\"area\":\"diabetes\",\"type\":\"regression\"},\"modelProperties\":{\"area\":\"test\"}},\"dataVersion\":\"2\",\"metadataVersion\":\"1\"}]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);

        assertNotNull(events);
        MachineLearningServicesModelRegisteredEventData eventData = (MachineLearningServicesModelRegisteredEventData)
            toSystemEventData(events.get(0));
        assertEquals("sklearn_regression_model", eventData.getModelName());
        assertEquals("3", eventData.getModelVersion());

        assertInstanceOf(Map.class, eventData.getModelTags());
        assertEquals("regression", ((Map<?, ?>) eventData.getModelTags()).get("type"));

        assertInstanceOf(Map.class, eventData.getModelProperties());
        assertEquals("test", ((Map<?, ?>) eventData.getModelProperties()).get("area"));
    }

    @Test
    public void consumeMachineLearningServicesModelDeployedEvent() {
        String requestContent = "[{\"topic\":\"/subscriptions/a5fe3bc5-98f0-4c84-affc-a589f54d9b23/resourceGroups/jenns/providers/Microsoft.MachineLearningServices/workspaces/jenns-canary\",\"eventType\":\"Microsoft.MachineLearningServices.ModelDeployed\",\"subject\":\"endpoints/aciservice1\",\"eventTime\":\"2019-10-23T18:20:08.8824474+00:00\",\"id\":\"40d0b167-be44-477b-9d23-a2befba7cde0\",\"data\":{\"serviceName\":\"aciservice1\",\"serviceComputeType\":\"ACI\",\"serviceTags\":{\"mytag\":\"test tag\"},\"serviceProperties\":{\"myprop\":\"test property\"},\"modelIds\":\"my_first_model:1,my_second_model:1\"},\"dataVersion\":\"2\",\"metadataVersion\":\"1\"}]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        MachineLearningServicesModelDeployedEventData eventData = (MachineLearningServicesModelDeployedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals("aciservice1", eventData.getServiceName());
        assertEquals(2, eventData.getModelIds().split(",").length);
    }

    @Test
    public void consumeMachineLearningServicesRunCompletedEvent() {
        String requestContent = "[{\"topic\":\"/subscriptions/a5fe3bc5-98f0-4c84-affc-a589f54d9b23/resourceGroups/jenns/providers/Microsoft.MachineLearningServices/workspaces/jenns-canary\",\"eventType\":\"Microsoft.MachineLearningServices.RunCompleted\",\"subject\":\"experiments/0fa9dfaa-cba3-4fa7-b590-23e48548f5c1/runs/AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc\",\"eventTime\":\"2019-10-18T19:29:55.8856038+00:00\",\"id\":\"044ac44d-462c-4043-99eb-d9e01dc760ab\",\"data\":{\"experimentId\":\"0fa9dfaa-cba3-4fa7-b590-23e48548f5c1\",\"experimentName\":\"automl-local-regression\",\"runId\":\"AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc\",\"runType\":\"automl\",\"RunTags\":{\"experiment_status\":\"ModelSelection\",\"experiment_status_descr\":\"Beginning model selection.\"},\"runProperties\":{\"num_iterations\":\"10\",\"target\":\"local\"}},\"dataVersion\":\"2\",\"metadataVersion\":\"1\"}]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        MachineLearningServicesRunCompletedEventData eventData = (MachineLearningServicesRunCompletedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals("AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc", eventData.getRunId());
        assertEquals("automl-local-regression", eventData.getExperimentName());
    }

    @Test
    public void consumeMachineLearningServicesRunStatusChangedEvent() {
        String requestContent = "[{\"topic\":\"/subscriptions/a5fe3bc5-98f0-4c84-affc-a589f54d9b23/resourceGroups/jenns/providers/Microsoft.MachineLearningServices/workspaces/jenns-canary\",\"eventType\":\"Microsoft.MachineLearningServices.RunStatusChanged\",\"subject\":\"experiments/0fa9dfaa-cba3-4fa7-b590-23e48548f5c1/runs/AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc\",\"eventTime\":\"2020-03-09T23:53:04.4579724Z\",\"id\":\"aa8cd7df-fe28-5d5d-9b40-3342dbc2a887\",\"data\":{\"runStatus\": \"Running\",\"experimentId\":\"0fa9dfaa-cba3-4fa7-b590-23e48548f5c1\",\"experimentName\":\"automl-local-regression\",\"runId\":\"AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc\",\"runType\":\"automl\",\"runTags\":{\"experiment_status\":\"ModelSelection\",\"experiment_status_descr\":\"Beginning model selection.\"},\"runProperties\":{\"num_iterations\":\"10\",\"target\":\"local\"}},\"dataVersion\":\"2\",\"metadataVersion\":\"1\"}]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        MachineLearningServicesRunStatusChangedEventData eventData = (MachineLearningServicesRunStatusChangedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals("AutoML_ad912b2d-6467-4f32-a616-dbe4af6dd8fc", eventData.getRunId());
        assertEquals("automl-local-regression", eventData.getExperimentName());
        assertEquals("Running", eventData.getRunStatus());
        assertEquals("automl", eventData.getRunType());
    }

    @Test
    public void consumeMachineLearningServicesDatasetDriftDetectedEvent() {
        String requestContent = "[{\"topic\":\"/subscriptions/60582a10-b9fd-49f1-a546-c4194134bba8/resourceGroups/copetersRG/providers/Microsoft.MachineLearningServices/workspaces/driftDemoWS\",\"eventType\":\"Microsoft.MachineLearningServices.DatasetDriftDetected\",\"subject\":\"datadrift/01d29aa4-e6a4-470a-9ef3-66660d21f8ef/run/01d29aa4-e6a4-470a-9ef3-66660d21f8ef_1571590300380\",\"eventTime\":\"2019-10-20T17:08:08.467191+00:00\",\"id\":\"2684de79-b145-4dcf-ad2e-6a1db798585f\",\"data\":{\"dataDriftId\":\"01d29aa4-e6a4-470a-9ef3-66660d21f8ef\",\"dataDriftName\":\"copetersDriftMonitor3\",\"runId\":\"01d29aa4-e6a4-470a-9ef3-66660d21f8ef_1571590300380\",\"baseDatasetId\":\"3c56d136-0f64-4657-a0e8-5162089a88a3\",\"tarAsSystemEventDatasetId\":\"d7e74d2e-c972-4266-b5fb-6c9c182d2a74\",\"driftCoefficient\":0.8350349068479208,\"startTime\":\"2019-07-04T00:00:00+00:00\",\"endTime\":\"2019-07-05T00:00:00+00:00\"},\"dataVersion\":\"2\",\"metadataVersion\":\"1\"}]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        MachineLearningServicesDatasetDriftDetectedEventData eventData = (MachineLearningServicesDatasetDriftDetectedEventData) toSystemEventData(events.get(0));
        assertEquals("copetersDriftMonitor3", eventData.getDataDriftName());
    }
    // End of machine learning tests

    // web
    @Test
    public void consumeCloudEventWebAppUpdatedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.AppUpdated\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebAppUpdatedEventData eventData = (WebAppUpdatedEventData) toSystemEventData(events.get(0));

        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebBackupOperationStartedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.BackupOperationStarted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebBackupOperationStartedEventData eventData = (WebBackupOperationStartedEventData) toSystemEventData(events.get(0));

        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebBackupOperationCompletedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.BackupOperationCompleted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebBackupOperationCompletedEventData eventData = (WebBackupOperationCompletedEventData) toSystemEventData(events.get(0));

        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebBackupOperationFailedEvent() {

        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.BackupOperationFailed\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebBackupOperationFailedEventData eventData = (WebBackupOperationFailedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebRestoreOperationStartedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.RestoreOperationStarted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebRestoreOperationStartedEventData eventData = (WebRestoreOperationStartedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebRestoreOperationCompletedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.RestoreOperationCompleted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";
        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebRestoreOperationCompletedEventData eventData = (WebRestoreOperationCompletedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebRestoreOperationFailedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.RestoreOperationFailed\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebRestoreOperationFailedEventData eventData = (WebRestoreOperationFailedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebSlotSwapStartedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.SlotSwapStarted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent, false);
        WebSlotSwapStartedEventData eventData = (WebSlotSwapStartedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebSlotSwapCompletedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"specversion\": \"1.0\", \"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.SlotSwapCompleted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"}}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebSlotSwapCompletedEventData eventData = (WebSlotSwapCompletedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebSlotSwapFailedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.SlotSwapFailed\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},   \"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebSlotSwapFailedEventData eventData = (WebSlotSwapFailedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebSlotSwapWithPreviewStartedEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.SlotSwapWithPreviewStarted\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},  \"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebSlotSwapWithPreviewStartedEventData eventData = (WebSlotSwapWithPreviewStartedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebSlotSwapWithPreviewCancelledEvent() {
        String siteName = "testSite01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/sites/testSite01\", \"subject\": \"/Microsoft.Web/sites/testSite01\",\"type\": \"Microsoft.Web.SlotSwapWithPreviewCancelled\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appEventTypeDetail\": { \"action\": \"Restarted\"},\"name\": \"testSite01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},   \"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebSlotSwapWithPreviewCancelledEventData eventData = (WebSlotSwapWithPreviewCancelledEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(siteName, eventData.getName());
    }

    @Test
    public void consumeCloudEventWebAppServicePlanUpdatedEvent() {
        String planName = "testPlan01";
        String requestContent = "[{\"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Web/serverfarms/testPlan01\", \"subject\": \"/Microsoft.Web/serverfarms/testPlan01\",\"type\": \"Microsoft.Web.AppServicePlanUpdated\", \"time\": \"2017-08-16T01:57:26.005121Z\",\"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",\"data\": { \"appServicePlanEventTypeDetail\": { \"stampKind\": \"Public\",\"action\": \"Updated\",\"status\": \"Started\" },\"name\": \"testPlan01\",\"clientRequestId\": \"ce636635-2b81-4981-a9d4-cec28fb5b014\",\"correlationRequestId\": \"61baa426-c91f-4e58-b9c6-d3852c4d88d\",\"requestId\": \"0a4d5b5e-7147-482f-8e21-4219aaacf62a\",\"address\": \"/subscriptions/ef90e930-9d7f-4a60-8a99-748e0eea69de/resourcegroups/egcanarytest/providers/Microsoft.Web/sites/egtestapp/restart?api-version=2016-03-01\",\"verb\": \"POST\"},\"specversion\": \"1.0\",\"specversion\": \"1.0\"}]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        WebAppServicePlanUpdatedEventData eventData = (WebAppServicePlanUpdatedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(planName, eventData.getName());
    }
    //end of web

    // Healthcare FHIR
    @Test
    public void consumeFhirResourceCreatedEvent() {
        String requestContent = "[ { \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"eventType\":\"Microsoft.HealthcareApis.FhirResourceCreated\", \"eventTime\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"dataVersion\": \"1.0\" }]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        HealthcareFhirResourceCreatedEventData eventData = (HealthcareFhirResourceCreatedEventData) toSystemEventData(events.get(0));
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    @Test
    public void consumeFhirResourceUpdatedEvent() {
        String requestContent = "[ { \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"eventType\":\"Microsoft.HealthcareApis.FhirResourceUpdated\", \"eventTime\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"dataVersion\": \"1.0\" }]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        HealthcareFhirResourceUpdatedEventData eventData = (HealthcareFhirResourceUpdatedEventData) toSystemEventData(events.get(0));
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    @Test
    public void consumeFhirResourceDeletedEvent() {
        String requestContent = "[ { \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"eventType\":\"Microsoft.HealthcareApis.FhirResourceDeleted\", \"eventTime\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"dataVersion\": \"1.0\" }]";
        List<EventGridEvent> events = EventGridEvent.fromString(requestContent);
        assertNotNull(events);
        HealthcareFhirResourceDeletedEventData eventData = (HealthcareFhirResourceDeletedEventData) toSystemEventData(events.get(0));
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    @Test
    public void consumeCloudEventFhirResourceCreatedEvent() {
        String requestContent = "[ { \"source\": \"/subscriptions/{subscription-id}/resourceGroups/{resource-group-name}/providers/Microsoft.HealthcareApis/workspaces/{workspace-name}\", \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"type\":\"Microsoft.HealthcareApis.FhirResourceCreated\", \"time\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"specversion\": \"1.0\" }]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        HealthcareFhirResourceCreatedEventData eventData = (HealthcareFhirResourceCreatedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    @Test
    public void consumeCloudEventFhirResourceUpdatedEvent() {
        String requestContent = "[ { \"source\": \"/subscriptions/{subscription-id}/resourceGroups/{resource-group-name}/providers/Microsoft.HealthcareApis/workspaces/{workspace-name}\", \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"type\":\"Microsoft.HealthcareApis.FhirResourceUpdated\", \"time\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"specversion\": \"1.0\" }]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        HealthcareFhirResourceUpdatedEventData eventData = (HealthcareFhirResourceUpdatedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    @Test
    public void consumeCloudEventFhirResourceDeletedEvent() {
        String requestContent = "[ { \"source\": \"/subscriptions/{subscription-id}/resourceGroups/{resource-group-name}/providers/Microsoft.HealthcareApis/workspaces/{workspace-name}\", \"subject\":\"{fhir-account}.fhir.azurehealthcareapis.com/Patient/e0a1f743-1a70-451f-830e-e96477163902\", \"type\":\"Microsoft.HealthcareApis.FhirResourceDeleted\", \"time\":\"2017-08-16T03:54:38.2696833Z\", \"id\":\"25b3b0d0-d79b-44d5-9963-440d4e6a9bba\", \"data\": { \"resourceType\": \"Patient\", \"resourceFhirAccount\": \"{fhir-account}.fhir.azurehealthcareapis.com\", \"resourceFhirId\": \"e0a1f743-1a70-451f-830e-e96477163902\", \"resourceVersionId\": 1 }, \"specversion\": \"1.0\" }]";

        List<CloudEvent> events = CloudEvent.fromString(requestContent);
        HealthcareFhirResourceDeletedEventData eventData = (HealthcareFhirResourceDeletedEventData) toSystemEventData(events.get(0));
        assertNotNull(events);
        assertEquals(HealthcareFhirResourceType.PATIENT, eventData.getFhirResourceType());
        assertEquals("{fhir-account}.fhir.azurehealthcareapis.com", eventData.getFhirServiceHostName());
        assertEquals("e0a1f743-1a70-451f-830e-e96477163902", eventData.getFhirResourceId());
        assertEquals(1, eventData.getFhirResourceVersionId());
    }

    // End of healthcare FHIR

    @Test
    public void verifyAcsRouterJobClassificationFailedEventDataErrors() {
        ResponseError error = new ResponseError("InvalidRequest", "The request is invalid");
        AcsRouterJobClassificationFailedEventData eventData = new AcsRouterJobClassificationFailedEventData();
        eventData.setErrors(Collections.singletonList(error));
        List<ResponseError> errors = eventData.getErrors();
        assertEquals(1, errors.size());
        assertEquals("InvalidRequest", errors.get(0).getCode());
        assertEquals("The request is invalid", errors.get(0).getMessage());
    }


    // TODO: When new event types are introduced, add one test here for each event type
    private String getTestPayloadFromFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("customization/" + fileName)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes);
        }
    }
}
