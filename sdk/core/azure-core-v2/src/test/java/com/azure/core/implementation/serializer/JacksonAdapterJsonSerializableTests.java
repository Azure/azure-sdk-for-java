// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonAdapterJsonSerializableTests {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final SimpleJsonSerializable OBJECT = new SimpleJsonSerializable(true, 10, 10.0, "10");
    private static final String JSON = "{\"boolean\":true,\"int\":10,\"decimal\":10.0,\"string\":\"10\"}";

    @Test
    public void serializeToString() throws IOException {
        assertEquals(JSON, ADAPTER.serialize(OBJECT, SerializerEncoding.JSON));
    }

    @Test
    public void serializeToBytes() throws IOException {
        assertArrayEquals(JSON.getBytes(StandardCharsets.UTF_8),
            ADAPTER.serializeToBytes(OBJECT, SerializerEncoding.JSON));
    }

    @Test
    public void serializeToEmptyOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ADAPTER.serialize(OBJECT, SerializerEncoding.JSON, baos);

        assertArrayEquals(JSON.getBytes(StandardCharsets.UTF_8), baos.toByteArray());
    }

    @Test
    public void serializeToNonEmptyOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ADAPTER.serialize(OBJECT, SerializerEncoding.JSON, baos);
        ADAPTER.serialize(OBJECT, SerializerEncoding.JSON, baos);

        byte[] jsonBytes = JSON.getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[jsonBytes.length * 2];
        System.arraycopy(jsonBytes, 0, expected, 0, jsonBytes.length);
        System.arraycopy(jsonBytes, 0, expected, jsonBytes.length, jsonBytes.length);

        assertArrayEquals(expected, baos.toByteArray());
    }

    @Test
    public void serializeRaw() {
        assertEquals(JSON, ADAPTER.serializeRaw(OBJECT));
    }

    @Test
    public void deserializeFromString() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(JSON, SimpleJsonSerializable.class, SerializerEncoding.JSON));
    }

    @Test
    public void deserializeFromBytes() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(JSON.getBytes(StandardCharsets.UTF_8), SimpleJsonSerializable.class,
            SerializerEncoding.JSON));
    }

    @Test
    public void deserializeFromInputStream() throws IOException {
        assertEquals(OBJECT, ADAPTER.deserialize(new ByteArrayInputStream(JSON.getBytes(StandardCharsets.UTF_8)),
            SimpleJsonSerializable.class, SerializerEncoding.JSON));
    }
}
