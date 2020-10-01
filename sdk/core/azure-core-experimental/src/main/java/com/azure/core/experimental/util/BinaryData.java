package com.azure.core.experimental.util;

import com.azure.core.util.serializer.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Binary representation of body.
 */
public class BinaryData {

    final private byte[] data;

    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param data to use.
     */
    public BinaryData(String data) {
        this.data = data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     */
    public BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     * Create {@link BinaryData} instance with given object and {@link JsonSerializer}.
     *
     * @param data to use.
     */
    public BinaryData(Object data, JsonSerializer serializer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        this.data = outputStream.toByteArray();
    }

    /**
     * Gets the data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public byte[] getData() {
        return this.data;
    }
}
