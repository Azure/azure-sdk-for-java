// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JacksonAdapterTests {
    @Test
    public void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":null}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":\"\"}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    private static class MapHolder {
        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }
    }

    @JacksonXmlRootElement(localName = "XmlString")
    private static class XmlString {
        @JsonProperty("Value")
        private String value;

        public String getValue() {
            return value;
        }
    }

    @ParameterizedTest
    @MethodSource("convertTextSupplier")
    public <T> void convertText(String value, Class<T> type, T expected) throws ReflectiveOperationException {
        assertEquals(expected, JacksonAdapter.convertText(value, type));
    }

    private static Stream<Arguments> convertTextSupplier() {
        return Stream.of(
            // byte
            Arguments.of(null, byte.class, (byte) 0),
            Arguments.of("", byte.class, (byte) 0),
            Arguments.of("-128", byte.class, (byte) -128),

            // Byte
            Arguments.of(null, Byte.class, null),
            Arguments.of("", Byte.class, null),
            Arguments.of("-128", Byte.class, (byte) -128),

            // short
            Arguments.of(null, short.class, (short) 0),
            Arguments.of("", short.class, (short) 0),
            Arguments.of("128", short.class, (short) 128),

            // Short
            Arguments.of(null, Short.class, null),
            Arguments.of("", Short.class, null),
            Arguments.of("128", Short.class, (short) 128),

            // int
            Arguments.of(null, int.class, 0),
            Arguments.of("", int.class, 0),
            Arguments.of("128", int.class, 128),

            // Integer
            Arguments.of(null, Integer.class, null),
            Arguments.of("", Integer.class, null),
            Arguments.of("128", Integer.class, 128),

            // long
            Arguments.of(null, long.class, 0L),
            Arguments.of("", long.class, 0L),
            Arguments.of("128", long.class, 128L),

            // Long
            Arguments.of(null, Long.class, null),
            Arguments.of("", Long.class, null),
            Arguments.of("128", Long.class, 128L),

            // float
            Arguments.of(null, float.class, 0F),
            Arguments.of("", float.class, 0F),
            Arguments.of("128", float.class, 128F),

            // Float
            Arguments.of(null, Float.class, null),
            Arguments.of("", Float.class, null),
            Arguments.of("128", Float.class, 128F),

            // double
            Arguments.of(null, double.class, 0D),
            Arguments.of("", double.class, 0D),
            Arguments.of("128", double.class, 128D),

            // Double
            Arguments.of(null, Double.class, null),
            Arguments.of("", Double.class, null),
            Arguments.of("128", Double.class, 128D),

            // boolean
            Arguments.of(null, boolean.class, false),
            Arguments.of("", boolean.class, false),
            Arguments.of("true", boolean.class, true),

            // Boolean
            Arguments.of(null, Boolean.class, null),
            Arguments.of("", Boolean.class, null),
            Arguments.of("true", Boolean.class, true),

            // Enum
            Arguments.of(null, HttpMethod.class, null),
            Arguments.of("", HttpMethod.class, null),
            Arguments.of("GET", HttpMethod.class, HttpMethod.GET),
            Arguments.of("POST", HttpMethod.class, HttpMethod.POST),

            // ExpandableStringEnum
            Arguments.of(null, LongRunningOperationStatus.class, null),
            Arguments.of("", LongRunningOperationStatus.class, null),
            Arguments.of("NOT_STARTED", LongRunningOperationStatus.class, LongRunningOperationStatus.NOT_STARTED),
            Arguments.of("FAILED", LongRunningOperationStatus.class, LongRunningOperationStatus.FAILED),
            Arguments.of("OTHER", LongRunningOperationStatus.class,
                LongRunningOperationStatus.fromString("OTHER", false)),

            // CharSequence
            Arguments.of(null, String.class, null),
            Arguments.of("", String.class, ""),
            Arguments.of("128", String.class, "128")
        );
    }

    @Test
    public void convertTextUnsuportedType() {
        assertThrows(IllegalStateException.class, () -> JacksonAdapter.convertText("a value", JacksonAdapter.class));
    }

    @Test
    public void convertTextInvalidEnumValue() {
        assertThrows(IllegalArgumentException.class, () -> JacksonAdapter.convertText("a value", HttpMethod.class));
    }
}
