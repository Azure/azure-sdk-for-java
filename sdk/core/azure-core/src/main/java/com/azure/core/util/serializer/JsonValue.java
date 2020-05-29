// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Interface that represents a JSON value.
 */
public interface JsonValue extends JsonNode {
    @Override
    default boolean isValue() {
        return true;
    }

    /**
     * @return Whether the {@link JsonValue} represents a boolean.
     */
    boolean isBoolean();

    /**
     * @return The boolean value contained in the {@link JsonValue}.
     */
    boolean getBoolean();

    /**
     * @return Whether the {@link JsonValue} represents a number.
     */
    boolean isNumber();

    /**
     * @return The double value contained in the {@link JsonValue}.
     */
    double getDouble();

    /**
     * @return The float value contained in the {@link JsonValue}.
     */
    float getFloat();

    /**
     * @return The int value contained in the {@link JsonValue}.
     */
    int getInteger();

    /**
     * @return The long value contained in the {@link JsonValue}.
     */
    long getLong();

    /**
     * @return The short value contained in the {@link JsonValue}.
     */
    short getShort();

    /**
     * @return Whether the {@link JsonValue} represents a number.
     */
    boolean isString();

    /**
     * @return The String value contained in the {@link JsonValue}.
     */
    String getString();
}
