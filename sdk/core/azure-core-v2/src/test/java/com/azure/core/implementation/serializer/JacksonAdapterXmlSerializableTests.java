// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import com.azure.xml.XmlSerializable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that {@link XmlSerializable} is supported by {@code JacksonAdapter} when {@code azure-xml} is included as a
 * dependency.
 */
public class JacksonAdapterXmlSerializableTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final SimpleXmlSerializable OBJECT = new SimpleXmlSerializable(true, 10, 10.0, "10");
    private static final String XML = "<?xml version='1.0' encoding='UTF-8'?>"
        + "<SimpleXml boolean=\"true\" decimal=\"10.0\"><int>10</int><string>10</string></SimpleXml>";

    @Test
    public void serializeToString() throws IOException {
        assertEquals(XML, ADAPTER.serialize(OBJECT, SerializerEncoding.XML));
    }

    @Test
    public void serializeToBytes() throws IOException {
        assertArrayEquals(XML.getBytes(StandardCharsets.UTF_8),
            ADAPTER.serializeToBytes(OBJECT, SerializerEncoding.XML));
    }

    @Test
    public void serializeToEmptyOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ADAPTER.serialize(OBJECT, SerializerEncoding.XML, baos);

        assertArrayEquals(XML.getBytes(StandardCharsets.UTF_8), baos.toByteArray());
    }

    @Test
    public void serializeToNonEmptyOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ADAPTER.serialize(OBJECT, SerializerEncoding.XML, baos);
        ADAPTER.serialize(OBJECT, SerializerEncoding.XML, baos);

        byte[] xmlBytes = XML.getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[xmlBytes.length * 2];
        System.arraycopy(xmlBytes, 0, expected, 0, xmlBytes.length);
        System.arraycopy(xmlBytes, 0, expected, xmlBytes.length, xmlBytes.length);

        assertArrayEquals(expected, baos.toByteArray());
    }

    @Test
    public void deserializeFromString() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(XML, SimpleXmlSerializable.class, SerializerEncoding.XML));
    }

    @Test
    public void deserializeFromBytes() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(XML.getBytes(StandardCharsets.UTF_8), SimpleXmlSerializable.class,
            SerializerEncoding.XML));
    }

    @Test
    public void deserializeFromInputStream() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)),
            SimpleXmlSerializable.class, SerializerEncoding.XML));
    }
}
