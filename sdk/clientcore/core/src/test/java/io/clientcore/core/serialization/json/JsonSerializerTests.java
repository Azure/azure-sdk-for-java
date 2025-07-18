// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.models.Person;
import io.clientcore.core.models.SimpleClass;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSerializerTests {
    private static final JsonSerializer SERIALIZER = new JsonSerializer();

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();

        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        assertEquals("{\"map\":{\"\":null}}", new String(SERIALIZER.serializeToBytes(mapHolder)));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();

        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");

        assertEquals("{\"map\":{\"\":\"\"}}", new String(SERIALIZER.serializeToBytes(mapHolder)));
    }

    private static class MapHolder implements JsonSerializable<MapHolder> {
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeMapField("map", this.map, JsonWriter::writeString)
                .writeEndObject();
        }

        public static MapHolder fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                MapHolder mapHolder = new MapHolder();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("map".equals(fieldName)) {
                        mapHolder.map = reader.readMap(JsonReader::getString);
                    } else {
                        reader.skipChildren();
                    }
                }

                return mapHolder;
            });
        }
    }

    @ParameterizedTest
    @MethodSource("deserializeJsonSupplier")
    public void deserializeJson(String json, DateTimeWrapper expected) throws IOException {
        DateTimeWrapper actual = new JsonSerializer().deserializeFromBytes(json.getBytes(), DateTimeWrapper.class);

        assertEquals(expected.getOffsetDateTime(), actual.getOffsetDateTime());
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormatDate = "{\"OffsetDateTime\":\"%s\"}";
        DateTimeWrapper minValue
            = new DateTimeWrapper().setOffsetDateTime(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        DateTimeWrapper unixEpoch
            = new DateTimeWrapper().setOffsetDateTime(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));

        return Stream.of(Arguments.of(String.format(jsonFormatDate, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(jsonFormatDate, "1970-01-01T00:00:00Z"), unixEpoch));
    }

    public static class DateTimeWrapper implements JsonSerializable<DateTimeWrapper> {
        private OffsetDateTime offsetDateTime;

        public DateTimeWrapper setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;

            return this;
        }

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("OffsetDateTime", offsetDateTime.toString())
                .writeEndObject();
        }

        public static DateTimeWrapper fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                DateTimeWrapper dateTimeWrapper = new DateTimeWrapper();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("OffsetDateTime".equals(fieldName)) {
                        dateTimeWrapper.offsetDateTime = OffsetDateTime.parse(reader.getString());
                    } else {
                        reader.skipChildren();
                    }
                }

                return dateTimeWrapper;
            });
        }
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToStringSerialization(Object value, String expected) throws IOException {
        if (expected == null) {
            assertNull(value);
        } else {
            assertEquals(expected, new String(SERIALIZER.serializeToBytes(value)));
        }
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToBytesSerialization(Object value, String expected) throws IOException {
        byte[] actual = SERIALIZER.serializeToBytes(value);

        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, new String(actual, StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToOutputStreamSerialization(Object value, String expected) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        SERIALIZER.serializeToStream(outputStream, value);

        if (expected == null) {
            assertEquals(0, outputStream.count());
        } else {
            assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("binaryDataListSerializationSupplier")
    public void testBinaryDataListSerialization(List<BinaryData> list, String expected) throws IOException {
        byte[] bytes = SERIALIZER.serializeToBytes(list);
        assertEquals(expected, new String(bytes, StandardCharsets.UTF_8));
    }

    private static Stream<Arguments> binaryDataListSerializationSupplier() {
        return Stream.of(
            Arguments.of(Arrays.asList(BinaryData.fromString("hello"), BinaryData.fromObject(5)), "[\"hello\",5]"),
            Arguments.of(
                Arrays.asList(BinaryData.fromString("hello"), BinaryData.fromObject(5),
                    BinaryData.fromObject(new Person().setAge(3).setName("John"))),
                "[\"hello\",5,{\"name\":\"John\",\"age\":3}]"));
    }

    @Test
    public void testBinaryDataListDeserialization() throws IOException {
        byte[] bytes = "[\"hello\", 5, {\"name\":\"John\",\"age\":3}]".getBytes(StandardCharsets.UTF_8);

        ParameterizedType type = TypeUtil.createParameterizedType(List.class, BinaryData.class);

        List<BinaryData> binaryDataList = SERIALIZER.deserializeFromBytes(bytes, type);
        assertNotNull(binaryDataList);
        assertEquals(3, binaryDataList.size());
        assertTrue(binaryDataList.get(0) instanceof BinaryData);
        assertTrue(binaryDataList.get(1) instanceof BinaryData);
        assertTrue(binaryDataList.get(2) instanceof BinaryData);

        assertEquals("hello", binaryDataList.get(0).toObject(String.class));
        assertEquals(5, (int) binaryDataList.get(1).toObject(Integer.class));

        Person person = binaryDataList.get(2).toObject(Person.class);
        assertEquals("John", person.getName());
        assertEquals(3, person.getAge());
    }

    @ParameterizedTest
    @MethodSource("binaryDataSerializationSupplier")
    public void testBinaryDataSerialization(BinaryData binaryData, String expected) throws IOException {
        assertEquals(expected, new String(SERIALIZER.serializeToBytes(binaryData)));
    }

    private static Stream<Arguments> binaryDataSerializationSupplier() {
        return Stream.of(Arguments.of(BinaryData.fromObject(5), "5"), Arguments.of(BinaryData.fromObject("1"), "\"1\""),
            Arguments.of(BinaryData.fromString("3"), "\"3\""), Arguments
                .of(BinaryData.fromObject(new Person().setAge(3).setName("John")), "{\"name\":\"John\",\"age\":3}"));
    }

    private static Stream<Arguments> textSerializationSupplier() {
        Map<String, String> map = Collections.singletonMap("key", "value");

        return Stream.of(Arguments.of(1, "1"), Arguments.of(1L, "1"), Arguments.of(1.0F, "1.0"),
            Arguments.of(1.0D, "1.0"), Arguments.of("1", "\"1\""), Arguments.of(HttpMethod.GET, "\"GET\""),
            Arguments.of(map, "{\"key\":\"value\"}"), Arguments.of(null, null));
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void stringToTextDeserialization(byte[] stringBytes, Class<?> type, Object expected) throws IOException {
        Object actual = SERIALIZER.deserializeFromBytes(stringBytes, type);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void bytesToTextDeserialization(byte[] bytes, Class<?> type, Object expected) throws IOException {
        Object actual = SERIALIZER.deserializeFromBytes(bytes, type);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void inputStreamToTextDeserialization(byte[] inputStreamBytes, Class<?> type, Object expected)
        throws IOException {
        Object actual = SERIALIZER.deserializeFromStream(new ByteArrayInputStream(inputStreamBytes), type);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> bytesDeserializationSupplier() {
        return Stream.of(Arguments.of("\"hello\"".getBytes(StandardCharsets.UTF_8), String.class, "hello"),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Integer.class, 1),
            Arguments.of("1000000000000".getBytes(StandardCharsets.UTF_8), Long.class, 1000000000000L),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), Double.class, 1.0D),
            Arguments.of("true".getBytes(StandardCharsets.UTF_8), Boolean.class, true));
    }

    @ParameterizedTest
    @MethodSource("unsupportedDeserializationSupplier")
    public void unsupportedTextTypesDeserialization(Class<?> unsupportedType,
        Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> {
            try {
                SERIALIZER.deserializeFromBytes(":////".getBytes(), unsupportedType);
            } catch (RuntimeException e) {
                throw e.getCause();
            }
        });
    }

    private static Stream<Arguments> unsupportedDeserializationSupplier() {
        return Stream.of(Arguments.of(InputStream.class, IOException.class),
            // Thrown when the String cannot be parsed by core
            Arguments.of(SimpleClass.class, InvocationTargetException.class),
            // Thrown when the class doesn't have a fromJson method
            Arguments.of(URL.class, IOException.class),
            // Thrown when the String cannot be parsed by core
            Arguments.of(URI.class, IOException.class) // Thrown when the String cannot be parsed by core
        );
    }

    @Test
    public void deserializeListOfJsonSerializableTypes() throws IOException {
        byte[] bytes = "[{\"property\":\"value1\"},{\"property\":\"value2\"}]".getBytes(StandardCharsets.UTF_8);

        ParameterizedType type = TypeUtil.createParameterizedType(List.class, FooModel.class);

        List<FooModel> models = SERIALIZER.deserializeFromBytes(bytes, type);
        assertNotNull(models);
        assertEquals(2, models.size());
        assertEquals("value1", models.get(0).getProperty());
        assertEquals("value2", models.get(1).getProperty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deserializeListOfNonJsonSerializableTypes() throws IOException {
        byte[] bytes = "[{\"property\":\"value1\"},{\"property\":\"value2\"}]".getBytes(StandardCharsets.UTF_8);

        ParameterizedType type = TypeUtil.createParameterizedType(List.class, BarModel.class);

        List<?> models = SERIALIZER.deserializeFromBytes(bytes, type);
        assertNotNull(models);
        assertEquals(2, models.size());
        assertTrue(models.get(0) instanceof LinkedHashMap);

        if (models.get(0) instanceof LinkedHashMap) {
            LinkedHashMap<String, String> model = (LinkedHashMap<String, String>) models.get(0);
            assertEquals("value1", model.get("property"));
        }
    }

    /**
     * A model that implements {@link JsonSerializable}.
     */
    public static final class FooModel implements JsonSerializable<FooModel> {
        private final String property;

        public FooModel(String property) {
            this.property = property;
        }

        public String getProperty() {
            return this.property;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("property", this.property);
            return jsonWriter.writeEndObject();
        }

        public static FooModel fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                String property = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("property".equals(fieldName)) {
                        property = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }
                FooModel deserializedModel = new FooModel(property);

                return deserializedModel;
            });
        }
    }

    /**
     * A model that does not implement {@link JsonSerializable}.
     */
    public static final class BarModel {
        private final String property;

        public BarModel(String property) {
            this.property = property;
        }

        public String getProperty() {
            return this.property;
        }
    }
}
