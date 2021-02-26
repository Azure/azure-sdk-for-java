// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.serializer.JacksonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloudEventTests {
    private static final JacksonSerializer SERIALIZER = new JacksonSerializer();

    @Test
    public void testRoundTripCloudEvents() throws IOException {
        String cloudEventJson = getTestPayloadFromFile("CloudEventDifferentDataTypes.json");
        CloudEvent cloudEvent = CloudEvent.fromString(cloudEventJson).get(0);

        assertNotNull(cloudEvent);
        assertEquals("/testresource", cloudEvent.getSource());
        assertEquals("Microsoft.MockPublisher.TestEvent", cloudEvent.getType());
        assertEquals(OffsetDateTime.parse("2020-07-21T18:41:31.166Z"), cloudEvent.getTime());
        assertEquals("9ddf9b10-fe3d-4a16-94bc-c0298924ded1", cloudEvent.getId());
        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertEquals("testsubject", cloudEvent.getSubject());
        assertEquals("/testschema", cloudEvent.getDataSchema());
        assertEquals("application/json", cloudEvent.getDataContentType());

        // actually deserialized as a LinkedHashMap instead of generic object.
        BinaryData data = cloudEvent.getData();
        Map<String, Object> deserializedData = data.toObject(new TypeReference<Map<String, Object>>() {
        });
        assertEquals(deserializedData.get("str"), "str value");
        assertEquals(deserializedData.get("number"), 1.3);
        assertEquals(deserializedData.get("integer"), 1);
        assertEquals(deserializedData.get("bool"), true);
        assertNull(deserializedData.get("null"));
        assertEquals(((Map<?, ?>) deserializedData.get("object")).get("okey"), "ovalue");

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            String serialized = bos.toString();

            CloudEvent cloudEvent2 = CloudEvent.fromString(serialized).get(0);
            assertEquals("/testresource", cloudEvent2.getSource());
            assertEquals("Microsoft.MockPublisher.TestEvent", cloudEvent2.getType());
            assertEquals(OffsetDateTime.parse("2020-07-21T18:41:31.166Z"), cloudEvent2.getTime());
            assertEquals("9ddf9b10-fe3d-4a16-94bc-c0298924ded1", cloudEvent2.getId());
            assertEquals("1.0", cloudEvent2.getSpecVersion());
            assertEquals("testsubject", cloudEvent2.getSubject());
            assertEquals("/testschema", cloudEvent2.getDataSchema());
            assertEquals("application/json", cloudEvent2.getDataContentType());

            BinaryData data2 = cloudEvent.getData();
            Map<String, Object> deserializedData2 = data2.toObject(new TypeReference<Map<String, Object>>() {
            });
            assertEquals(deserializedData2.get("str"), "str value");
            assertEquals(deserializedData2.get("number"), 1.3);
            assertEquals(deserializedData2.get("integer"), 1);
            assertEquals(deserializedData2.get("bool"), true);
            assertNull(deserializedData2.get("null"));
            assertEquals(((Map<?, ?>) deserializedData2.get("object")).get("okey"), "ovalue");
        }
    }

    @Test
    public void testDeserializeCloudEventsFailValidation() throws IOException {
        String cloudEventJson = getTestPayloadFromFile("CloudEventNoType.json");
        assertThrows(IllegalArgumentException.class, () -> {
            CloudEvent.fromString(cloudEventJson);
        });
    }

    @Test
    public void testDeserializeCloudEventsSkipValidation() throws IOException {
        String cloudEventJson = getTestPayloadFromFile("CloudEventNoType.json");
        assertDoesNotThrow(() -> {
            CloudEvent.fromString(cloudEventJson, true);
        });
    }

    @Test
    public void consumeCloudEventWithoutArrayBrackets() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventNoArray.json");

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        ContosoItemReceivedEventData data = events.get(0).getData().toObject(ContosoItemReceivedEventData.class);
        assertNotNull(data);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", data.getItemSku());

        Map<String, Object> additionalProperties = events.get(0).getExtensionAttributes();

        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.containsKey("foo"));
        assertEquals("bar", additionalProperties.get("foo"));
    }

    @Test
    public void consumeCloudEventWithNullData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventNullData.json");

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertNull(events.get(0).getData());
    }

    @Test
    public void consumeCloudEventWithBinaryData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventBinaryData.json");

        byte[] data = "c2FtcGxlYmluYXJ5ZGF0YQ==".getBytes(StandardCharsets.UTF_8);

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        byte[] eventData = events.get(0).getData().toBytes();

        assertNotNull(eventData);

        assertArrayEquals(data, eventData);
    }

    @Test
    public void consumeCloudEventJsonData() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventJsonData.json");

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        ContosoItemReceivedEventData data = events.get(0).getData().toObject(ContosoItemReceivedEventData.class);
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

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getExtensionAttributes().get("comexampleothervalue"), 5);

        String xmlData = events.get(0).getData().toString();

        assertEquals("<much wow=\"xml\"/>", xmlData);
    }

    @Test
    public void testSerializeByteDataAndAllAttributes() throws IOException {
        final String dataPayload = "AAA";
        BinaryData binaryData = BinaryData.fromBytes(dataPayload.getBytes(StandardCharsets.UTF_8));
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.BYTES, "bytes")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            String serializedString = bos.toString();
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(cloudEvent.getData().toString(), deserializedCloudEvent.getData().toString());
            assertEquals(new String(Base64.getDecoder().decode(cloudEvent.getData().toBytes())), dataPayload);
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void testSerializeJsonData() throws IOException {
        Map<String, Object> mapData = new HashMap<String, Object>() {
            {
                put("Field1", "Value1");
                put("Field2", "Value2");
            }
        };
        BinaryData binaryData = BinaryData.fromObject(mapData);
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            String serializedString = bos.toString();
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(mapData, deserializedCloudEvent.getData().toObject(new TypeReference<Map<String, Object>>() {
            }));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void testSerializeStringData() throws IOException {
        final String dataPayload = "AAA";
        BinaryData binaryData = BinaryData.fromString(dataPayload);
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            String serializedString = bos.toString();
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(dataPayload, deserializedCloudEvent.getData().toString());
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void testSerializeStringDataJsonLiteral() throws IOException {
        BinaryData binaryData = BinaryData.fromString("{\"foo\":\"value\"}");
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            String serializedString = bos.toString();
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals("{\"foo\":\"value\"}", deserializedCloudEvent.getData().toString());
            Map<String, String> deserializedMap = deserializedCloudEvent.getData().toObject(
                new TypeReference<Map<String, String>>() {

                });
            assertEquals("value", deserializedMap.get("foo"));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);

        }
    }

    private String getTestPayloadFromFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("CloudEvent/" + fileName)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes);
        }
    }

    private void compareCloudEventContent(CloudEvent cloudEvent, CloudEvent deserializedCloudEvent) {
        assertEquals(cloudEvent.getSpecVersion(), deserializedCloudEvent.getSpecVersion());
        assertEquals(cloudEvent.getDataSchema(), deserializedCloudEvent.getDataSchema());
        assertEquals(cloudEvent.getDataContentType(), deserializedCloudEvent.getDataContentType());
        assertEquals(cloudEvent.getId(), deserializedCloudEvent.getId());
        assertEquals(cloudEvent.getExtensionAttributes(), deserializedCloudEvent.getExtensionAttributes());
        assertEquals(cloudEvent.getSource(), deserializedCloudEvent.getSource());
        assertEquals(cloudEvent.getSubject(), deserializedCloudEvent.getSubject());
        assertEquals(cloudEvent.getTime().toInstant(), deserializedCloudEvent.getTime().toInstant());
        assertEquals(cloudEvent.getType(), deserializedCloudEvent.getType());
    }

    private static class ContosoItemReceivedEventData {
        @JsonProperty(value = "itemSku", access = JsonProperty.Access.WRITE_ONLY)
        private String itemSku;

        @JsonProperty(value = "itemUri", access = JsonProperty.Access.WRITE_ONLY)
        private String itemUri;

        public String getItemSku() {
            return this.itemSku;
        }

        public String getItemUri() {
            return this.itemUri;
        }
    }
}
