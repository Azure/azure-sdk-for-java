// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Interface for JsonReaders.
 */
interface IJsonReader {

    /**
     * Gets the SerializationFormat of the JsonWriter.
     *
     * @return The SerializationFormat of the JsonWriter.
     */
    JsonSerializationFormat getSerializationFormat();

    /**
     * Gets the current level of nesting of the JSON that the JsonReader is reading.
     *
     * @return The current level of nesting of the JSON.
     */
    int getCurrentDepth();

    /**
     * Gets the {@link JsonTokenType} of the current token that the JsonReader is about to read.
     *
     * @return The JsonTokenType of the current token.
     */
    JsonTokenType getCurrentTokenType();

    /**
     * Advances the JsonReader by one token.
     *
     * @return {@code true} if the JsonReader successfully advanced to the next token; {@code false} if the JsonReader has passed the end of the JSON.
     */
    boolean read();

    /**
     * Gets the next JSON token from the JsonReader as a UTF-16 string.
     *
     * @return The next JSON token from the JsonReader as a string.
     */
    String getStringValue();

    /**
     * Tries to get the buffered UTF-8 string value.
     *
     * @param value The buffered UTF-8 string value if found.
     * @return {@code true} if the buffered UTF-8 string value was retrieved; {@code false} otherwise.
     */
    boolean tryGetBufferedStringValue(ByteBuffer value);

    /**
     * Gets the next JSON token from the JsonReader as a 1 byte signed integer.
     *
     * @return The next JSON token from the JsonReader as a 1 byte signed integer.
     */
    byte getInt8Value();

    /**
     * Gets the next JSON token from the JsonReader as a 2 byte signed integer.
     *
     * @return The next JSON token from the JsonReader as a 2 byte signed integer.
     */
    short getInt16Value();

    /**
     * Gets the next JSON token from the JsonReader as a 4 byte signed integer.
     *
     * @return The next JSON token from the JsonReader as a 4 byte signed integer.
     */
    int getInt32Value();

    /**
     * Gets the next JSON token from the JsonReader as an 8 byte signed integer.
     *
     * @return The next JSON token from the JsonReader as an 8 byte signed integer.
     */
    long getInt64Value();

    /**
     * Gets the next JSON token from the JsonReader as a single precision floating point.
     *
     * @return The next JSON token from the JsonReader as a single precision floating point.
     */
    float getFloat32Value();

    /**
     * Gets the next JSON token from the JsonReader as a double precision floating point.
     *
     * @return The next JSON token from the JsonReader as a double precision floating point.
     */
    double getFloat64Value();

    /**
     * Gets the next JSON token from the JsonReader as a UUID.
     *
     * @return The next JSON token from the JsonReader as a UUID.
     */
    UUID getUuidValue();

    /**
     * Gets the next JSON token from the JsonReader as a binary list.
     *
     * @return The next JSON token from the JsonReader as a binary list.
     */
    ByteBuffer getBinaryValue(); // todo - Is bytebuffer a suitable alternative for C# ReadOnlyMemory<Byte>?

    /**
     * Writes the current token on the reader to the writer.
     *
     * @param writer The writer to write to.
     */
    void writeCurrentToken(IJsonWriter writer);

    /**
     * Writes all the tokens in the reader to the writer.
     *
     * @param writer The writer to write to.
     */
    void writeAll(IJsonWriter writer);
}
