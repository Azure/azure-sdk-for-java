// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.util.BinaryData;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.implementation.models.ContosoItemReceivedEventData;
import com.azure.messaging.eventgrid.implementation.models.ContosoItemSentEventData;
import com.azure.messaging.eventgrid.implementation.models.DroneShippingInfo;
import com.azure.messaging.eventgrid.implementation.models.RocketShippingInfo;
import com.azure.messaging.eventgrid.systemevents.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

    // just test to see if these events can be deserialized
    @Test
    public void testDeserializeEventGridEvents() throws JsonProcessingException {
        String storageEventJson = "{\"topic\": \"/subscriptions/subscriptionID/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/xstoretestaccount\",\"subject\": \"/blobServices/default/containers/testcontainer/blobs/testfile.txt\",   \"eventType\": \"Microsoft.Storage.BlobCreated\",  \"eventTime\": \"2017-06-26T18:41:00.9584103Z\",  \"id\": \"831e1650-001e-001b-66ab-eeb76e069631\",  \"data\": {    \"api\": \"PutBlockList\",    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",    \"eTag\": \"0x8D4BCC2E4835CD0\",    \"contentType\": \"text/plain\",    \"contentLength\": 524288,    \"blobType\": \"BlockBlob\",    \"url\": \"https://example.blob.core.windows.net/testcontainer/testfile.txt\",    \"sequencer\": \"00000000000004420000000000028963\",    \"storageDiagnostics\": {      \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\" }},  \"dataVersion\": \"\",  \"metadataVersion\": \"1\"}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
                @Override
                public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return OffsetDateTime.parse(jsonParser.getValueAsString());
                }
            }));

        com.azure.messaging.eventgrid.implementation.models.EventGridEvent eventGridEvent =
            mapper.readValue(storageEventJson, com.azure.messaging.eventgrid.implementation.models.EventGridEvent.class);

        assertNotNull(eventGridEvent);
        assertEquals("Microsoft.Storage.BlobCreated", eventGridEvent.getEventType(), "Event types do not match");
    }

    @Test
    public void consumeStorageBlobDeletedEventWithExtraProperty() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEventWithExtraProperty.json");
        //


        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobDeletedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobDeletedEventData);
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
        ContosoItemReceivedEventData[] eventData = events[0].getData().toObject(TypeReference.createInstance(ContosoItemReceivedEventData[].class));
        assertNotNull(eventData);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", (eventData[0]).getItemSku());
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

        ContosoItemSentEventData eventData0 = events[0].getData().toObject(TypeReference.createInstance(ContosoItemSentEventData.class));
        ContosoItemSentEventData eventData1 = events[1].getData().toObject(TypeReference.createInstance(ContosoItemSentEventData.class));

        assertNotNull(eventData0);
        assertNotNull(eventData1);

        assertTrue(eventData0.getShippingInfo() instanceof DroneShippingInfo);

        assertTrue(eventData1.getShippingInfo() instanceof RocketShippingInfo);
    }


    @Test
    public void consumeMultipleEventsInSameBatch() throws IOException {
        String jsonData = getTestPayloadFromFile("MultipleEventsInSameBatch.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(4, events.length);
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobCreatedEventData);
        assertTrue(toSystemEventData(events[1]) instanceof StorageBlobDeletedEventData);
        assertTrue(toSystemEventData(events[2]) instanceof StorageBlobDeletedEventData);
        assertTrue(toSystemEventData(events[3]) instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof AppConfigurationKeyValueDeletedEventData);
        AppConfigurationKeyValueDeletedEventData eventData = (AppConfigurationKeyValueDeletedEventData) toSystemEventData(events[0]);
        assertEquals("key1", eventData.getKey());
    }

    @Test
    public void consumeAppConfigurationKeyValueModifiedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueModified.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof AppConfigurationKeyValueModifiedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof ContainerRegistryImagePushedEventData);
        ContainerRegistryImagePushedEventData eventData = (ContainerRegistryImagePushedEventData) toSystemEventData(events[0]);
        assertEquals("127.0.0.1", eventData.getRequest().getAddr());
    }

    @Test
    public void consumeContainerRegistryImageDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImageDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ContainerRegistryImageDeletedEventData);
        ContainerRegistryImageDeletedEventData eventData = (ContainerRegistryImageDeletedEventData) toSystemEventData(events[0]);
        assertEquals("testactor", eventData.getActor().getName());
    }

    @Test
    public void consumeContainerRegistryChartDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ContainerRegistryChartDeletedEventData);
        ContainerRegistryChartDeletedEventData eventData = (ContainerRegistryChartDeletedEventData) toSystemEventData(events[0]);
        assertEquals("mediatype1", eventData.getTarget().getMediaType());
    }

    @Test
    public void consumeContainerRegistryChartPushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartPushedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ContainerRegistryChartPushedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof IotHubDeviceCreatedEventData);
        IotHubDeviceCreatedEventData eventData = (IotHubDeviceCreatedEventData) toSystemEventData(events[0]);
        assertEquals("enabled", eventData.getTwin().getStatus());
    }

    @Test
    public void consumeIoTHubDeviceDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof IotHubDeviceDeletedEventData);
        IotHubDeviceDeletedEventData eventData = (IotHubDeviceDeletedEventData) toSystemEventData(events[0]);
        assertEquals("AAAAAAAAAAE=", eventData.getTwin().getEtag());
    }

    @Test
    public void consumeIoTHubDeviceConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceConnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof IotHubDeviceConnectedEventData);
        IotHubDeviceConnectedEventData eventData = (IotHubDeviceConnectedEventData) toSystemEventData(events[0]);
        assertEquals("EGTESTHUB1", eventData.getHubName());
    }

    @Test
    public void consumeIoTHubDeviceDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof IotHubDeviceDisconnectedEventData);
        IotHubDeviceDisconnectedEventData eventData = (IotHubDeviceDisconnectedEventData) toSystemEventData(events[0]);
        assertEquals("000000000000000001D4132452F67CE200000002000000000000000000000002", eventData.getDeviceConnectionStateEventInfo().getSequenceNumber());
    }

    @Test
    public void consumeIoTHubDeviceTelemetryEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceTelemetryEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof IotHubDeviceTelemetryEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof SubscriptionValidationEventData);
        SubscriptionValidationEventData eventData = (SubscriptionValidationEventData) toSystemEventData(events[0]);
        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.getValidationCode());
    }

    @Test
    public void consumeEventGridSubscriptionDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof SubscriptionDeletedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof EventHubCaptureFileCreatedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MapsGeofenceEnteredEventData);
        MapsGeofenceEnteredEventData eventData = (MapsGeofenceEnteredEventData) toSystemEventData(events[0]);
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceExitedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceExitedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MapsGeofenceExitedEventData);
        MapsGeofenceExitedEventData eventData = (MapsGeofenceExitedEventData) toSystemEventData(events[0]);
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceResultEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceResultEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MapsGeofenceResultEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobCanceledEventData);
        MediaJobCanceledEventData eventData = (MediaJobCanceledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.CANCELING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELED, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertTrue(eventData.getOutputs().get(0) instanceof MediaJobOutputAsset);

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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobCancelingEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobProcessingEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobFinishedEventData);
        MediaJobFinishedEventData eventData = (MediaJobFinishedEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.FINISHED, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertTrue(eventData.getOutputs().get(0) instanceof MediaJobOutputAsset);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobErroredEventData);
        MediaJobErroredEventData eventData = (MediaJobErroredEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.ERROR, eventData.getState());
        assertEquals(1, eventData.getOutputs().size());
        assertTrue(eventData.getOutputs().get(0) instanceof MediaJobOutputAsset);

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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputStateChangeEventData);
        MediaJobOutputStateChangeEventData eventData = (MediaJobOutputStateChangeEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
        MediaJobOutputAsset outputAsset = (MediaJobOutputAsset) eventData.getOutput();
        assertEquals("output-2ac2fe75-6557-4de5-ab25-5713b74a6901", outputAsset.getAssetName());
    }

    @Test
    public void consumeMediaJobScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobScheduledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobScheduledEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputCanceledEventData);
        MediaJobOutputCanceledEventData eventData = (MediaJobOutputCanceledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.CANCELING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELED, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCancelingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputCancelingEventData);
        MediaJobOutputCancelingEventData eventData = (MediaJobOutputCancelingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELING, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputErroredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputErroredEventData);
        MediaJobOutputErroredEventData eventData = (MediaJobOutputErroredEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.ERROR, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputFinishedEventData);
        MediaJobOutputFinishedEventData eventData = (MediaJobOutputFinishedEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.FINISHED, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputProcessingEventData);
        MediaJobOutputProcessingEventData eventData = (MediaJobOutputProcessingEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputScheduledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputScheduledEventData);
        MediaJobOutputScheduledEventData eventData = (MediaJobOutputScheduledEventData) toSystemEventData(events[0]);
        assertEquals(MediaJobState.QUEUED, eventData.getPreviousState());
        assertEquals(MediaJobState.SCHEDULED, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputProgressEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProgressEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobOutputProgressEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaJobStateChangeEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventEncoderConnectedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventConnectionRejectedEventData);
        MediaLiveEventConnectionRejectedEventData eventData = (MediaLiveEventConnectionRejectedEventData) toSystemEventData(events[0]);
        assertEquals("Mystream1", eventData.getStreamId());
    }

    @Test
    public void consumeMediaLiveEventEncoderDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventEncoderDisconnectedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventIncomingStreamReceivedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventIncomingStreamsOutOfSyncEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventIncomingVideoStreamsOutOfSyncEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventIncomingDataChunkDroppedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventIngestHeartbeatEventData);
        MediaLiveEventIngestHeartbeatEventData eventData = (MediaLiveEventIngestHeartbeatEventData) toSystemEventData(events[0]);
        assertEquals("video", eventData.getTrackType());
        assertEquals("video", eventData.getTrackName());
        assertEquals("11999", eventData.getLastTimestamp());
        assertEquals("1000", eventData.getTimescale());
        assertTrue(eventData.isUnexpectedBitrate());
        assertEquals("Running", eventData.getState());
        assertFalse(eventData.isHealthy());
    }

    @Test
    public void consumeMediaLiveEventTrackDiscontinuityDetectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventTrackDiscontinuityDetectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof MediaLiveEventTrackDiscontinuityDetectedEventData);
        MediaLiveEventTrackDiscontinuityDetectedEventData eventData = (MediaLiveEventTrackDiscontinuityDetectedEventData) toSystemEventData(events[0]);
        assertEquals("video", eventData.getTrackType());
        assertEquals("video", eventData.getTrackName());
        assertEquals("10999", eventData.getPreviousTimestamp());
        assertEquals("14999", eventData.getNewTimestamp());
        assertEquals("1000", eventData.getTimescale());
        assertEquals("4000", eventData.getDiscontinuityGap());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceWriteFailureEventData);
        ResourceWriteFailureEventData eventData = (ResourceWriteFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceWriteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceWriteCancelEventData);
        ResourceWriteCancelEventData eventData = (ResourceWriteCancelEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceDeleteSuccessEventData);
        ResourceDeleteSuccessEventData eventData = (ResourceDeleteSuccessEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceDeleteFailureEventData);
        ResourceDeleteFailureEventData eventData = (ResourceDeleteFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceDeleteCancelEventData);
        ResourceDeleteCancelEventData eventData = (ResourceDeleteCancelEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceActionSuccessEventData);
        ResourceActionSuccessEventData eventData = (ResourceActionSuccessEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceActionFailureEventData);
        ResourceActionFailureEventData eventData = (ResourceActionFailureEventData) toSystemEventData(events[0]);
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ResourceActionCancelEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof ServiceBusActiveMessagesAvailableWithNoListenersEventData);
        ServiceBusActiveMessagesAvailableWithNoListenersEventData eventData = (ServiceBusActiveMessagesAvailableWithNoListenersEventData) toSystemEventData(events[0]);
        assertEquals("testns1", eventData.getNamespaceName());
    }

    @Test
    public void consumeServiceBusDeadletterMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusDeadletterMessagesAvailableWithNoListenersEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobCreatedEventData);
        StorageBlobCreatedEventData eventData = (StorageBlobCreatedEventData) toSystemEventData(events[0]);
        assertEquals("https://myaccount.blob.core.windows.net/testcontainer/file1.txt", eventData.getUrl());
    }

    @Test
    public void consumeStorageBlobDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.fromString(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) toSystemEventData(events[0]);
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeCloudEventStorageBlobRenamedEvent() {
        String jsonData = "[ {  \"source\": \"/subscriptions/319a9601-1ec0-0000-aebc-8fe82724c81e/resourceGroups/testrg/providers/Microsoft.Storage/storageAccounts/myaccount\",  \"subject\": \"/blobServices/default/containers/testcontainer/blobs/testfile.txt\",  \"type\": \"Microsoft.Storage.BlobRenamed\",  \"time\": \"2017-08-16T01:57:26.005121Z\",  \"id\": \"602a88ef-0001-00e6-1233-1646070610ea\",  \"data\": {    \"api\": \"RenameFile\",    \"clientRequestId\": \"799304a4-bbc5-45b6-9849-ec2c66be800a\",    \"requestId\": \"602a88ef-0001-00e6-1233-164607000000\",    \"eTag\": \"0x8D4E44A24ABE7F1\",    \"destinationUrl\": \"https://myaccount.blob.core.windows.net/testcontainer/testfile.txt\",    \"sequencer\": \"00000000000000EB000000000000C65A\"  },  \"specversion\": \"1.0\"}]";

        CloudEvent[] events = CloudEvent.fromString(jsonData).toArray(new CloudEvent[0]);

        assertNotNull(events);
        assertTrue(toSystemEventData(events[0]) instanceof StorageBlobRenamedEventData);
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
        assertTrue(toSystemEventData(events[0]) instanceof ResourceWriteSuccessEventData);
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

        assertTrue(eventData.getModelTags() instanceof Map);
        assertEquals("regression", ((Map<?, ?>) eventData.getModelTags()).get("type"));

        assertTrue(eventData.getModelProperties() instanceof Map);
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
