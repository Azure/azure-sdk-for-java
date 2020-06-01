// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonValue;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.util.Objects;

/**
 * Jackson specific implementation of {@link JsonValue}.
 */
public final class JacksonJsonValue implements JsonValue {
    private final ValueNode valueNode;

    /**
     * Constructs a {@link JsonValue} wrapping the passed boolean.
     *
     * @param value Boolean value to wrap.
     */
    public JacksonJsonValue(boolean value) {
        this.valueNode = (value) ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed double.
     *
     * @param value Double value to wrap.
     */
    public JacksonJsonValue(double value) {
        this.valueNode = new DoubleNode(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed float.
     *
     * @param value Float value to wrap.
     */
    public JacksonJsonValue(float value) {
        this.valueNode = new FloatNode(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed int.
     *
     * @param value Int value to wrap.
     */
    public JacksonJsonValue(int value) {
        this.valueNode = new IntNode(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed long.
     *
     * @param value Long value to wrap.
     */
    public JacksonJsonValue(long value) {
        this.valueNode = new LongNode(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed short.
     *
     * @param value Short value to wrap.
     */
    public JacksonJsonValue(short value) {
        this.valueNode = new ShortNode(value);
    }

    /**
     * Constructs a {@link JsonValue} wrapping the passed string.
     *
     * @param value String value to wrap.
     */
    public JacksonJsonValue(String value) {
        this.valueNode = new TextNode(value);
    }

    /**
     * Constructs a {@link JsonValue} backed by the passed Jackson {@link ValueNode}.
     *
     * @param valueNode The backing Jackson {@link ValueNode}.
     */
    public JacksonJsonValue(ValueNode valueNode) {
        this.valueNode = valueNode;
    }

    ValueNode getValueNode() {
        return valueNode;
    }

    @Override
    public boolean isBoolean() {
        return valueNode.isBoolean();
    }

    @Override
    public boolean getBoolean() {
        return valueNode.asBoolean();
    }

    @Override
    public boolean isNumber() {
        return valueNode.isNumber();
    }

    @Override
    public double getDouble() {
        return valueNode.asDouble();
    }

    @Override
    public float getFloat() {
        return valueNode.floatValue();
    }

    @Override
    public int getInteger() {
        return valueNode.asInt();
    }

    @Override
    public long getLong() {
        return valueNode.asLong();
    }

    @Override
    public short getShort() {
        return valueNode.shortValue();
    }

    @Override
    public boolean isString() {
        return valueNode.isTextual();
    }

    @Override
    public String getString() {
        return valueNode.asText();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof JacksonJsonValue)) {
            return false;
        }

        return Objects.equals(valueNode, ((JacksonJsonValue) obj).valueNode);
    }

    @Override
    public int hashCode() {
        return valueNode.hashCode();
    }
}
