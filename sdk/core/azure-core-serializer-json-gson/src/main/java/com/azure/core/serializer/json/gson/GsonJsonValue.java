// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonValue;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

/**
 * GSON specific implementation of {@link JsonValue}.
 */
public final class GsonJsonValue implements JsonValue {
    private final JsonPrimitive jsonPrimitive;

    /**
     * Constructs a {@link JsonValue} wrapping the passed boolean.
     *
     * @param value Boolean value to wrap.
     */
    public GsonJsonValue(boolean value) {
        this.jsonPrimitive = new JsonPrimitive(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed double.
     *
     * @param value Double value to wrap.
     */
    public GsonJsonValue(double value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed float.
     *
     * @param value Float value to wrap.
     */
    public GsonJsonValue(float value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed int.
     *
     * @param value Int value to wrap.
     */
    public GsonJsonValue(int value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed long.
     *
     * @param value Long value to wrap.
     */
    public GsonJsonValue(long value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed short.
     *
     * @param value Short value to wrap.
     */
    public GsonJsonValue(short value) {
        this((Number) value);
    }

    private GsonJsonValue(Number number) {
        this.jsonPrimitive = new JsonPrimitive(number);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed string.
     *
     * @param value String value to wrap.
     */
    public GsonJsonValue(String value) {
        this.jsonPrimitive = new JsonPrimitive(value);
    }

    /**
     * Constructs a {@link JsonValue} backed by the passed GSON {@link JsonPrimitive}.
     *
     * @param jsonPrimitive The backing GSON {@link JsonPrimitive}.
     */
    public GsonJsonValue(JsonPrimitive jsonPrimitive) {
        this.jsonPrimitive = jsonPrimitive;
    }

    JsonPrimitive getJsonPrimitive() {
        return jsonPrimitive;
    }

    @Override
    public boolean isBoolean() {
        return jsonPrimitive.isBoolean();
    }

    @Override
    public boolean getBoolean() {
        return jsonPrimitive.isBoolean();
    }

    @Override
    public boolean isNumber() {
        return jsonPrimitive.isNumber();
    }

    @Override
    public double getDouble() {
        return jsonPrimitive.getAsDouble();
    }

    @Override
    public float getFloat() {
        return jsonPrimitive.getAsFloat();
    }

    @Override
    public int getInteger() {
        return jsonPrimitive.getAsInt();
    }

    @Override
    public long getLong() {
        return jsonPrimitive.getAsLong();
    }

    @Override
    public short getShort() {
        return jsonPrimitive.getAsShort();
    }

    @Override
    public boolean isString() {
        return jsonPrimitive.isString();
    }

    @Override
    public String getString() {
        return jsonPrimitive.getAsString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof GsonJsonValue)) {
            return false;
        }

        return Objects.equals(jsonPrimitive, ((GsonJsonValue) obj).jsonPrimitive);
    }

    @Override
    public int hashCode() {
        return jsonPrimitive.hashCode();
    }
}
