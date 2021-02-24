// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloudEventTests {
    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    @Test
    public void testDeserializeCloudEvents() {
        String cloudEventJson = "{\n"
            + "  \"id\": \"9ddf9b10-fe3d-4a16-94bc-c0298924ded1\",\n"
            + " \"source\": \"/testresource\",\n"
            + " \"datacontenttype\": \"application/json\",\n"
            + " \"dataschema\": \"/testschema\",\n"
            + " \"subject\": \"testsubject\",\n"
            + "  \"data\": {\n"
            + "    \"Field2\": \"Value2\",\n"
            + "    \"Field3\": \"Value3\",\n"
            + "    \"Field1\": \"Value1\"\n"
            + "  },\n"
            + "  \"type\": \"Microsoft.MockPublisher.TestEvent\",\n"
            + "  \"time\": \"2020-07-21T18:41:31.166Z\",\n"
            + "  \"specversion\": \"1.0\"\n"
            + "}";

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
        Map<String, Object> deserializedData = data.toObject(new TypeReference<>() {
        });
        assertEquals(deserializedData.get("Field1"), "Value1");
        assertEquals(deserializedData.get("Field2"), "Value2");
        assertEquals(deserializedData.get("Field3"), "Value3");
    }

    @Test
    public void testDeserializeCloudEventsFailValidation() {
        String cloudEventJson = "{\n"
            + "  \"id\": \"9ddf9b10-fe3d-4a16-94bc-c0298924ded1\",\n"
            + " \"datacontenttype\": \"application/json\",\n"
            + " \"dataschema\": \"/testschema\",\n"
            + " \"subject\": \"testsubject\",\n"
            + "  \"data\": {\n"
            + "    \"Field2\": \"Value2\",\n"
            + "    \"Field3\": \"Value3\",\n"
            + "    \"Field1\": \"Value1\"\n"
            + "  },\n"
            + "  \"type\": \"Microsoft.MockPublisher.TestEvent\",\n"
            + "  \"time\": \"2020-07-21T18:41:31.166Z\",\n"
            + "  \"specversion\": \"1.0\"\n"
            + "}";
        assertThrows(IllegalArgumentException.class, () -> {CloudEvent.fromString(cloudEventJson);});
    }

    @Test
    public void consumeCloudEventWithoutArrayBrackets() throws IOException {
        String jsonData = getTestPayloadFromFile("CloudEventNoArray.json");

        List<CloudEvent> events = CloudEvent.fromString(jsonData);

        assertNotNull(events);
        assertEquals(1, events.size());

        ContosoItemReceivedEventData data = events.get(0).getData().toObject(
            TypeReference.createInstance(ContosoItemReceivedEventData.class));
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
    public void testSerializeByteDataAndAllAttributes() {
        BinaryData binaryData = BinaryData.fromBytes("AAA".getBytes(StandardCharsets.UTF_8));
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.BYTES, "bytes")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try {
            String serializedString = SERIALIZER.serialize(cloudEvent, SerializerEncoding.JSON);
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(cloudEvent.getData().toString(), deserializedCloudEvent.getData().toString());
            assertEquals(new String(Base64.getDecoder().decode(cloudEvent.getData().toBytes())), "AAA");
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSerializeJsonData() {
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
        try {
            String serializedString = SERIALIZER.serialize(cloudEvent, SerializerEncoding.JSON);
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(mapData, deserializedCloudEvent.getData().toObject(new TypeReference<Map<String, Object>>() {
            }));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSerializeStringData() {
        BinaryData binaryData = BinaryData.fromString("AAA");
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try {
            String serializedString = SERIALIZER.serialize(cloudEvent, SerializerEncoding.JSON);
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals("AAA", deserializedCloudEvent.getData().toString());
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSerializeStringDataJsonLiteral() {
        BinaryData binaryData = BinaryData.fromString("{\"foo\":\"value\"}");
        CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try {
            String serializedString = SERIALIZER.serialize(cloudEvent, SerializerEncoding.JSON);
            CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals("{\"foo\":\"value\"}", deserializedCloudEvent.getData().toString());
            Map<String, String> deserializedMap = deserializedCloudEvent.getData().toObject(new TypeReference<>() {
            });
            assertEquals("value", deserializedMap.get("foo"));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);

        } catch (IOException e) {
            throw new RuntimeException(e);
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
