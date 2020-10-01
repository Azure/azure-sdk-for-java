package com.azure.core.experimental.util;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Binary representation of data.
 */
public class BinaryData {
    final private byte[] data;

    /**
     * Create {@link BinaryData} instance with given value.
     *
     * @param data to use.
     */
    public BinaryData(String data) {
        this(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     */
    public BinaryData(byte[] data) {
        this.data = data;
        //this.serializer = null;
    }

    /**
     * Create {@link BinaryData} instance with given object and {@link JsonSerializer}.
     *
     * @param data to use.
     */
    public static BinaryData getBinaryData(Object data, ObjectSerializer serializer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, data);
        return new BinaryData(outputStream.toByteArray());

    }

    /**
     * Gets the binary data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public byte[] getData() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    /**
     * Gets the binary data.
     *
     * @return byte array representing {@link BinaryData}.
     */
    public String getDataAsString() {
        return new String(this.data);
    }

    /**
     * Apply the {@link ObjectSerializer} on the bytes representation of the data.
     *
     * @param clazz representing the type of the Object.
     * @param serializer to use deserialize data into type.
     * @return The type
     */
    public <T> T getDataAsObject(Class<T> clazz, ObjectSerializer serializer) {
        TypeReference<T>  ref = TypeReference.createInstance(clazz);
        InputStream jsonStream = new ByteArrayInputStream(this.data);
        return serializer.deserialize(jsonStream, ref);
    }
}
