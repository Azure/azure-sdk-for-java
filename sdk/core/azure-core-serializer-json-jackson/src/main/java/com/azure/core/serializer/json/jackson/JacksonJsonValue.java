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

/**
 * Jackson specific implementation of {@link JsonValue}.
 */
public final class JacksonJsonValue implements JsonValue {
    private final ValueNode valueNode;

    public JacksonJsonValue(boolean value) {
        this.valueNode = (value) ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    public JacksonJsonValue(double value) {
        this.valueNode = new DoubleNode(value);
    }

    public JacksonJsonValue(float value) {
        this.valueNode = new FloatNode(value);
    }

    public JacksonJsonValue(int value) {
        this.valueNode = new IntNode(value);
    }

    public JacksonJsonValue(long value) {
        this.valueNode = new LongNode(value);
    }

    public JacksonJsonValue(short value) {
        this.valueNode = new ShortNode(value);
    }

    public JacksonJsonValue(String value) {
        this.valueNode = new TextNode(value);
    }

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
}
