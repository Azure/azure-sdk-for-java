package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;
import java.util.UUID;

/// <summary>
/// Interface for all JsonWriters that know how to write json of a specific serialization format.
/// </summary>
public interface IJsonWriter
{
    /// <summary>
    /// Gets the SerializationFormat of the JsonWriter.
    /// </summary>
    JsonSerializationFormat getSerializationFormat();

    /// <summary>
    /// Gets the current length of the internal buffer.
    /// </summary>
    long getCurrentLength();

    /// <summary>
    /// Writes the object start symbol to internal buffer.
    /// </summary>
    void writeObjectStart();

    /// <summary>
    /// Writes the object end symbol to the internal buffer.
    /// </summary>
    void writeObjectEnd();

    /// <summary>
    /// Writes the array start symbol to the internal buffer.
    /// </summary>
    void writeArrayStart();

    /// <summary>
    /// Writes the array end symbol to the internal buffer.
    /// </summary>
    void writeArrayEnd();

    /// <summary>
    /// Writes a field name to the internal buffer.
    /// </summary>
    /// <param name="fieldName">The name of the field to write.</param>
    void writeFieldName(String fieldName);

    /// <summary>
    /// Writes a UTF-8 field name to the internal buffer.
    /// </summary>
    /// <param name="fieldName"></param>
    //void WriteFieldName(Utf8Span fieldName);

    /// <summary>
    /// Writes a string to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the string to write.</param>
    void writeStringValue(String value);

    /// <summary>
    /// Writes a UTF-8 string value to the internal buffer.
    /// </summary>
    /// <param name="value"></param>
    //void WriteStringValue(Utf8Span value);

    /// <summary>
    /// Writes a number to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the number to write.</param>
    void writeNumber64Value(long value);

    /// <summary>
    /// Writes a boolean to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the boolean to write.</param>
    void writeBoolValue(boolean value);

    /// <summary>
    /// Writes a null to the internal buffer.
    /// </summary>
    void writeNullValue();

    /// <summary>
    /// Writes a single signed byte integer to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void WriteInt8Value(byte value);

    /// <summary>
    /// Writes a signed 2-byte integer to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeInt16Value(short value);

    /// <summary>
    /// Writes an signed 4-byte integer to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeInt32Value(int value);

    /// <summary>
    /// Writes a signed 8-byte integer to the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeInt64Value(long value);

    /// <summary>
    /// Writes a single precision floating point number into the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeFloat32Value(float value);

    /// <summary>
    /// Writes a double precision floating point number into the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeFloat64Value(double value);

    /// <summary>
    /// Writes a 4 byte unsigned integer into the internal buffer.
    /// </summary>
    /// <param name="value">The value of the integer to write.</param>
    void writeUInt32Value(long value);

    /// <summary>
    /// Writes a Guid value into the internal buffer.
    /// </summary>
    /// <param name="value">The value of the guid to write.</param>
    void writeGuidValue(UUID value);

    /// <summary>
    /// Writes a Binary value into the internal buffer.
    /// </summary>
    /// <param name="value">The value of the bytes to write.</param>
    void writeBinaryValue(ByteBuffer value);

    /// <summary>
    /// Gets the result of the JsonWriter.
    /// </summary>
    /// <returns>The result of the JsonWriter as an array of bytes.</returns>
    ByteBuffer getResult();
}
