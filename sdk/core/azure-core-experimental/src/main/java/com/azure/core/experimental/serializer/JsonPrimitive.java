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
     * @return Whether the {@link JsonPrimitive} represents a boolean.
     */
    boolean isBoolean();

    /**
     * @return The boolean value contained in the {@link JsonPrimitive}.
     */
    boolean getAsBoolean();

    /**
     * @return Whether the {@link JsonPrimitive} represents a number.
     */
    boolean isNumber();

    /**
     * @return The double value contained in the {@link JsonPrimitive}.
     */
    double getAsDouble();

    /**
     * @return The float value contained in the {@link JsonPrimitive}.
     */
    float getAsFloat();

    /**
     * @return The int value contained in the {@link JsonPrimitive}.
     */
    int getAsInt();

    /**
     * @return The long value contained in the {@link JsonPrimitive}.
     */
    long getAsLong();

    /**
     * @return The short value contained in the {@link JsonPrimitive}.
     */
    short getAsShort();

    /**
     * @return Whether the {@link JsonPrimitive} represents a number.
     */
    boolean isString();

    /**
     * @return The String value contained in the {@link JsonPrimitive}.
     */
    String getAsString();
}
