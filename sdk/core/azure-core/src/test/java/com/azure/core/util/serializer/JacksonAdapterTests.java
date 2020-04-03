// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @ParameterizedTest
    @MethodSource("xmlBomRemovalSupplier")
    public void xmlBomRemoval(String xml, String expected) throws IOException {
        XmlString actual = new JacksonAdapter().deserialize(xml, XmlString.class, SerializerEncoding.XML);

        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual.getValue());
        }
    }

    private static Stream<Arguments> xmlBomRemovalSupplier() {
        byte[] utf8BomBytes = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        String utf8Bom = new String(utf8BomBytes, StandardCharsets.UTF_8);
        String cp1252Utf8Bom = new String(utf8BomBytes);

        String cleanXml = "﻿<?xml version=\"1.0\" encoding=\"utf-8\"?><XmlString><Value>This is clean xml.</Value></XmlString>";
        String dirtyXml = "﻿<?xml version=\"1.0\" encoding=\"utf-8\"?><XmlString><Value>Clean this xml.</Value></XmlString>";

        return Stream.of(
            // Null value returns null.
            Arguments.of(null, null),

            // Empty value returns null.
            Arguments.of("", null),

            // Value that is only a UTF-8 BOM returns null.
            Arguments.of(utf8Bom, null),
            Arguments.of(cp1252Utf8Bom, null),

            // Value without a leading BOM isn't mutated.
            Arguments.of(cleanXml, "This is clean xml."),

            // Value with a leading UTF-8 BOM scrubs the BOM.
            Arguments.of(utf8Bom.concat(dirtyXml), "Clean this xml."),
            Arguments.of(cp1252Utf8Bom.concat(dirtyXml), "Clean this xml.")
        );
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
}
