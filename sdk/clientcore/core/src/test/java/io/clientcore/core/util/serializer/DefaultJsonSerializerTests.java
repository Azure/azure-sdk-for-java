// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.serializer;

import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;
import io.clientcore.core.models.SimpleClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.clientcore.core.util.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultJsonSerializerTests {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();

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
            return jsonWriter
                .writeStartObject()
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
        DateTimeWrapper actual = new DefaultJsonSerializer()
            .deserializeFromBytes(json.getBytes(), DateTimeWrapper.class);

        assertEquals(expected.getOffsetDateTime(), actual.getOffsetDateTime());
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormatDate = "{\"OffsetDateTime\":\"%s\"}";
        DateTimeWrapper minValue =
            new DateTimeWrapper().setOffsetDateTime(OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        DateTimeWrapper unixEpoch =
            new DateTimeWrapper().setOffsetDateTime(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));

        return Stream.of(
            Arguments.of(String.format(jsonFormatDate, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(jsonFormatDate, "1970-01-01T00:00:00Z"), unixEpoch)
        );
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

    private static Stream<Arguments> textSerializationSupplier() {
        Map<String, String> map = Collections.singletonMap("key", "value");

        return Stream.of(
            Arguments.of(1, "1"),
            Arguments.of(1L, "1"),
            Arguments.of(1.0F, "1.0"),
            Arguments.of(1.0D, "1.0"),
            Arguments.of("1", "\"1\""),
            Arguments.of(HttpMethod.GET, "\"GET\""),
            Arguments.of(HttpExceptionType.RESOURCE_MODIFIED, "\"RESOURCE_MODIFIED\""),
            Arguments.of(HttpExceptionType.fromString(null), null),
            Arguments.of(map, "{\"key\":\"value\"}"),
            Arguments.of(null, null)
        );
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
        Object actual =
            SERIALIZER.deserializeFromStream(new ByteArrayInputStream(inputStreamBytes), type);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> bytesDeserializationSupplier() {
        return Stream.of(
            Arguments.of("\"hello\"".getBytes(StandardCharsets.UTF_8), String.class, "hello"),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Integer.class, 1),
            Arguments.of("1000000000000".getBytes(StandardCharsets.UTF_8), Long.class, 1000000000000L),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), Double.class, 1.0D),
            Arguments.of("true".getBytes(StandardCharsets.UTF_8), Boolean.class, true)
        );
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
        return Stream.of(
            Arguments.of(InputStream.class, IOException.class),
            // Thrown when the String cannot be parsed by core-json
            Arguments.of(SimpleClass.class, InvocationTargetException.class),
            // Thrown when the class doesn't have a fromJson method
            Arguments.of(URL.class, IOException.class),
            // Thrown when the String cannot be parsed by core-json
            Arguments.of(URI.class, IOException.class) // Thrown when the String cannot be parsed by core-json
        );
    }
}
