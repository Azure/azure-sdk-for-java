// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.Closeable;

/**
 * Reads a JSON encoded value as a stream of tokens.
 */
public interface JsonReader extends Closeable {
    /**
     * Gets the {@link JsonToken} that the reader currently points.
     * <p>
     * Returns null if the reader isn't pointing to a token. This happens if the reader hasn't begun to read the JSON
     * value or if reading of the JSON value has completed.
     *
     * @return The {@link JsonToken} that the reader currently points, or null if the reader isn't pointing to a token.
     */
    JsonToken currentToken();

    /**
     * Iterates to and returns the next {@link JsonToken} in the JSON encoded value.
     * <p>
     * Returns null if iterating to the next token completes reading of the JSON encoded value.
     *
     * @return The next {@link JsonToken} in the JSON encoded value, or null if reading completes.
     */
    JsonToken nextToken();

    /**
     * Gets the boolean value if the reader is currently pointing to a {@link JsonToken#TRUE} or {@link JsonToken#FALSE}
     * token.
     * <p>
     * If the reader is pointing to any other token type an {@link IllegalStateException} will be thrown.
     *
     * @return The boolean value based on whether the current token is {@link JsonToken#TRUE} or
     * {@link JsonToken#FALSE}.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#TRUE} or {@link JsonToken#FALSE}.
     */
    boolean getBooleanValue();

    /**
     * Gets the double value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a double.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The double value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * double.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    double getDoubleValue();

    /**
     * Gets the int value if the reader is currently pointing to a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to an int.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The int value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to an
     * int.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    int getIntValue();

    /**
     * Gets the long value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a long.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The long value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * long.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    long getLongValue();

    /**
     * Gets the String value if the reader is currently pointing to a {@link JsonToken#TRUE}, {@link JsonToken#FALSE},
     * {@link JsonToken#NULL}, {@link JsonToken#NUMBER}, or {@link JsonToken#STRING}.
     * <p>
     * If the current token is a {@link JsonToken#TRUE}, {@link JsonToken#FALSE}, or {@link JsonToken#NUMBER} the String
     * representation of the value will be returned. If the current token is {@link JsonToken#NULL} null will be
     * returned.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The String value based on the current token.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#TRUE}, {@link JsonToken#FALSE},
     * {@link JsonToken#NULL}, {@link JsonToken#NUMBER}, or {@link JsonToken#STRING}.
     */
    String getStringValue();

    /**
     * Recursively skips the JSON token sub-stream if the current token is either {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#START_ARRAY}.
     * <p>
     * If the current token isn't the beginning of an object or array this method is a no-op.
     */
    void skipValue();
}
