// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * Interface that represents a JSON value.
 */
public interface JsonPrimitive extends JsonNode {
    @Override
    default boolean isValue() {
        return true;
    }

    /**
     * Indicates whether the {@link JsonPrimitive} represents a boolean ({@code true, false}).
     *
     * @return Whether the {@link JsonPrimitive} represents a boolean.
     */
    boolean isBoolean();

    /**
     * Gets the boolean value of the {@link JsonPrimitive}.
     *
     * @return The boolean value contained in the {@link JsonPrimitive}.
     */
    boolean getAsBoolean();

    /**
     * Indicates whether the {@link JsonPrimitive} represents a number ({@code 10, 10.0}).
     *
     * @return Whether the {@link JsonPrimitive} represents a number.
     */
    boolean isNumber();

    /**
     * Gets the double value of the {@link JsonPrimitive}.
     *
     * @return The double value contained in the {@link JsonPrimitive}.
     */
    double getAsDouble();

    /**
     * Gets the float value of the {@link JsonPrimitive}.
     *
     * @return The float value contained in the {@link JsonPrimitive}.
     */
    float getAsFloat();

    /**
     * Gets the int value of the {@link JsonPrimitive}.
     *
     * @return The int value contained in the {@link JsonPrimitive}.
     */
    int getAsInt();

    /**
     * Gets the long value of the {@link JsonPrimitive}.
     *
     * @return The long value contained in the {@link JsonPrimitive}.
     */
    long getAsLong();

    /**
     * Gets the short value of the {@link JsonPrimitive}.
     *
     * @return The short value contained in the {@link JsonPrimitive}.
     */
    short getAsShort();

    /**
     * Indicates whether the {@link JsonPrimitive} represents a string ({@code "string"}).
     *
     * @return Whether the {@link JsonPrimitive} represents a string.
     */
    boolean isString();

    /**
     * Gets the String value of the {@link JsonPrimitive}.
     *
     * @return The String value contained in the {@link JsonPrimitive}.
     */
    String getAsString();
}
