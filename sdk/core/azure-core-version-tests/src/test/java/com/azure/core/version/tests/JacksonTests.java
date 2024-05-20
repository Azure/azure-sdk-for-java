// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JacksonTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    @Test
    public void simpleToJson() throws IOException {
        SimpleJson simpleJson = new SimpleJson();
        simpleJson.anInt = 1;
        simpleJson.string = "hello";
        simpleJson.nullableDecimal = 12.5D;

        assertEquals("{\"int\":1,\"string\":\"hello\",\"nullableDecimal\":12.5}",
            ADAPTER.serialize(simpleJson, SerializerEncoding.JSON));

    }

    @Test
    public void simpleFromJson() throws IOException {
        SimpleJson simpleJson = ADAPTER.deserialize("{\"int\":1,\"string\":\"hello\",\"nullableDecimal\":12.5}",
            SimpleJson.class, SerializerEncoding.JSON);

        assertEquals(1, simpleJson.anInt);
        assertEquals("hello", simpleJson.string);
        assertEquals(12.5D, simpleJson.nullableDecimal);
    }

    @Test
    public void simpleToXml() throws IOException {
        SimpleXml simpleXml = new SimpleXml();
        simpleXml.anInt = 1;
        simpleXml.string = "hello";
        simpleXml.nullableDecimal = 12.5D;

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?><SimpleXml><int>1</int><string>hello</string><nullableDecimal>12.5</nullableDecimal></SimpleXml>",
            ADAPTER.serialize(simpleXml, SerializerEncoding.XML));
    }

    @Test
    public void simpleFromXml() throws IOException {
        SimpleXml simpleXml = ADAPTER.deserialize(
            "<?xml version='1.0' encoding='UTF-8'?><SimpleXml><int>1</int><string>hello</string><nullableDecimal>12.5</nullableDecimal></SimpleXml>",
            SimpleXml.class, SerializerEncoding.XML);

        assertEquals(1, simpleXml.anInt);
        assertEquals("hello", simpleXml.string);
        assertEquals(12.5D, simpleXml.nullableDecimal);
    }

    @Test
    public void fromXmlCoercesEmptyStringToNull() throws IOException {
        SimpleXml simpleXml = ADAPTER.deserialize(
            "<?xml version='1.0' encoding='UTF-8'?><SimpleXml><int>1</int><string>hello</string><nullableDecimal></nullableDecimal></SimpleXml>",
            SimpleXml.class, SerializerEncoding.XML);

        assertEquals(1, simpleXml.anInt);
        assertEquals("hello", simpleXml.string);
        assertNull(simpleXml.nullableDecimal);
    }

    public static final class SimpleJson {
        @JsonProperty("int")
        private int anInt;

        @JsonProperty("string")
        private String string;

        @JsonProperty("nullableDecimal")
        private Double nullableDecimal;
    }

    @JacksonXmlRootElement(localName = "SimpleXml")
    public static final class SimpleXml {
        @JsonProperty("int")
        private int anInt;

        @JsonProperty("string")
        private String string;

        @JsonProperty("nullableDecimal")
        private Double nullableDecimal;
    }
}
