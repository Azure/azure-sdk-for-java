// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonPrimitive;
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
 * Jackson specific implementation of {@link JsonPrimitive}.
 */
public final class JacksonJsonPrimitive implements JsonPrimitive {
    private final ValueNode valueNode;

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed boolean.
     *
     * @param value Boolean value to wrap.
     */
    public JacksonJsonPrimitive(boolean value) {
        this.valueNode = (value) ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed double.
     *
     * @param value Double value to wrap.
     */
    public JacksonJsonPrimitive(double value) {
        this.valueNode = new DoubleNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed float.
     *
     * @param value Float value to wrap.
     */
    public JacksonJsonPrimitive(float value) {
        this.valueNode = new FloatNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed int.
     *
     * @param value Int value to wrap.
     */
    public JacksonJsonPrimitive(int value) {
        this.valueNode = new IntNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed long.
     *
     * @param value Long value to wrap.
     */
    public JacksonJsonPrimitive(long value) {
        this.valueNode = new LongNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed short.
     *
     * @param value Short value to wrap.
     */
    public JacksonJsonPrimitive(short value) {
        this.valueNode = new ShortNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} wrapping the passed string.
     *
     * @param value String value to wrap.
     */
    public JacksonJsonPrimitive(String value) {
        this.valueNode = new TextNode(value);
    }

    /**
     * Constructs a {@link JsonPrimitive} backed by the passed Jackson {@link ValueNode}.
     *
     * @param valueNode The backing Jackson {@link ValueNode}.
     * @throws NullPointerException If {@code valueNode} is {@code null}.
     */
    public JacksonJsonPrimitive(ValueNode valueNode) {
        this.valueNode = Objects.requireNonNull(valueNode, "'valueNode' cannot be null.");
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
        return isBoolean() ? valueNode.asBoolean() : Boolean.parseBoolean(getString());
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
        return (float) valueNode.asDouble();
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
        return (short) valueNode.asInt();
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

        if (!(obj instanceof JacksonJsonPrimitive)) {
            return false;
        }

        return Objects.equals(valueNode, ((JacksonJsonPrimitive) obj).valueNode);
    }

    @Override
    public int hashCode() {
        return valueNode.hashCode();
    }
}
