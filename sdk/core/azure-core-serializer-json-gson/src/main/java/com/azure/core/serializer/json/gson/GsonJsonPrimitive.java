// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonPrimitive;

import java.util.Objects;

/**
 * GSON specific implementation of {@link JsonPrimitive}.
 */
public final class GsonJsonPrimitive implements JsonPrimitive {
    private final com.google.gson.JsonPrimitive jsonPrimitive;

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed boolean.
     *
     * @param value Boolean value to wrap.
     */
    public GsonJsonPrimitive(boolean value) {
        this.jsonPrimitive = new com.google.gson.JsonPrimitive(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed double.
     *
     * @param value Double value to wrap.
     */
    public GsonJsonPrimitive(double value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed float.
     *
     * @param value Float value to wrap.
     */
    public GsonJsonPrimitive(float value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed int.
     *
     * @param value Int value to wrap.
     */
    public GsonJsonPrimitive(int value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed long.
     *
     * @param value Long value to wrap.
     */
    public GsonJsonPrimitive(long value) {
        this((Number) value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed short.
     *
     * @param value Short value to wrap.
     */
    public GsonJsonPrimitive(short value) {
        this((Number) value);
    }

    private GsonJsonPrimitive(Number number) {
        this.jsonPrimitive = new com.google.gson.JsonPrimitive(number);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed string.
     *
     * @param value String value to wrap.
     */
    public GsonJsonPrimitive(String value) {
        this.jsonPrimitive = new com.google.gson.JsonPrimitive(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} backed by the passed GSON {@link com.google.gson.JsonPrimitive}.
     *
     * @param jsonPrimitive The backing GSON {@link com.google.gson.JsonPrimitive}.
     * @throws NullPointerException If {@code jsonPrimitive} is {@code null}.
     */
    public GsonJsonPrimitive(com.google.gson.JsonPrimitive jsonPrimitive) {
        this.jsonPrimitive = Objects.requireNonNull(jsonPrimitive, "'jsonPrimitive' cannot be null.");
    }

    com.google.gson.JsonPrimitive getJsonPrimitive() {
        return jsonPrimitive;
    }

    @Override
    public boolean isBoolean() {
        return jsonPrimitive.isBoolean();
    }

    @Override
    public boolean getBoolean() {
        return jsonPrimitive.getAsBoolean();
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

        if (!(obj instanceof GsonJsonPrimitive)) {
            return false;
        }

        return Objects.equals(jsonPrimitive, ((GsonJsonPrimitive) obj).jsonPrimitive);
    }

    @Override
    public int hashCode() {
        return jsonPrimitive.hashCode();
    }
}
