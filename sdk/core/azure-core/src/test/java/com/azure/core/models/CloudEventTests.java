// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CloudEventTests {
    private static final DefaultJsonSerializer SERIALIZER = new DefaultJsonSerializer();

    @Test
    public void testRoundTripCloudEvents() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventDifferentDataTypes.json");
        final CloudEvent cloudEvent = CloudEvent.fromString(cloudEventJson).get(0);
        final Map<String, Object> map = new HashMap<String, Object>() {
            {
                put("str", "str value");
                put("number", 1.3);
                put("integer", 1);
                put("bool", true);
                put("null", null);
                put("array", new ArrayList<Integer>() {
                    {
                        add(1);
                        add(2);
                        add(3);
                    }
                });
                put("object", new HashMap<String, String>() {
                    {
                        put("okey", "ovalue");
                    }
                });
            }
        };

        // Check if deserialized CloudEvent has the correct properties.
        assertNotNull(cloudEvent);
        assertEquals("/testresource", cloudEvent.getSource());
        assertEquals("Microsoft.MockPublisher.TestEvent", cloudEvent.getType());
        assertEquals(OffsetDateTime.parse("2020-07-21T18:41:31.166Z"), cloudEvent.getTime());
        assertEquals("9ddf9b10-fe3d-4a16-94bc-c0298924ded1", cloudEvent.getId());
        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertEquals("testsubject", cloudEvent.getSubject());
        assertEquals("/testschema", cloudEvent.getDataSchema());
        assertEquals("application/json", cloudEvent.getDataContentType());
        assertEquals(map, cloudEvent.getData().toObject(Map.class, SERIALIZER));

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serialized = bos.toString();

            // Check if the original map matches the raw deserialized JsonNode from the serialized String
            assertMapAndJsonNode(map, serialized);

            // Check if the original CloudEvent matches the deserialized CloudEvent from the serialized String
            final CloudEvent cloudEvent2 = CloudEvent.fromString(serialized).get(0);
            assertEquals("/testresource", cloudEvent2.getSource());
            assertEquals("Microsoft.MockPublisher.TestEvent", cloudEvent2.getType());
            assertEquals(OffsetDateTime.parse("2020-07-21T18:41:31.166Z"), cloudEvent2.getTime());
            assertEquals("9ddf9b10-fe3d-4a16-94bc-c0298924ded1", cloudEvent2.getId());
            assertEquals("1.0", cloudEvent2.getSpecVersion());
            assertEquals("testsubject", cloudEvent2.getSubject());
            assertEquals("/testschema", cloudEvent2.getDataSchema());
            assertEquals("application/json", cloudEvent2.getDataContentType());

            final BinaryData data2 = cloudEvent.getData();
            final Map<String, Object> deserializedData2 = data2.toObject(new TypeReference<Map<String, Object>>() {
            }, SERIALIZER);

            // Check if the original map matches the deserialized data map from the serialized String
            assertEquals(map, deserializedData2);
        }
    }

    @Test
    public void deserializeCloudEventsFailValidation() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventNoType.json");
        assertThrows(IllegalArgumentException.class, () -> {
            CloudEvent.fromString(cloudEventJson);
        });
    }

    @Test
    public void deserializeCloudEventsSkipValidation() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventNoType.json");
        assertDoesNotThrow(() -> {
            CloudEvent.fromString(cloudEventJson, true);
        });
    }

    @Test
    public void deserializeCloudEventWithoutArrayBrackets() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventNoArray.json");

        final List<CloudEvent> events = CloudEvent.fromString(cloudEventJson);

        assertNotNull(events);
        assertEquals(1, events.size());

        final ContosoItemReceivedEventData data = events.get(0).getData().toObject(ContosoItemReceivedEventData.class,
            SERIALIZER);
        assertNotNull(data);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", data.getItemSku());

        final Map<String, Object> additionalProperties = events.get(0).getExtensionAttributes();

        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.containsKey("foo"));
        assertEquals("bar", additionalProperties.get("foo"));
    }

    @Test
    public void deserializeCloudEventWithNullData() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventNullData.json");

        final List<CloudEvent> events = CloudEvent.fromString(cloudEventJson);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertNull(events.get(0).getData());
    }

    @Test
    public void deserializeCloudEventWithBinaryData() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventBinaryData.json");
        final List<CloudEvent> events = CloudEvent.fromString(cloudEventJson);

        assertNotNull(events);
        assertEquals(1, events.size());

        final byte[] eventData = events.get(0).getData().toBytes();

        assertNotNull(eventData);

        assertArrayEquals(Base64.getDecoder().decode("c2FtcGxlYmluYXJ5ZGF0YQ=="), eventData);
    }

    @Test
    public void deserializeCloudEventJsonData() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventJsonData.json");

        final List<CloudEvent> events = CloudEvent.fromString(cloudEventJson);

        assertNotNull(events);
        assertEquals(1, events.size());

        final ContosoItemReceivedEventData data = events.get(0).getData().toObject(ContosoItemReceivedEventData.class,
            SERIALIZER);
        assertNotNull(data);

        assertEquals("512d38b6-c7b8-40c8-89fe-f46f9e9622b6", data.getItemSku());

        final Map<String, Object> additionalProperties = events.get(0).getExtensionAttributes();

        assertNotNull(additionalProperties);
        assertTrue(additionalProperties.containsKey("foo"));
        assertEquals("bar", additionalProperties.get("foo"));
    }

    @Test
    public void deserializeCloudEventXmlData() throws IOException {
        final String cloudEventJson = getTestPayloadFromFile("CloudEventXmlData.json");

        final List<CloudEvent> events = CloudEvent.fromString(cloudEventJson);

        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(events.get(0).getExtensionAttributes().get("comexampleothervalue"), 5);

        final String xmlData = events.get(0).getData().toObject(String.class, SERIALIZER);

        assertEquals("<much wow=\"xml\"/>", xmlData);
    }

    @Test
    public void serializeByteData() throws IOException {
        final String dataPayload = "AAA";
        final BinaryData binaryData = BinaryData.fromBytes(dataPayload.getBytes(StandardCharsets.UTF_8));
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.BYTES, "bytes")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(cloudEvent.getData().toString(), deserializedCloudEvent.getData().toString());
            assertEquals(dataPayload, cloudEvent.getData().toString());
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void serializeJsonData() throws IOException {
        final Map<String, Object> mapData = new HashMap<String, Object>() {
            {
                put("Field1", "Value1");
                put("Field2", "Value2");
            }
        };
        final BinaryData binaryData = BinaryData.fromObject(mapData, SERIALIZER);
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(mapData, deserializedCloudEvent.getData().toObject(new TypeReference<Map<String, Object>>() {
            }, SERIALIZER));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveDataValues")
    public void serializePrimitiveData(Object dataValue) throws IOException {
        final BinaryData binaryData = BinaryData.fromObject(dataValue, SERIALIZER);
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final ObjectMapper objectMapper = new ObjectMapper();

            // check the "data" value of the serialized String
            final JsonNode jsonNode = objectMapper.readTree(serializedString).get("data");
            if (jsonNode.isInt()) {
                assertEquals(dataValue, jsonNode.asInt());
            } else if (jsonNode.isDouble()) {
                assertEquals(dataValue, jsonNode.asDouble());
            } else if (jsonNode.isBoolean()) {
                assertEquals(dataValue, jsonNode.asBoolean());
            } else if (jsonNode.isTextual()) {
                assertEquals(dataValue, jsonNode.asText());
            } else {
                fail("Wrong JsonNode type. The serialized String might be wrong.");
            }

            // Deserialized the serialized String and check back the CloudEvent.getData()
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(dataValue, deserializedCloudEvent.getData().toObject(dataValue.getClass(), SERIALIZER));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    private static Stream<Arguments> primitiveDataValues() {
        return Stream.of(
            Arguments.of("str"),
            Arguments.of(1),
            Arguments.of(1.1),
            Arguments.of(true),
            Arguments.of(false)
        );
    }

    @Test
    public void serializeStringDataAsObject() throws IOException {
        final String dataPayload = "AAA";
        final BinaryData binaryData = BinaryData.fromObject(dataPayload, SERIALIZER);
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals(dataPayload, deserializedCloudEvent.getData().toObject(String.class, SERIALIZER));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void serializeStringDataNonJsonLiteral() {
        final String dataPayload = "AAA";
        final BinaryData binaryData = BinaryData.fromString(dataPayload);
        assertThrows(IllegalArgumentException.class, () -> {
            new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
                .setDataSchema("/testSchema")
                .setSubject("testSubject")
                .setTime(OffsetDateTime.now())
                .setSpecVersion("1.0")
                .addExtensionAttribute("foo", "value");
        });
    }

    @Test
    public void serializeStringDataJsonLiteral() throws IOException {
        final BinaryData binaryData = BinaryData.fromString("{\"foo\":\"value\"}");
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", binaryData, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertEquals("{\"foo\":\"value\"}", deserializedCloudEvent.getData().toString());
            final Map<String, String> deserializedMap = deserializedCloudEvent.getData().toObject(
                new TypeReference<Map<String, String>>() {

                }, SERIALIZER);
            assertEquals("value", deserializedMap.get("foo"));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void serializeNullBinaryData() throws IOException {
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test", null, CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertNull(deserializedCloudEvent.getData());
            assertEquals("value", deserializedCloudEvent.getExtensionAttributes().get("foo"));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @Test
    public void serializeBinaryDataFromNull() throws IOException {
        final CloudEvent cloudEvent = new CloudEvent("/testSource", "CloudEvent.Test",
            BinaryData.fromObject(null, SERIALIZER),
            CloudEventDataFormat.JSON, "application/json")
            .setDataSchema("/testSchema")
            .setSubject("testSubject")
            .setTime(OffsetDateTime.now())
            .setSpecVersion("1.0")
            .addExtensionAttribute("foo", "value");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SERIALIZER.serialize(bos, cloudEvent);
            final String serializedString = bos.toString();
            final CloudEvent deserializedCloudEvent = CloudEvent.fromString(serializedString).get(0);
            assertNull(deserializedCloudEvent.getData().toObject(Object.class, SERIALIZER));  // any class works here.
            assertEquals("value", deserializedCloudEvent.getExtensionAttributes().get("foo"));
            compareCloudEventContent(cloudEvent, deserializedCloudEvent);
        }
    }

    @ParameterizedTest
    @MethodSource("addAttributeIllegalArgumentTestData")
    public void addAttributeIllegalArgument(String attributeName, Object value) {
        assertThrows(IllegalArgumentException.class, () -> {
            new CloudEvent("/testsrouce", "testtype", BinaryData.fromObject("str", SERIALIZER),
                CloudEventDataFormat.JSON, "application/json")
                .addExtensionAttribute(attributeName, value);
        });
    }

    private static Stream<Arguments> addAttributeIllegalArgumentTestData() {
        return Stream.of(
            Arguments.of("a_b", "value"),
            Arguments.of("Ab", 1)
        );
    }

    @ParameterizedTest
    @MethodSource("addAttributeNullPointerTestData")
    public void addAttributeNullPointer(String attributeName, Object value) {
        assertThrows(NullPointerException.class, () -> {
            new CloudEvent("/testsrouce", "testtype", BinaryData.fromObject("str", SERIALIZER),
                CloudEventDataFormat.JSON, "application/json")
                .addExtensionAttribute(attributeName, value);
        });
    }

    private static Stream<Arguments> addAttributeNullPointerTestData() {
        return Stream.of(
            Arguments.of(null, "value"),
            Arguments.of("name", null)
        );
    }

    @Test
    public void addAttribute() {
        assertDoesNotThrow(() -> {
            new CloudEvent("/testsrouce", "testtype", BinaryData.fromObject("str", SERIALIZER),
                CloudEventDataFormat.JSON, "application/json")
                .addExtensionAttribute("name", "value");
        });
    }

    private String getTestPayloadFromFile(String fileName) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("CloudEvent/" + fileName)) {
            final byte[] bytes = new byte[inputStream.available()];
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

    private void assertMapAndJsonNode(Map<String, Object> map, String jsonString) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(jsonString);
        assertMapAndJsonNode(map, jsonNode.get("data"));
    }

    @SuppressWarnings("unchecked")
    private void assertMapAndJsonNode(Map<String, Object> map, JsonNode jsonNode) {
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                assertMapAndJsonNode((Map<String, Object>) map.get(key), jsonNode.get(key));
            } else if (value instanceof String) {
                assertEquals(value, jsonNode.get(key).asText());
            } else if (value instanceof Boolean) {
                assertEquals(value, jsonNode.get(key).asBoolean());
            } else if (value instanceof Integer) {
                assertEquals(value, jsonNode.get(key).asInt());
            } else if (value instanceof Double) {
                assertEquals(value, jsonNode.get(key).asDouble());
            } else if (value instanceof List) {
                List<?> elements = (List<?>) value;
                JsonNode arrayNode = jsonNode.get(key);
                for (int i = 0; i < elements.size(); i++) {
                    assertEquals(elements.get(i), arrayNode.get(i).asInt());
                }
            }
        });
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
