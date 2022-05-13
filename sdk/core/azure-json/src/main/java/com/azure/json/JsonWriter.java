// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

/**
 * Writes a JSON encoded value to a stream.
 */
@SuppressWarnings("resource")
public abstract class JsonWriter implements Closeable {
    /**
     * Gets the current {@link JsonWriteContext writing context} for the JSON object.
     * <p>
     * The writing context can help determine whether a write operation would be illegal.
     * <p>
     * The initial write context is {@link JsonWriteContext#ROOT}.
     *
     * @return The current writing context.
     */
    public abstract JsonWriteContext getWriteContext();

    /**
     * Closes the JSON stream.
     * <p>
     * If the {@link #getWriteContext() writing context} isn't {@link JsonWriteContext#COMPLETED} when this is called an
     * {@link IllegalStateException} will be thrown.
     *
     * @throws IllegalStateException If the {@link JsonWriter} is closed before the
     * {@link #getWriteContext() writing context} is {@link JsonWriteContext#COMPLETED}.
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Flushes any un-flushed content written to this writer.
     * <p>
     * It should be assumed that each write call won't flush any contents.
     *
     * @return The flushed JsonWriter object.
     */
    public abstract JsonWriter flush();

    /**
     * Writes a {@link JsonCapable} object.
     * <p>
     * This API is used instead of {@link #writeJsonCapableField(String, JsonCapable)} when the value needs to be
     * written to the root of the JSON value, as an element in an array, or after a call to
     * {@link #writeFieldName(String)}.
     *
     * @param jsonCapable The {@link JsonCapable} object.
     * @return The updated JsonWriter object.
     * @throws NullPointerException If {@code jsonCapable} is null.
     */
    public final JsonWriter writeJsonCapable(JsonCapable<?> jsonCapable) {
        Objects.requireNonNull(jsonCapable, "'jsonCapable' cannot be null.");

        return jsonCapable.toJson(this);
    }

    /**
     * Writes a JSON start object ({@code &#123;}).
     *
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeStartObject();

    /**
     * Writes a JSON start object ({@code &#123;}) with a preceding field name.
     * <p>
     * This API is the equivalent of calling {@link #writeFieldName(String)} and {@link #writeStartObject()}, in that
     * order.
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeStartObject(String fieldName) {
        return writeFieldName(fieldName).writeStartObject();
    }

    /**
     * Writes a JSON end object ({@code &#125;}).
     * <p>
     * If the current writing context isn't an object an {@link IllegalStateException} will be thrown.
     *
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeEndObject();

    /**
     * Writes a JSON start array ({@code [}).
     *
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeStartArray();

    /**
     * Writes a JSON start array ({@code [}) with a preceding field name.
     * <p>
     * This API is the equivalent of calling {@link #writeFieldName(String)} and {@link #writeStartArray()}, in that
     * order.
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeStartArray(String fieldName) {
        return writeFieldName(fieldName).writeStartArray();
    }

    /**
     * Writes a JSON end array ({@code ]}).
     *
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeEndArray();

    /**
     * Writes a JSON field name ({@code "fieldName":}).
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeFieldName(String fieldName);

    /**
     * Writes a JSON binary value.
     * <p>
     * This API converts the binary value to a Base64 encoded string.
     * <p>
     * If {@code value} is null this API will be the equivalent to {@link #writeNull()}.
     * <p>
     * This API is used instead of {@link #writeBinaryField(String, byte[])} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value Binary value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeBinary(byte[] value);

    /**
     * Writes a JSON boolean value ({@code true} or {@code false}).
     * <p>
     * This API is used instead of {@link #writeBooleanField(String, boolean)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value boolean value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeBoolean(boolean value);

    /**
     * Writes a JSON double value.
     * <p>
     * This API is used instead of {@link #writeDoubleField(String, double)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value double value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeDouble(double value);

    /**
     * Writes a JSON float value.
     * <p>
     * This API is used instead of {@link #writeFloatField(String, float)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value float value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeFloat(float value);

    /**
     * Writes a JSON int value.
     * <p>
     * This API is used instead of {@link #writeIntField(String, int)} when the value needs to be written to the root of
     * the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value int value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeInt(int value);

    /**
     * Writes a JSON long value.
     * <p>
     * This API is used instead of {@link #writeLongField(String, long)} when the value needs to be written to the root
     * of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value long value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeLong(long value);

    /**
     * Writes a JSON null.
     * <p>
     * This API is used instead of {@link #writeNullField(String)} when the value needs to be written to the root of the
     * JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeNull();

    /**
     * Writes a JSON String value.
     * <p>
     * If the {@code value} is null, this API will be equivalent to calling {@link #writeNull()}.
     * <p>
     * This API is used instead of {@link #writeStringField(String, String)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value String value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeString(String value);

    /**
     * Writes the passed value literally without any additional handling.
     * <p>
     * Use this API when writing a String value that is already properly formatted JSON, such as a JSON string
     * ({@code "\"string\""}), number ({@code "42"}, {@code "42.0"}), boolean ({@code "true"}, {@code "false"}), null
     * ({@code "null"}), array ({@code "[\"string\", \"array\"]"}), or object ({@code {"\"field\":\"value\""}}).
     * <p>
     * This API is used instead of {@link #writeRawField(String, String)} when the value needs to be written to the root
     * of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value The raw JSON value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeRawValue(String value);

    /**
     * Writes a {@link JsonCapable} object.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeJsonCapable(JsonCapable)} to simplify adding a
     * key-value to a JSON object.
     *
     * @param fieldName The field name.
     * @param jsonCapable The {@link JsonCapable} object.
     * @return The updated JsonWriter object.
     * @throws NullPointerException If {@code jsonCapable} is null.
     */
    public final JsonWriter writeJsonCapableField(String fieldName, JsonCapable<?> jsonCapable) {
        Objects.requireNonNull(jsonCapable, "'jsonCapable' cannot be null.");

        return jsonCapable.toJson(writeFieldName(fieldName));
    }

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
    public abstract JsonWriter writeBinaryField(String fieldName, byte[] value);

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
    public abstract JsonWriter writeBooleanField(String fieldName, boolean value);

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
    public abstract JsonWriter writeDoubleField(String fieldName, double value);

    /**
     * Writes a JSON float field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeFloat(float)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value The float value.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeFloatField(String fieldName, float value);

    /**
     * Writes a JSON int field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeInt(int)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value The int value.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeIntField(String fieldName, int value);

    /**
     * Writes a JSON long field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeLong(long)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value The binary value.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeLongField(String fieldName, long value);

    /**
     * Writes a JSON null field ({@code "fieldName":null}).
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeNull()} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeNullField(String fieldName);

    /**
     * Writes a JSON String field.
     * <p>
     * If the {@code value} is null, this API will be equivalent to calling {@link #writeNullField(String)}.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeString(String)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The String value.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeStringField(String fieldName, String value);

    /**
     * Writes the passed field literally without any additional handling.
     * <p>
     * Use this API when writing a String value that is already properly formatted JSON, such as a JSON string
     * ({@code "\"string\""}), number ({@code "42"}, {@code "42.0"}), boolean ({@code "true"}, {@code "false"}), null
     * ({@code "null"}), array ({@code "[\"string\", \"array\"]"}), or object ({@code {"\"field\":\"value\""}}).
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeRawValue(String)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value The raw JSON value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeRawField(String fieldName, String value);
}
