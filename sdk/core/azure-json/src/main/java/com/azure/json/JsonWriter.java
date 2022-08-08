// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

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
     * Writes a nullable value if, and only if, it isn't null.
     *
     * @param nullable The nullable value.
     * @param writerFunc Function that writes the value.
     * @param <T> Type of the nullable value.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeNonNull(T nullable, BiConsumer<JsonWriter, T> writerFunc) {
        if (nullable == null) {
            return this;
        }

        writerFunc.accept(this, nullable);
        return this;
    }

    /**
     * Writes a {@link JsonSerializable} object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate that null shouldn't be
     * written.
     * <p>
     * This API is used instead of {@link #writeJsonField(String, JsonSerializable)} when the value needs to be written
     * to the root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value {@link JsonSerializable} object to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeJson(JsonSerializable<?> value) {
        return (value == null) ? writeNull() : value.toJson(this);
    }

    /**
     * Writes a JSON array.
     * <p>
     * This API will begin by writing the start array ({@code [}) followed by all elements in the array using the
     * {@code elementWriterFunc} and finishing by writing the end array ({@code ]}).
     * <p>
     * If the passed {@code array} is null JSON null will be written. If null shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * This API is used instead of {@link #writeArrayField(String, Object[], BiConsumer)} when the value needs to be
     * written to the root of the JSON value, as an element in an array, or after a call to
     * {@link #writeFieldName(String)}.
     *
     * @param array The array being written.
     * @param elementWriterFunc The function that writes each element of the array.
     * @param <T> The array element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeArray(T[] array, BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNull();
        }

        writeStartArray();

        for (T element : array) {
            elementWriterFunc.accept(this, element);
        }

        return writeEndArray();
    }

    /**
     * Writes a JSON array.
     * <p>
     * This API will begin by writing the start array ({@code [}) followed by all elements in the array using the
     * {@code elementWriterFunc} and finishing by writing the end array ({@code ]}).
     * <p>
     * If the passed {@code array} is null JSON null will be written. If null shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * This API is used instead of {@link #writeArrayField(String, Iterable, BiConsumer)} when the value needs to be
     * written to the root of the JSON value, as an element in an array, or after a call to
     * {@link #writeFieldName(String)}.
     *
     * @param array The array being written.
     * @param elementWriterFunc The function that writes each element of the array.
     * @param <T> The array element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeArray(Iterable<T> array, BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNull();
        }

        writeStartArray();

        for (T element : array) {
            elementWriterFunc.accept(this, element);
        }

        return writeEndArray();
    }

    /**
     * Writes a JSON map.
     * <p>
     * This API will begin by writing the start object ({@code &#123;}) followed by key-value fields in the map using
     * the {@code valueWriterFunc} and finishing by writing the end object ({@code &#125;}).
     * <p>
     * If the passed {@code map} is null JSON null will be written. If null shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * This API is used instead of {@link #writeMapField(String, Map, BiConsumer)} when the value needs to be written to
     * the root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param map The map being written.
     * @param valueWriterFunc The function that writes value of each key-value pair in the map.
     * @param <T> The value element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeMap(Map<String, T> map, BiConsumer<JsonWriter, T> valueWriterFunc) {
        if (map == null) {
            return writeNull();
        }

        writeStartObject();

        for (Map.Entry<String, T> entry : map.entrySet()) {
            writeFieldName(entry.getKey());
            valueWriterFunc.accept(this, entry.getValue());
        }

        return writeEndObject();
    }

    /**
     * Writes a JSON binary value.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
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
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
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
     * For the nullable {@code Double} use {@link #writeNumber(Number)}.
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
     * <p>
     * For the nullable {@code Float} use {@link #writeNumber(Number)}.
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
     * <p>
     * For the nullable {@code Integer} use {@link #writeNumber(Number)}.
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
     * <p>
     * For the nullable {@code Long} use {@link #writeNumber(Number)}.
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
     * Writes a nullable JSON number value.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
     * <p>
     * This API is used instead of {@link #writeNumberField(String, Number)} when the value needs to be written to the
     * root of the JSON value, as an element in an array, or after a call to {@link #writeFieldName(String)}.
     *
     * @param value Number value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeNumber(Number value) {
        if (value == null) {
            return writeNull();
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return writeInt(value.intValue());
        } else if (value instanceof Long) {
            return writeLong(value.longValue());
        } else if (value instanceof Float) {
            return writeFloat(value.floatValue());
        } else if (value instanceof Double) {
            return writeDouble(value.doubleValue());
        } else {
            return writeRawValue(value.toString());
        }
    }

    /**
     * Writes a JSON String value.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
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
     * Combines {@link #writeFieldName(String)} and {@link #writeJson(JsonSerializable)} to simplify adding a key-value
     * to a JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
     *
     * @param fieldName The field name.
     * @param value {@link JsonSerializable} object to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeJsonField(String fieldName, JsonSerializable<?> value) {
        return (value == null) ? writeNullField(fieldName) : value.toJson(writeFieldName(fieldName));
    }

    /**
     * Writes a JSON array field.
     * <p>
     * This API will begin by writing the field name and start array ({@code [}) followed by all elements in the array
     * using the {@code elementWriterFunc} and finishing by writing the end array ({@code ]}).
     * <p>
     * If the passed {@code array} is null a JSON null field will be written. If a null field shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeArray(Object[], BiConsumer)} to simplify adding a
     * key-value to a JSON object.
     *
     * @param fieldName The field name.
     * @param array The array being written.
     * @param elementWriterFunc The function that writes each element of the array.
     * @param <T> The array element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeArrayField(String fieldName, T[] array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNullField(fieldName);
        }

        writeStartArray(fieldName);

        for (T element : array) {
            elementWriterFunc.accept(this, element);
        }

        return writeEndArray();
    }

    /**
     * Writes a JSON array field.
     * <p>
     * This API will begin by writing the field name and start array ({@code [}) followed by all elements in the array
     * using the {@code elementWriterFunc} and finishing by writing the end array ({@code ]}).
     * <p>
     * If the passed {@code array} is null a JSON null field will be written. If a null field shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeArray(Iterable, BiConsumer)} to simplify adding a
     * key-value to a JSON object.
     *
     * @param fieldName The field name.
     * @param array The array being written.
     * @param elementWriterFunc The function that writes each element of the array.
     * @param <T> The array element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeArrayField(String fieldName, Iterable<T> array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNullField(fieldName);
        }

        writeStartArray(fieldName);

        for (T element : array) {
            elementWriterFunc.accept(this, element);
        }

        return writeEndArray();
    }

    /**
     * Writes a JSON map field.
     * <p>
     * This API will begin by writing the field name and start object ({@code &#123;}) followed by key-value fields in
     * the map using the {@code valueWriterFunc} and finishing by writing the end object ({@code &#125;}).
     * <p>
     * If the passed {@code map} is null a JSON null field will be written. If a null field shouldn't be written use
     * {@link #writeNonNull(Object, BiConsumer)}.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeMap(Map, BiConsumer)} to simplify adding a key-value to
     * a JSON object.
     *
     * @param fieldName The field name.
     * @param map The map being written.
     * @param valueWriterFunc The function that writes each value of the map.
     * @param <T> The value element type.
     * @return The updated JsonWriter object.
     */
    public final <T> JsonWriter writeMapField(String fieldName, Map<String, T> map,
        BiConsumer<JsonWriter, T> valueWriterFunc) {
        if (map == null) {
            return writeNullField(fieldName);
        }

        writeStartObject(fieldName);

        for (Map.Entry<String, T> entry : map.entrySet()) {
            writeFieldName(entry.getKey());
            valueWriterFunc.accept(this, entry.getValue());
        }

        return writeEndObject();
    }

    /**
     * Writes a JSON binary field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeBinary(byte[])} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
     *
     * @param fieldName The field name.
     * @param value Binary value to write.
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
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be written.
     *
     * @param fieldName The field name.
     * @param value Boolean value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeBooleanField(String fieldName, Boolean value) {
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
     * Writes a nullable JSON number field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeNumber(Number)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't be
     * written.
     *
     * @param fieldName The field name.
     * @param value Number value to write.
     * @return The updated JsonWriter object.
     */
    public final JsonWriter writeNumberField(String fieldName, Number value) {
        return writeFieldName(fieldName).writeNumber(value);
    }

    /**
     * Writes a JSON String field.
     * <p>
     * Combines {@link #writeFieldName(String)} and {@link #writeString(String)} to simplify adding a key-value to a
     * JSON object.
     * <p>
     * A value is always written no matter whether {@code value} is null, if null shouldn't be written this API call
     * must be null guarded. Or, use {@link #writeNonNull(Object, BiConsumer)} to indicate null shouldn't
     * be written.
     *
     * @param fieldName The field name.
     * @param value String value to write.
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

    /**
     * Writes the unknown type {@code value} field.
     * <p>
     * The following is how each {@code value} is handled (in this order):
     *
     * <ul>
     *     <li>null -&gt; {@link #writeNull()}</li>
     *     <li>{@code short} -&gt; {@link #writeInt(int)}</li>
     *     <li>{@code int} -&gt; {@link #writeInt(int)}</li>
     *     <li>{@code long} -&gt; {@link #writeLong(long)}</li>
     *     <li>{@code float} -&gt; {@link #writeFloat(float)}</li>
     *     <li>{@code double} -&gt; {@link #writeDouble(double)}</li>
     *     <li>{@code boolean} -&gt; {@link #writeBoolean(boolean)}</li>
     *     <li>{@link CharSequence} -&gt; {@link #writeString(String)}</li>
     *     <li>{@code char} -&gt; {@link #writeString(String)}</li>
     *     <li>{@link JsonSerializable} -&gt; {@link #writeJson(JsonSerializable)}</li>
     *     <li>{@code Object[]} -&gt; {@link #writeUntyped(Object)} for each element</li>
     *     <li>{@link Iterable} -&gt; {@link #writeUntyped(Object)} for each element</li>
     *     <li>{@link Map} -&gt; {@link #writeUntyped(Object)} for each element where the key to {@code toString}'d</li>
     *     <li>{@link Object} -&gt; empty JSON object ({@code {}})</li>
     *     <li>All other values use the {@code toString} value with {@link #writeString(String)}</li>
     * </ul>
     *
     * @param fieldName The field name.
     * @param value The value to write.
     * @return The updated JsonWriter object.
     */
    public JsonWriter writeUntypedField(String fieldName, Object value) {
        return writeFieldName(fieldName).writeUntyped(value);
    }

    /**
     * Writes the unknown type {@code value}.
     * <p>
     * The following is how each {@code value} is handled (in this order):
     *
     * <ul>
     *     <li>null -&gt; {@link #writeNull()}</li>
     *     <li>{@code short} -&gt; {@link #writeInt(int)}</li>
     *     <li>{@code int} -&gt; {@link #writeInt(int)}</li>
     *     <li>{@code long} -&gt; {@link #writeLong(long)}</li>
     *     <li>{@code float} -&gt; {@link #writeFloat(float)}</li>
     *     <li>{@code double} -&gt; {@link #writeDouble(double)}</li>
     *     <li>{@code boolean} -&gt; {@link #writeBoolean(boolean)}</li>
     *     <li>{@link CharSequence} -&gt; {@link #writeString(String)}</li>
     *     <li>{@code char} -&gt; {@link #writeString(String)}</li>
     *     <li>{@link JsonSerializable} -&gt; {@link #writeJson(JsonSerializable)}</li>
     *     <li>{@code Object[]} -&gt; {@link #writeUntyped(Object)} for each element</li>
     *     <li>{@link Iterable} -&gt; {@link #writeUntyped(Object)} for each element</li>
     *     <li>{@link Map} -&gt; {@link #writeUntyped(Object)} for each element where the key to {@code toString}'d</li>
     *     <li>{@link Object} -&gt; empty JSON object ({@code {}})</li>
     *     <li>All other values use the {@code toString} value with {@link #writeString(String)}</li>
     * </ul>
     *
     * @param value The value to write.
     * @return The updated JsonWriter object.
     */
    public JsonWriter writeUntyped(Object value) {
        if (value == null) {
            return writeNull();
        } else if (value instanceof Short) {
            return writeInt((short) value);
        } else if (value instanceof Integer) {
            return writeInt((int) value);
        } else if (value instanceof Long) {
            return writeLong((long) value);
        } else if (value instanceof Float) {
            return writeFloat((float) value);
        } else if (value instanceof Double) {
            return writeDouble((double) value);
        } else if (value instanceof Boolean) {
            return writeBoolean((boolean) value);
        } else if (value instanceof byte[]) {
            return writeBinary((byte[]) value);
        } else if (value instanceof CharSequence) {
            return writeString(String.valueOf(value));
        } else if (value instanceof Character) {
            return writeString(String.valueOf(((Character) value).charValue()));
        } else if (value instanceof JsonSerializable<?>) {
            return ((JsonSerializable<?>) value).toJson(this);
        } else if (value instanceof Object[]) {
            return writeArray((Object[]) value, JsonWriter::writeUntyped);
        } else if (value instanceof Iterable<?>) {
            return writeArray((Iterable<?>) value, JsonWriter::writeUntyped);
        } else if (value instanceof Map<?, ?>) {
            Map<?, ?> mapValue = (Map<?, ?>) value;

            writeStartObject();
            mapValue.forEach((k, v) -> writeFieldName(String.valueOf(k)).writeUntyped(v));
            return writeEndObject();
        } else if (value.getClass() == Object.class) {
            return writeStartObject().writeEndObject();
        } else {
            return writeString(String.valueOf(value));
        }
    }
}
