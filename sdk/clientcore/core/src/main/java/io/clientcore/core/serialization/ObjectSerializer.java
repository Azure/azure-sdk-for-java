// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public interface ObjectSerializer {
    /**
     * Reads a byte array into its object representation.
     *
     * @param data The byte array.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     * @throws IOException If the deserialization fails.
     */
    <T> T deserializeFromBytes(byte[] data, Type type) throws IOException;

    /**
     * Reads a stream into its object representation.
     *
     * @param stream {@link InputStream} of data.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     * @throws IOException If the deserialization fails.
     */
    <T> T deserializeFromStream(InputStream stream, Type type) throws IOException;

    /**
     * Reads a channel into its object representation.
     *
     * @param channel {@link ReadableByteChannel} of data.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized channel.
     * @throws IOException If the deserialization fails.
     */
    default <T> T deserializeFromChannel(ReadableByteChannel channel, Type type) throws IOException {
        return deserializeFromStream(Channels.newInputStream(channel), type);
    }

    /**
     * Serializes an object into a byte array.
     *
     * @param value The object to serialize.
     * @return The binary representation of the serialized object.
     * @throws IOException If the serialization fails.
     */
    byte[] serializeToBytes(Object value) throws IOException;

    /**
     * Serializes and writes an object into a provided stream.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    void serializeToStream(OutputStream stream, Object value) throws IOException;

    /**
     * Serializes and writes an object into a provided channel.
     *
     * @param channel {@link WritableByteChannel} where the serialized object will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    default void serializeToChannel(WritableByteChannel channel, Object value) throws IOException {
        serializeToStream(Channels.newOutputStream(channel), value);
    }

    /**
     * Indicates whether the given implementation of {@link ObjectSerializer} supports the provided format.
     * <p>
     * An implementation of {@link ObjectSerializer} may support multiple formats, such as JSON and XML.
     * <p>
     * A check for support should be made before attempting to serialize or deserialize an object.
     *
     * @param format The format to check support for.
     * @return Whether the format is supported.
     */
    boolean supportsFormat(SerializationFormat format);
}
