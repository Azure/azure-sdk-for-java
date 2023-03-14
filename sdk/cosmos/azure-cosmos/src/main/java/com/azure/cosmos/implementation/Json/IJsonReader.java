package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface IJsonReader
{
    /// <summary>
    /// Gets the <see ref="JsonSerializationFormat"/> for the JsonReader
    /// </summary>
    JsonSerializationFormat getSerializationFormat();

    /// <summary>
    /// Gets the current level of nesting of the JSON that the JsonReader is reading.
    /// </summary>
    int getCurrentDepth();

    /// <summary>
    /// Gets the <see ref="JsonTokenType"/> of the current token that the JsonReader is about to read.
    /// </summary>
    JsonTokenType getCurrentTokenType();

    /// <summary>
    /// Advances the JsonReader by one token.
    /// </summary>
    /// <returns><code>true</code> if the JsonReader successfully advanced to the next token; <code>false</code> if the JsonReader has passed the end of the JSON.</returns>
    boolean read();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a double.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a double.</returns>
    long getNumberValue();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a UTF-16 string.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a string.</returns>
    String getStringValue();

    /// <summary>
    /// Tries to get the buffered UTF-8 string value.
    /// </summary>
    /// <param name="value">The buffered UTF-8 string value if found.</param>
    /// <returns>true if the buffered UTF-8 string value was retrieved; false otherwise.</returns>
    boolean tryGetBufferedStringValue(Utf8Memory value);

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a 1 byte signed integer.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a 1 byte signed integer.</returns>
    byte getInt8Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a 2 byte signed integer.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a 2 byte signed integer.</returns>
    short getInt16Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a 4 byte signed integer.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a 4 byte signed integer.</returns>
    int getInt32Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a 8 byte signed integer.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a 8 byte signed integer.</returns>
    long getInt64Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a 4 byte unsigned integer.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a 4 byte unsigned integer.</returns>
    long getUInt32Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a single precision floating point.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a single precision floating point.</returns>
    float getFloat32Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a double precision floating point.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a double precision floating point.</returns>
    double getFloat64Value();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a GUID.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a GUID.</returns>
    UUID getGuidValue();

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a binary list.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a binary list.</returns>
    ByteBuffer getBinaryValue();

    /// <summary>
    /// Writes the current token on the reader to the writer.
    /// </summary>
    /// <param name="writer">The writer to write to.</param>
    void writeCurrentToken(IJsonWriter writer);

    /// <summary>
    /// Writes all the tokens in the reader to the writer.
    /// </summary>
    /// <param name="writer"></param>
    void writeAll(IJsonWriter writer);

}
