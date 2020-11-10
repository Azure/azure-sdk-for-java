// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DeserializationTests {

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

    // just test to see if these events can be deserialized
    @Test
    public void testDeserializeCloudEvents() throws JsonProcessingException {
        String cloudEventJson = "{\n" +
            "  \"id\": \"9ddf9b10-fe3d-4a16-94bc-c0298924ded1\",\n" +
            "  \"data\": {\n" +
            "    \"Field2\": \"Value2\",\n" +
            "    \"Field3\": \"Value3\",\n" +
            "    \"Field1\": \"Value1\"\n" +
            "  },\n" +
            "  \"type\": \"Microsoft.MockPublisher.TestEvent\",\n" +
            "  \"time\": \"2020-07-21T18:41:31.166Z\",\n" +
            "  \"specversion\": \"1.0\"\n" +
            "}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
                @Override
                public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return OffsetDateTime.parse(jsonParser.getValueAsString());
                }
            }));

        com.azure.messaging.eventgrid.implementation.models.CloudEvent cloudEvent = mapper.readValue(cloudEventJson, com.azure.messaging.eventgrid.implementation.models.CloudEvent.class);

        assertNotNull(cloudEvent);
        assertEquals("Microsoft.MockPublisher.TestEvent", cloudEvent.getType(), "Event types do not match");

        // actually deserialized as a LinkedHashMap instead of generic object.
        Object data = cloudEvent.getData();

        assertNotNull(data);
    }

    @Test
    public void consumeStorageBlobDeletedEventWithExtraProperty() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEventWithExtraProperty.json");
        //


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[0].getData();
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeEventGridEventWithoutArrayBrackets() throws IOException {
        // using a storageBlobDeletedEvent
        String jsonData = getTestPayloadFromFile("EventGridEventNoArray.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        assertTrue(events[0].getData() instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[0].getData();
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    @Test
    public void consumeCloudEventWithoutArrayBrackets() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventNoArray.json");

        List<CloudEvent> events = CloudEvent.parse(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getSpecVersion(), "1.0");

        ContosoItemReceivedEventData data = events.get(0).getData(ContosoItemReceivedEventData.class);
        assertNotNull(data);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", data.getItemSku());

        Map<String, Object> additionalProperties = events.get(0).getExtensionAttributes();

        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.containsKey("foo"));
        assertEquals("bar", additionalProperties.get("foo"));
    }

    @Test
    public void consumeEventGridEventWithNullData() throws IOException {
        // using a storageBlobDeletedEvent
        String jsonData = getTestPayloadFromFile("EventGridNullData.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        assertNull(events[0].getData());
        assertEquals("/blobServices/default/containers/testcontainer/blobs/testfile.txt", events[0].getSubject());
    }

    @Test
    public void consumeCloudEventWithNullData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventNullData.json");

        List<CloudEvent> events = CloudEvent.parse(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals("1.0", events.get(0).getSpecVersion());

        assertNull(events.get(0).getData());
    }

    @Test
    public void consumeCloudEventWithBinaryData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventBinaryData.json");

        byte[] data = Base64.getDecoder().decode("samplebinarydata");

        List<CloudEvent> events = CloudEvent.parse(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getSpecVersion(), "1.0");

        byte[] eventData = (byte[]) events.get(0).getData();

        assertNotNull(eventData);

        assertArrayEquals(data, eventData);
    }

    @Test
    public void consumeCloudEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEvent.json");

        List<CloudEvent> events = CloudEvent.parse(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getSpecVersion(), "1.0");

        ContosoItemReceivedEventData data = events.get(0).getData(ContosoItemReceivedEventData.class);
        assertNotNull(data);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", data.getItemSku());

        Map<String, Object> additionalProperties = events.get(0).getExtensionAttributes();

        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.containsKey("foo"));
        assertEquals("bar", additionalProperties.get("foo"));

    }

    @Test
    public void consumeCloudEventXmlData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventXmlData.json");

        List<CloudEvent> events = CloudEvent.parse(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getSpecVersion(), "1.0");

        assertEquals(events.get(0).getExtensionAttributes().get("comexampleothervalue"), 5);

        String xmlData = events.get(0).getData(String.class);

        assertEquals("<much wow=\"xml\"/>", xmlData);
    }

    @Test
    public void consumeCustomEvents() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEvents.json");


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        assertNotNull(events[0].getData(ContosoItemReceivedEventData.class));
        ContosoItemReceivedEventData eventData = events[0].getData(ContosoItemReceivedEventData.class);
        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.getItemSku());
    }

    @Test
    public void consumeCustomEventWithArrayData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithArrayData.json");
        //


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);
        ContosoItemReceivedEventData[] eventData = events[0].getData(ContosoItemReceivedEventData[].class);
        assertNotNull(eventData);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", (eventData[0]).getItemSku());
    }

    @Test
    public void consumeCustomEventWithBooleanData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithBooleanData.json");


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);

        Boolean eventData = events[0].getData(Boolean.class);
        assertNotNull(eventData);

        assertTrue(eventData);
    }

    @Test
    public void consumeCustomEventWithStringData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithStringData.json");
        //


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(1, events.length);

        String eventData = events[0].getData(String.class);
        assertNotNull(eventData);

        assertEquals("stringdata", eventData);
    }

    @Test
    public void consumeCustomEventWithPolymorphicData() throws IOException {
        String jsonData = getTestPayloadFromFile("CustomEventWithPolymorphicData.json");


        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(2, events.length);

        ContosoItemSentEventData eventData0 = events[0].getData(ContosoItemSentEventData.class);
        ContosoItemSentEventData eventData1 = events[1].getData(ContosoItemSentEventData.class);

        assertNotNull(eventData0);
        assertNotNull(eventData1);

        assertTrue(eventData0.getShippingInfo() instanceof DroneShippingInfo);

        assertTrue(eventData1.getShippingInfo() instanceof RocketShippingInfo);
    }


    @Test
    public void consumeMultipleEventsInSameBatch() throws IOException {
        String jsonData = getTestPayloadFromFile("MultipleEventsInSameBatch.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertEquals(4, events.length);
        assertTrue(events[0].getData() instanceof StorageBlobCreatedEventData);
        assertTrue(events[1].getData() instanceof StorageBlobDeletedEventData);
        assertTrue(events[2].getData() instanceof StorageBlobDeletedEventData);
        assertTrue(events[3].getData() instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[2].getData();
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    // AppConfiguration events
    @Test
    public void consumeAppConfigurationKeyValueDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueDeleted.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof AppConfigurationKeyValueDeletedEventData);
        AppConfigurationKeyValueDeletedEventData eventData = (AppConfigurationKeyValueDeletedEventData) events[0].getData();
        assertEquals("key1", eventData.getKey());
    }

    @Test
    public void consumeAppConfigurationKeyValueModifiedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("AppConfigurationKeyValueModified.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof AppConfigurationKeyValueModifiedEventData);
        AppConfigurationKeyValueModifiedEventData eventData = (AppConfigurationKeyValueModifiedEventData) events[0].getData();
        assertEquals("key1", eventData.getKey());
    }

    // ContainerRegistry events
    @Test
    public void consumeContainerRegistryImagePushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImagePushedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ContainerRegistryImagePushedEventData);
        ContainerRegistryImagePushedEventData eventData = (ContainerRegistryImagePushedEventData) events[0].getData();
        assertEquals("127.0.0.1", eventData.getRequest().getAddr());
    }

    @Test
    public void consumeContainerRegistryImageDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryImageDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ContainerRegistryImageDeletedEventData);
        ContainerRegistryImageDeletedEventData eventData = (ContainerRegistryImageDeletedEventData) events[0].getData();
        assertEquals("testactor", eventData.getActor().getName());
    }

    @Test
    public void consumeContainerRegistryChartDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ContainerRegistryChartDeletedEventData);
        ContainerRegistryChartDeletedEventData eventData = (ContainerRegistryChartDeletedEventData) events[0].getData();
        assertEquals("mediatype1", eventData.getTarget().getMediaType());
    }

    @Test
    public void consumeContainerRegistryChartPushedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ContainerRegistryChartPushedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ContainerRegistryChartPushedEventData);
        ContainerRegistryChartPushedEventData eventData = (ContainerRegistryChartPushedEventData) events[0].getData();
        assertEquals("mediatype1", eventData.getTarget().getMediaType());
    }

    // IoTHub Device events
    @Test
    public void consumeIoTHubDeviceCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof IotHubDeviceCreatedEventData);
        IotHubDeviceCreatedEventData eventData = (IotHubDeviceCreatedEventData) events[0].getData();
        assertEquals("enabled", eventData.getTwin().getStatus());
    }

    @Test
    public void consumeIoTHubDeviceDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof IotHubDeviceDeletedEventData);
        IotHubDeviceDeletedEventData eventData = (IotHubDeviceDeletedEventData) events[0].getData();
        assertEquals("AAAAAAAAAAE=", eventData.getTwin().getEtag());
    }

    @Test
    public void consumeIoTHubDeviceConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceConnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof IotHubDeviceConnectedEventData);
        IotHubDeviceConnectedEventData eventData = (IotHubDeviceConnectedEventData) events[0].getData();
        assertEquals("EGTESTHUB1", eventData.getHubName());
    }

    @Test
    public void consumeIoTHubDeviceDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof IotHubDeviceDisconnectedEventData);
        IotHubDeviceDisconnectedEventData eventData = (IotHubDeviceDisconnectedEventData) events[0].getData();
        assertEquals("000000000000000001D4132452F67CE200000002000000000000000000000002", eventData.getDeviceConnectionStateEventInfo().getSequenceNumber());
    }

    @Test
    public void consumeIoTHubDeviceTelemetryEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("IoTHubDeviceTelemetryEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof IotHubDeviceTelemetryEventData);
        IotHubDeviceTelemetryEventData eventData = (IotHubDeviceTelemetryEventData) events[0].getData();
        assertEquals("Active", eventData.getProperties().get("Status"));
    }

    // EventGrid events
    @Test
    public void consumeEventGridSubscriptionValidationEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionValidationEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof SubscriptionValidationEventData);
        SubscriptionValidationEventData eventData = (SubscriptionValidationEventData) events[0].getData();
        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", eventData.getValidationCode());
    }

    @Test
    public void consumeEventGridSubscriptionDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventGridSubscriptionDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof SubscriptionDeletedEventData);
        SubscriptionDeletedEventData eventData = (SubscriptionDeletedEventData) events[0].getData();
        assertEquals("/subscriptions/id/resourceGroups/rg/providers/Microsoft.EventGrid/topics/topic1/providers/Microsoft.EventGrid/eventSubscriptions/eventsubscription1", eventData.getEventSubscriptionId());
    }

    // Event Hub Events
    @Test
    public void consumeEventHubCaptureFileCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("EventHubCaptureFileCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof EventHubCaptureFileCreatedEventData);
        EventHubCaptureFileCreatedEventData eventData = (EventHubCaptureFileCreatedEventData) events[0].getData();
        assertEquals("AzureBlockBlob", eventData.getFileType());
    }

    // Maps events
    @Test
    public void consumeMapsGeoFenceEnteredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceEnteredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MapsGeofenceEnteredEventData);
        MapsGeofenceEnteredEventData eventData = (MapsGeofenceEnteredEventData) events[0].getData();
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceExitedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceExitedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MapsGeofenceExitedEventData);
        MapsGeofenceExitedEventData eventData = (MapsGeofenceExitedEventData) events[0].getData();
        assertEquals(true, eventData.isEventPublished());
    }

    @Test
    public void consumeMapsGeoFenceResultEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MapsGeofenceResultEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MapsGeofenceResultEventData);
        MapsGeofenceResultEventData eventData = (MapsGeofenceResultEventData) events[0].getData();
        assertEquals(true, eventData.isEventPublished());
    }

    // Media Services events
    @Test
    public void consumeMediaJobCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobCanceledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobCanceledEventData);
        MediaJobCanceledEventData eventData = (MediaJobCanceledEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobCancelingEventData);
        MediaJobCancelingEventData eventData = (MediaJobCancelingEventData) events[0].getData();
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELING, eventData.getState());
    }

    @Test
    public void consumeMediaJobProcessingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobProcessingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobProcessingEventData);
        MediaJobProcessingEventData eventData = (MediaJobProcessingEventData) events[0].getData();
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getState());
    }

    @Test
    public void consumeMediaJobFinishedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobFinishedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobFinishedEventData);
        MediaJobFinishedEventData eventData = (MediaJobFinishedEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobErroredEventData);
        MediaJobErroredEventData eventData = (MediaJobErroredEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputStateChangeEventData);
        MediaJobOutputStateChangeEventData eventData = (MediaJobOutputStateChangeEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobScheduledEventData);
        MediaJobScheduledEventData eventData = (MediaJobScheduledEventData) events[0].getData();
        assertEquals(MediaJobState.QUEUED, eventData.getPreviousState());
        assertEquals(MediaJobState.SCHEDULED, eventData.getState());
    }

    @Test
    public void consumeMediaJobOutputCanceledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCanceledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputCanceledEventData);
        MediaJobOutputCanceledEventData eventData = (MediaJobOutputCanceledEventData) events[0].getData();
        assertEquals(MediaJobState.CANCELING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELED, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputCancelingEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputCancelingEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputCancelingEventData);
        MediaJobOutputCancelingEventData eventData = (MediaJobOutputCancelingEventData) events[0].getData();
        assertEquals(MediaJobState.PROCESSING, eventData.getPreviousState());
        assertEquals(MediaJobState.CANCELING, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputErroredEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputErroredEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputErroredEventData);
        MediaJobOutputErroredEventData eventData = (MediaJobOutputErroredEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputFinishedEventData);
        MediaJobOutputFinishedEventData eventData = (MediaJobOutputFinishedEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputProcessingEventData);
        MediaJobOutputProcessingEventData eventData = (MediaJobOutputProcessingEventData) events[0].getData();
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputScheduledEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputScheduledEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputScheduledEventData);
        MediaJobOutputScheduledEventData eventData = (MediaJobOutputScheduledEventData) events[0].getData();
        assertEquals(MediaJobState.QUEUED, eventData.getPreviousState());
        assertEquals(MediaJobState.SCHEDULED, eventData.getOutput().getState());
        assertTrue(eventData.getOutput() instanceof MediaJobOutputAsset);
    }

    @Test
    public void consumeMediaJobOutputProgressEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaJobOutputProgressEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobOutputProgressEventData);
        MediaJobOutputProgressEventData eventData = (MediaJobOutputProgressEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaJobStateChangeEventData);
        MediaJobStateChangeEventData eventData = (MediaJobStateChangeEventData) events[0].getData();
        assertEquals(MediaJobState.SCHEDULED, eventData.getPreviousState());
        assertEquals(MediaJobState.PROCESSING, eventData.getState());
    }

    @Test
    public void consumeMediaLiveEventEncoderConnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderConnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventEncoderConnectedEventData);
        MediaLiveEventEncoderConnectedEventData eventData = (MediaLiveEventEncoderConnectedEventData) events[0].getData();
        assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.getIngestUrl());
        assertEquals("Mystream1", eventData.getStreamId());
        assertEquals("<ip address>", eventData.getEncoderIp());
        assertEquals("3557", eventData.getEncoderPort());
    }

    @Test
    public void consumeMediaLiveEventConnectionRejectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventConnectionRejectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventConnectionRejectedEventData);
        MediaLiveEventConnectionRejectedEventData eventData = (MediaLiveEventConnectionRejectedEventData) events[0].getData();
        assertEquals("Mystream1", eventData.getStreamId());
    }

    @Test
    public void consumeMediaLiveEventEncoderDisconnectedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventEncoderDisconnectedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventEncoderDisconnectedEventData);
        MediaLiveEventEncoderDisconnectedEventData eventData = (MediaLiveEventEncoderDisconnectedEventData) events[0].getData();
        assertEquals("rtmp://liveevent-ec9d26a8.channel.media.azure.net:1935/live/cb5540b10a5646218c1328be95050c59", eventData.getIngestUrl());
        assertEquals("Mystream1", eventData.getStreamId());
        assertEquals("<ip address>", eventData.getEncoderIp());
        assertEquals("3557", eventData.getEncoderPort());
    }

    @Test
    public void consumeMediaLiveEventIncomingStreamReceivedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("MediaLiveEventIncomingStreamReceivedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventIncomingStreamReceivedEventData);
        MediaLiveEventIncomingStreamReceivedEventData eventData = (MediaLiveEventIncomingStreamReceivedEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventIncomingStreamsOutOfSyncEventData);
        MediaLiveEventIncomingStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingStreamsOutOfSyncEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventIncomingVideoStreamsOutOfSyncEventData);
        MediaLiveEventIncomingVideoStreamsOutOfSyncEventData eventData = (MediaLiveEventIncomingVideoStreamsOutOfSyncEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventIncomingDataChunkDroppedEventData);
        MediaLiveEventIncomingDataChunkDroppedEventData eventData = (MediaLiveEventIncomingDataChunkDroppedEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventIngestHeartbeatEventData);
        MediaLiveEventIngestHeartbeatEventData eventData = (MediaLiveEventIngestHeartbeatEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof MediaLiveEventTrackDiscontinuityDetectedEventData);
        MediaLiveEventTrackDiscontinuityDetectedEventData eventData = (MediaLiveEventTrackDiscontinuityDetectedEventData) events[0].getData();
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

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceWriteFailureData);
        ResourceWriteFailureData eventData = (ResourceWriteFailureData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceWriteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceWriteCancelData);
        ResourceWriteCancelData eventData = (ResourceWriteCancelData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceDeleteSuccessData);
        ResourceDeleteSuccessData eventData = (ResourceDeleteSuccessData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceDeleteFailureData);
        ResourceDeleteFailureData eventData = (ResourceDeleteFailureData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceDeleteCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceDeleteCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceDeleteCancelData);
        ResourceDeleteCancelData eventData = (ResourceDeleteCancelData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceActionSuccessData);
        ResourceActionSuccessData eventData = (ResourceActionSuccessData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionFailureEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionFailureEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceActionFailureData);
        ResourceActionFailureData eventData = (ResourceActionFailureData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    @Test
    public void consumeResourceActionCancelEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceActionCancelEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceActionCancelData);
        ResourceActionCancelData eventData = (ResourceActionCancelData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    // ServiceBus events
    @Test
    public void consumeServiceBusActiveMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusActiveMessagesAvailableWithNoListenersEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ServiceBusActiveMessagesAvailableWithNoListenersEventData);
        ServiceBusActiveMessagesAvailableWithNoListenersEventData eventData = (ServiceBusActiveMessagesAvailableWithNoListenersEventData) events[0].getData();
        assertEquals("testns1", eventData.getNamespaceName());
    }

    @Test
    public void consumeServiceBusDeadletterMessagesAvailableWithNoListenersEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ServiceBusDeadletterMessagesAvailableWithNoListenersEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ServiceBusDeadletterMessagesAvailableWithNoListenersEventData);
        ServiceBusDeadletterMessagesAvailableWithNoListenersEventData eventData = (ServiceBusDeadletterMessagesAvailableWithNoListenersEventData) events[0].getData();
        assertEquals("testns1", eventData.getNamespaceName());
    }

    // Storage events
    @Test
    public void consumeStorageBlobCreatedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobCreatedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof StorageBlobCreatedEventData);
        StorageBlobCreatedEventData eventData = (StorageBlobCreatedEventData) events[0].getData();
        assertEquals("https://myaccount.blob.core.windows.net/testcontainer/file1.txt", eventData.getUrl());
    }

    @Test
    public void consumeStorageBlobDeletedEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("StorageBlobDeletedEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof StorageBlobDeletedEventData);
        StorageBlobDeletedEventData eventData = (StorageBlobDeletedEventData) events[0].getData();
        assertEquals("https://example.blob.core.windows.net/testcontainer/testfile.txt", eventData.getUrl());
    }

    // Resource Manager (Azure Subscription/Resource Group) events
    @Test
    public void consumeResourceWriteSuccessEvent() throws IOException {
        String jsonData = getTestPayloadFromFile("ResourceWriteSuccessEvent.json");
        //

        EventGridEvent[] events = EventGridEvent.parse(jsonData).toArray(new EventGridEvent[0]);

        assertNotNull(events);
        assertTrue(events[0].getData() instanceof ResourceWriteSuccessData);
        ResourceWriteSuccessData eventData = (ResourceWriteSuccessData) events[0].getData();
        assertEquals("72f988bf-86f1-41af-91ab-2d7cd011db47", eventData.getTenantId());
    }

    // TODO: When new event types are introduced, add one test here for each event type

    private String getTestPayloadFromFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] bytes = IOUtils.toByteArray(classLoader.getResourceAsStream("customization/" + fileName));
        return new String(bytes);
    }
}
