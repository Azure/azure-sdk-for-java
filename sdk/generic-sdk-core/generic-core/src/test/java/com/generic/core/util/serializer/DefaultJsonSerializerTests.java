// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

import com.generic.core.models.SimpleClass;
//import com.generic.core.http.models.HttpMethod;
import com.generic.core.implementation.AccessibleByteArrayOutputStream;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
//import com.generic.core.implementation.util.DateTimeRfc1123;
//import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.TypeReference;
import com.generic.json.JsonReader;
import com.generic.json.JsonSerializable;
import com.generic.json.JsonToken;
import com.generic.json.JsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URL;
import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
//import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
//import java.util.UUID;
import java.util.stream.Stream;

import static com.generic.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultJsonSerializerTests {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();

    @Test
    public void mapWithEmptyKeyAndNullValue() {
        final MapHolder mapHolder = new MapHolder();

        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        assertEquals("{\"map\":{\"\":null}}", new String(SERIALIZER.serializeToBytes(mapHolder)));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() {
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
    public void deserializeJson(String json, SimpleClass expected) {
        SimpleClass simpleClass = new DefaultJsonSerializer()
            .deserializeFromBytes(json.getBytes(), TypeReference.createInstance(SimpleClass.class));

        assertEquals(expected, simpleClass);
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormat = "{\"field1\":\"%s\",\"field2\":\"%s\"}";
        SimpleClass simpleClass = new SimpleClass("field1", "field2");
        SimpleClass anotherSimpleClass = new SimpleClass("some value", "another value");

        return Stream.of(
            Arguments.of(String.format(jsonFormat, "field1", "field2"), simpleClass),
            Arguments.of(String.format(jsonFormat, "some value", "another value"), anotherSimpleClass)
        );
    }

    private static class DateTimeWrapper implements JsonSerializable<DateTimeWrapper> {
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
                .writeStringField("offsetDateTime", offsetDateTime.toString())
                .writeEndObject();
        }

        public static DateTimeWrapper fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                DateTimeWrapper dateTimeWrapper = new DateTimeWrapper();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("offsetDateTime".equals(fieldName)) {
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
    public void textToStringSerialization(Object value, String expected) {
        assertEquals(expected, new String(SERIALIZER.serializeToBytes(value)));
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToBytesSerialization(Object value, String expected) {
        byte[] actual = SERIALIZER.serializeToBytes(value);

        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, new String(actual, StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToOutputStreamSerialization(Object value, String expected) {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        SERIALIZER.serialize(outputStream, value);

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
            Arguments.of(1.0D, "1.0")/*,
            Arguments.of("1", "1"),
            Arguments.of(HttpMethod.GET, "GET"),
            Arguments.of(map, String.valueOf(map)),
            Arguments.of(null, null)*/
        );
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void stringToTextDeserialization(byte[] stringBytes, Class<?> type, Object expected) {
        Object actual = SERIALIZER.deserializeFromBytes(stringBytes, TypeReference.createInstance(type));

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void bytesToTextDeserialization(byte[] bytes, Class<?> type, Object expected) {
        Object actual = SERIALIZER.deserializeFromBytes(bytes, TypeReference.createInstance(type));

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("bytesDeserializationSupplier")
    public void inputStreamToTextDeserialization(byte[] inputStreamBytes, Class<?> type, Object expected) {
        Object actual =
            SERIALIZER.deserialize(new ByteArrayInputStream(inputStreamBytes), TypeReference.createInstance(type));

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> bytesDeserializationSupplier() throws MalformedURLException {
        /*String urlUri = "https://azure.com";
        byte[] urlUriBytes = urlUri.getBytes(StandardCharsets.UTF_8);
        OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(offsetDateTime);
        LocalDate localDate = LocalDate.now(ZoneOffset.UTC);
        UUID uuid = UUID.randomUUID();
        HttpMethod httpMethod = HttpMethod.GET;
        SimpleClass simpleClass = new SimpleClass("some field", "another field");*/

        return Stream.of(
            Arguments.of("\"hello\"".getBytes(StandardCharsets.UTF_8), String.class, "hello"),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Integer.class, 1),
            Arguments.of("1000000000000".getBytes(StandardCharsets.UTF_8), Long.class, 1000000000000L),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), Double.class, 1.0D),
            Arguments.of("true".getBytes(StandardCharsets.UTF_8), Boolean.class, true)/*,
            Arguments.of(urlUriBytes, URL.class, UrlBuilder.parse(urlUri).toUrl()),
            Arguments.of(urlUriBytes, URI.class, URI.create(urlUri)),
            Arguments.of(getObjectBytes(offsetDateTime), OffsetDateTime.class, offsetDateTime),
            Arguments.of(getObjectBytes(dateTimeRfc1123), DateTimeRfc1123.class, dateTimeRfc1123),
            Arguments.of(getObjectBytes(localDate), LocalDate.class, localDate),
            Arguments.of(getObjectBytes(uuid), UUID.class, uuid),
            Arguments.of(getObjectBytes(httpMethod), HttpMethod.class, httpMethod),
            Arguments.of(getObjectBytes(simpleClass), SimpleClass.class, simpleClass)*/
        );
    }

    /*@ParameterizedTest
    @MethodSource("unsupportedDeserializationSupplier")
    public void unsupportedTextTypesDeserialization(Class<?> unsupportedType,
                                                    Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> {
            try {
                SERIALIZER.deserializeFromBytes(":////".getBytes(), TypeReference.createInstance(unsupportedType));
            } catch (RuntimeException e) {
                throw e.getCause();
            }
        });
    }

    private static Stream<Arguments> unsupportedDeserializationSupplier() {
        return Stream.of(
            Arguments.of(InputStream.class, IllegalArgumentException.class), // JsonParseException
            Arguments.of(SimpleClass.class, IllegalArgumentException.class), // InvocationTargetException
            Arguments.of(URL.class, IllegalArgumentException.class), // Thrown when the String isn't a valid URL // JsonParseException
            Arguments.of(URI.class, IllegalArgumentException.class) // Thrown when the String isn't a valid URI // JsonParseException
        );
    }*/

    private static byte[] getObjectBytes(Object value) {
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }
}
