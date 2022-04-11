// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.Closeable;

/**
 * Writes a JSON encoded value to a stream.
 */
public interface JsonWriter extends Closeable {
    /**
     * Flushes any un-flushed content written to this writer.
     *
     * @return The flushed JsonWriter object.
     */
    JsonWriter flush();

    /**
     * Writes a JSON start object, '{'.
     *
     * @return The updated JsonWriter object.
     */
    JsonWriter writeStartObject();

    /**
     * Writes a JSON end object, '}'.
     *
     * @return The updated JsonWriter object.
     */
    JsonWriter writeEndObject();

    /**
     * Writes a JSON start array, '['.
     *
     * @return The updated JsonWriter object.
     */
    JsonWriter writeStartArray();

    /**
     * Writes a JSON end array, ']'.
     *
     * @return The updated JsonWriter object.
     */
    JsonWriter writeEndArray();

    /**
     * Writes a JSON field name.
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeFieldName(String fieldName);

    /**
     * Writes a JSON binary value.
     * <p>
     * This API is used instead of {@link #writeBinaryField(String, byte[])} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value Binary value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeBinary(byte[] value);

    /**
     * Writes a JSON boolean value.
     * <p>
     * This API is used instead of {@link #writeBooleanField(String, boolean)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value boolean value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeBoolean(boolean value);

    /**
     * Writes a JSON double value.
     * <p>
     * This API is used instead of {@link #writeDoubleField(String, double)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value double value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeDouble(double value);

    /**
     * Writes a JSON float value.
     * <p>
     * This API is used instead of {@link #writeFloatField(String, float)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value float value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeFloat(float value);

    /**
     * Writes a JSON int value.
     * <p>
     * This API is used instead of {@link #writeIntField(String, int)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value int value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeInt(int value);

    /**
     * Writes a JSON long value.
     * <p>
     * This API is used instead of {@link #writeLongField(String, long)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value long value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeLong(long value);

    /**
     * Writes a JSON null.
     * <p>
     * This API is used instead of {@link #writeNullField(String)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @return The updated JsonWriter object.
     */
    JsonWriter writeNull();

    /**
     * Writes a JSON String value.
     * <p>
     * This API is used instead of {@link #writeStringField(String, String)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value String value to write.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeString(String value);

    /**
     * Writes a JSON binary field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBinary(byte[])} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The binary value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeBinaryField(String fieldName, byte[] value);

    /**
     * Writes a JSON boolean field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBoolean(boolean)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The boolean value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeBooleanField(String fieldName, boolean value);

    /**
     * Writes a JSON double field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeDouble(double)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The double value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeDoubleField(String fieldName, double value);

    /**
     * Writes a JSON float field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeFloat(float)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The float value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeFloatField(String fieldName, float value);

    /**
     * Writes a JSON int field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeInt(int)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The int value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeIntField(String fieldName, int value);

    /**
     * Writes a JSON long field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeLong(long)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The binary value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeLongField(String fieldName, long value);

    /**
     * Writes a JSON null field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeNull()} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeNullField(String fieldName);

    /**
     * Writes a JSON String field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeString(String)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The String value.
     * @return The updated JsonWriter object.
     */
    JsonWriter writeStringField(String fieldName, String value);
}
