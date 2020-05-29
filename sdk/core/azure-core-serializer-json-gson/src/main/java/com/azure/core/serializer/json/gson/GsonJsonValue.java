// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonValue;
import com.google.gson.JsonPrimitive;

/**
 * GSON specific implementation of {@link JsonValue}.
 */
public final class GsonJsonValue implements JsonValue {
    private final JsonPrimitive jsonPrimitive;

    public GsonJsonValue(boolean value) {
        this.jsonPrimitive = new JsonPrimitive(value);
    }

    public GsonJsonValue(double value) {
        this((Number) value);
    }

    public GsonJsonValue(float value) {
        this((Number) value);
    }

    public GsonJsonValue(int value) {
        this((Number) value);
    }

    public GsonJsonValue(long value) {
        this((Number) value);
    }

    public GsonJsonValue(short value) {
        this((Number) value);
    }

    private GsonJsonValue(Number number) {
        this.jsonPrimitive = new JsonPrimitive(number);
    }

    public GsonJsonValue(String value) {
        this.jsonPrimitive = new JsonPrimitive(value);
    }

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
}
