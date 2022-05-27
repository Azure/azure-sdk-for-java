// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.Closeable;
import java.io.IOException;

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
     * Writes a {@link JsonSerializable} object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded.
     * <p>
     * This API is used instead of {@link #writeJsonField(String, JsonSerializable)} when the value needs to be
     * written to the root of the JSON value, as an element in an array, or after a call to
     * {@link #writeFieldName(String)}.
     *
     * @param value {@link JsonSerializable} object to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeJson(JsonSerializable<?> value) {
        return (value == null) ? writeNull() : value.toJson(this);
    }

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
     * <p>
     * For the nullable {@code Boolean} use {@link #writeBoolean(Boolean)}.
     *
     * @param value boolean value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeBoolean(boolean value);

    /**
     * Writes a nullable JSON boolean value ({@code true}, {@code false}, or {@code null}).
     * <p>
     * This API combines {@link #writeBoolean(boolean)} and {@link #writeNull()} based on whether {@code value} is null.
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded.
     * <p>
     * This API is used instead of {@link #writeBooleanField(String, Boolean)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the primitive {@code boolean} use {@link #writeBoolean(boolean)}.
     *
     * @param value Boolean value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeBoolean(Boolean value) {
        return (value == null) ? writeNull() : writeBoolean(value.booleanValue());
    }

    /**
     * Writes a JSON double value.
     * <p>
     * This API is used instead of {@link #writeDoubleField(String, double)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the nullable {@code Double} use {@link #writeDouble(Double)}.
     *
     * @param value double value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeDouble(double value);

    /**
     * Writes a nullable JSON double value.
     * <p>
     * This API combines {@link #writeDouble(double)} and {@link #writeNull()} based on whether {@code value} is null. A
     * value is always written no matter whether {@code value} is null, if null shouldn't be written this API call must
     * be null guarded.
     * <p>
     * This API is used instead of {@link #writeDoubleField(String, Double)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the primitive {@code double} use {@link #writeDouble(double)}.
     *
     * @param value Double value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeDouble(Double value) {
        return (value == null) ? writeNull() : writeDouble(value.doubleValue());
    }

    /**
     * Writes a JSON float value.
     * <p>
     * This API is used instead of {@link #writeFloatField(String, float)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the nullable {@code Float} use {@link #writeFloat(Float)}.
     *
     * @param value float value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeFloat(float value);

    /**
     * Writes a nullable JSON float value.
     * <p>
     * This API combines {@link #writeFloat(float)} and {@link #writeNull()} based on whether {@code value} is null. A
     * value is always written no matter whether {@code value} is null, if null shouldn't be written this API call must
     * be null guarded.
     * <p>
     * This API is used instead of {@link #writeFloatField(String, Float)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the primitive {@code float} use {@link #writeFloat(float)}.
     *
     * @param value Float value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeFloat(Float value) {
        return (value == null) ? writeNull() : writeFloat(value.floatValue());
    }

    /**
     * Writes a JSON int value.
     * <p>
     * This API is used instead of {@link #writeIntField(String, int)} when the value needs to be written to the root of
     * the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the nullable {@code Integer} use {@link #writeInteger(Integer)}.
     *
     * @param value int value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeInt(int value);

    /**
     * Writes a nullable JSON int value.
     * <p>
     * This API combines {@link #writeInt(int)} and {@link #writeNull()} based on whether {@code value} is null. A value
     * is always written no matter whether {@code value} is null, if null shouldn't be written this API call must be
     * null guarded.
     * <p>
     * This API is used instead of {@link #writeIntegerField(String, Integer)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the primitive {@code int} use {@link #writeInt(int)}.
     *
     * @param value Integer value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeInteger(Integer value) {
        return (value == null) ? writeNull() : writeInt(value);
    }

    /**
     * Writes a JSON long value.
     * <p>
     * This API is used instead of {@link #writeLongField(String, long)} when the value needs to be written to the root
     * of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the nullable {@code Long} use {@link #writeLong(Long)}.
     *
     * @param value long value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeLong(long value);

    /**
     * Writes a nullable JSON long value.
     * <p>
     * This API combines {@link #writeLong(long)} and {@link #writeNull()} based on whether {@code value} is null. A
     * value is always written no matter whether {@code value} is null, if null shouldn't be written this API call must
     * be null guarded.
     * <p>
     * This API is used instead of {@link #writeLongField(String, Long)} when the value needs to be written to the root
     * of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     * <p>
     * For the primitive {@code long} use {@link #writeLong(long)}.
     *
     * @param value Long value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeLong(Long value) {
        return (value == null) ? writeNull() : writeLong(value.longValue());
    }

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
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded.
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
     * Writes a {@link JsonSerializable} field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeJson(JsonSerializable)} to simplify adding a
     * key-value to a JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeJsonField(String, JsonSerializable, boolean)} to indicate whether
     * null should be written.
     *
     * @param fieldName The field name.
     * @param value {@link JsonSerializable} object to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeJsonField(String fieldName, JsonSerializable<?> value) {
        return writeJsonField(fieldName, value, true);
    }

    /**
     * Writes a {@link JsonSerializable} field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeJson(JsonSerializable)} to simplify adding a
     * key-value to a JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value {@link JsonSerializable} object to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeJsonField(String fieldName, JsonSerializable<?> value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : value.toJson(writeFieldName(fieldName));
    }

    /**
     * Writes a JSON binary field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBinary(byte[])} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeBinaryField(String, byte[], boolean)} to indicate whether null should
     * be written.
     *
     * @param fieldName The field name.
     * @param value Binary value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeBinaryField(String fieldName, byte[] value);

    /**
     * Writes a JSON binary field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBinary(byte[])} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Binary value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeBinaryField(String fieldName, byte[] value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeBinaryField(fieldName, value);
    }

    /**
     * Writes a JSON boolean field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBoolean(boolean)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value boolean value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeBooleanField(String fieldName, boolean value);

    /**
     * Writes a nullable JSON boolean field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBoolean(Boolean)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeBooleanField(String, Boolean, boolean)} to indicate whether null
     * should be written.
     *
     * @param fieldName The field name.
     * @param value Boolean value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeBooleanField(String fieldName, Boolean value) {
        return writeBooleanField(fieldName, value, true);
    }

    /**
     * Writes a nullable JSON boolean field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBoolean(Boolean)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Boolean value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeBooleanField(String fieldName, Boolean value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeBooleanField(fieldName, value.booleanValue());
    }

    /**
     * Writes a JSON double field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeDouble(double)} to simplify adding a key-value to a
     * JSON object.
     *
     * @param fieldName The field name.
     * @param value double value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeDoubleField(String fieldName, double value);

    /**
     * Writes a nullable JSON double field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeDouble(Double)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeDoubleField(String, Double, boolean)} to indicate whether null should
     * be written.
     *
     * @param fieldName The field name.
     * @param value Double value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeDoubleField(String fieldName, Double value) {
        return writeDoubleField(fieldName, value, true);
    }

    /**
     * Writes a nullable JSON double field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeDouble(Double)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Double value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeDoubleField(String fieldName, Double value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeDoubleField(fieldName, value.doubleValue());
    }

    /**
     * Writes a JSON float field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeFloat(float)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value float value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeFloatField(String fieldName, float value);

    /**
     * Writes a nullable JSON float field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeFloat(Float)} to simplify adding a key-value to a JSON
     * object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeFloatField(String, Float, boolean)} to indicate whether null should be
     * written.
     *
     * @param fieldName The field name.
     * @param value Float value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeFloatField(String fieldName, Float value) {
        return writeFloatField(fieldName, value, true);
    }

    /**
     * Writes a nullable JSON float field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeFloat(Float)} to simplify adding a key-value to a JSON
     * object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Float value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeFloatField(String fieldName, Float value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeFloatField(fieldName, value.floatValue());
    }

    /**
     * Writes a JSON int field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeInt(int)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value int value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeIntField(String fieldName, int value);

    /**
     * Writes a nullable JSON int field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeInteger(Integer)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeBooleanField(String, Boolean, boolean)} to indicate whether null
     * should be written.
     *
     * @param fieldName The field name.
     * @param value Integer value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeIntegerField(String fieldName, Integer value) {
        return writeIntegerField(fieldName, value, true);
    }

    /**
     * Writes a nullable JSON int field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeInteger(Integer)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Integer value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeIntegerField(String fieldName, Integer value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeIntField(fieldName, value);
    }

    /**
     * Writes a JSON long field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeLong(long)} to simplify adding a key-value to a JSON
     * object.
     *
     * @param fieldName The field name.
     * @param value long value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeLongField(String fieldName, long value);

    /**
     * Writes a nullable JSON long field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeLong(Long)} to simplify adding a key-value to a JSON
     * object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeBooleanField(String, Boolean, boolean)} to indicate whether null
     * should be written.
     *
     * @param fieldName The field name.
     * @param value Long value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeLongField(String fieldName, Long value) {
        return writeLongField(fieldName, value, true);
    }

    /**
     * Writes a nullable JSON long field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeLong(Long)} to simplify adding a key-value to a JSON
     * object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value Long value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeLongField(String fieldName, Long value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeLongField(fieldName, value.longValue());
    }

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
     * @param value String value to write.
     * @return The updated JsonWriter object.
     */
    public abstract JsonWriter writeStringField(String fieldName, String value);

    /**
     * Writes a JSON String field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeString(String)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * If {@code writeNull} is false and {@code value} is null neither a field name or a field value will be written,
     * effectively treating the call as a no-op.
     *
     * @param fieldName The field name.
     * @param value String value to write.
     * @param writeNull Whether a null field should be written.
     * @return The updated JsonWriter object, or if {@code writeNull} is false and {@code value} is null the unmodified
     * JsonWriter.
     */
    public final JsonWriter writeStringField(String fieldName, String value, boolean writeNull) {
        if (!writeNull && value == null) {
            return this;
        }

        return (value == null) ? writeNullField(fieldName) : writeStringField(fieldName, value);
    }

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
