// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.Closeable;
import java.util.function.Predicate;

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
     * @return The boolean value based on whether the current token is {@link JsonToken#TRUE} or {@link
     * JsonToken#FALSE}.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#TRUE} or {@link JsonToken#FALSE}.
     */
    boolean getBooleanValue();

    /**
     * Gets the double value if the reader is currently pointing to a {@link JsonToken#NUMBER} or {@link
     * JsonToken#STRING}.
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
     * Gets the int value if the reader is currently pointing to a {@link JsonToken#NUMBER} or {@link
     * JsonToken#STRING}.
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
     * Gets the long value if the reader is currently pointing to a {@link JsonToken#NUMBER} or {@link
     * JsonToken#STRING}.
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
     * Gets the field name if the reader is currently pointing to a {@link JsonToken#FIELD_NAME}.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The field name based on the current token.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#FIELD_NAME}.
     */
    String getFieldName();

    /**
     * Recursively skips the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     */
    void skipChildren();

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     *
     * @return The raw textual value of the JSON token sub-stream.
     */
    default String readChildren() {
        StringBuilder builder = new StringBuilder();

        readChildren(builder);

        return builder.toString();
    }

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT} into the passed {@link StringBuilder}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     *
     * @param buffer The {@link StringBuilder} where the read sub-stream will be written.
     */
    default void readChildren(StringBuilder buffer) {
        JsonToken token = currentToken();

        // Not pointing to an array or object start, no-op.
        if (token != JsonToken.START_ARRAY && token != JsonToken.START_OBJECT) {
            return;
        }

        buffer.append(getTextValue());

        // Initial array or object depth is 1.
        int depth = 1;

        Predicate<JsonToken> structStart = t -> t == JsonToken.START_ARRAY || t == JsonToken.START_OBJECT;
        Predicate<JsonToken> structEnd = t -> t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT;

        while (depth > 0) {
            JsonToken previousToken = token;
            token = nextToken();

            if (structStart.test(token)) {
                // Entering another array or object, increase depth.
                depth++;
            } else if (structEnd.test(token)) {
                // Existing the array or object, decrease depth.
                depth--;
            } else if (token == null) {
                // Should never get into this state if the JSON token stream is properly formatted JSON.
                // But if this happens, just return until a better strategy can be determined.
                return;
            }

            // 1. If the previous token was a struct start token it should never be followed by ','.
            // 2. If the current token is a struct end a ',' should never occur between it and the previous token.
            // 3. If the previous token was a field name a ',' should never occur after it.
            if (!(structStart.test(previousToken) || structEnd.test(token) || previousToken == JsonToken.FIELD_NAME)) {
                buffer.append(',');
            }

            if (token == JsonToken.FIELD_NAME) {
                buffer.append("\"").append(getFieldName()).append("\":");
            } else if (token == JsonToken.STRING) {
                buffer.append("\"").append(getStringValue()).append("\"");
            } else {
                buffer.append(getTextValue());
            }
        }
    }

    /**
     * Prepares the {@link JsonReader} for reading an object.
     * <p>
     * Object reading begins by getting the {@link JsonToken} the {@link JsonReader} is currently pointing. If the
     * current token is null it's an indicator that the {@link JsonReader} hasn't begun reading the underlying JSON
     * stream. In this case it will iterate to the next token to begin reading.
     * <p>
     * If the returned {@link JsonToken} is null this is an indication that the JSON stream has completed reading.
     *
     * @return The {@link JsonToken} where object reading will begin.
     */
    default JsonToken beginReadingObject() {
        JsonToken token = currentToken();

        if (token == null) {
            token = nextToken();
        }

        return token;
    }

    /**
     * Gets the text value for the {@link #currentToken()}.
     * <p>
     * The following is how each {@link JsonToken} type is handled:
     *
     * <ul>
     *     <li>{@link JsonToken#START_OBJECT} -> {</li>
     *     <li>{@link JsonToken#END_OBJECT} -> }</li>
     *     <li>{@link JsonToken#START_ARRAY} -> [</li>
     *     <li>{@link JsonToken#END_ARRAY} -> ]</li>
     *     <li>{@link JsonToken#FIELD_NAME} -> {@link #getFieldName()}</li>
     *     <li>{@link JsonToken#TRUE} -> String.valueOf {@link #getBooleanValue()}</li>
     *     <li>{@link JsonToken#FALSE} -> String.valueOf {@link #getBooleanValue()}</li>
     *     <li>{@link JsonToken#NULL} -> "null"</li>
     *     <li>{@link JsonToken#STRING} -> {@link #getStringValue()}</li>
     *     <li>{@link JsonToken#NUMBER} -> String.valueOf {@link #getStringValue()}</li>
     * </ul>
     *
     * If the current token is null an {@link IllegalStateException} will be thrown.
     *
     * @return The text value for the {@link #currentToken()}.
     * @throws IllegalStateException If the current token is null.
     */
    default String getTextValue() {
        JsonToken token = currentToken();

        if (token == null) {
            throw new IllegalStateException("Current token cannot be null.");
        }

        switch (token) {
            case START_OBJECT:
                return "{";

            case END_OBJECT:
                return "}";

            case START_ARRAY:
                return "[";

            case END_ARRAY:
                return "]";

            case FIELD_NAME:
                return getFieldName();

            case TRUE:
                return "true";

            case FALSE:
                return "false";

            case NUMBER:
            case STRING:
                return getStringValue();

            case NULL:
                return "null";

            default:
                return ""; // Should never reach this point.
        }
    }
}
