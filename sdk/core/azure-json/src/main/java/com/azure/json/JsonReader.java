// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.Closeable;
import java.util.Base64;

/**
 * Reads a JSON encoded value as a stream of tokens.
 */
public abstract class JsonReader implements Closeable {
    /**
     * Gets the {@link JsonToken} that the reader currently points.
     * <p>
     * Returns null if the reader isn't pointing to a token. This happens if the reader hasn't begun to read the JSON
     * value or if reading of the JSON value has completed.
     *
     * @return The {@link JsonToken} that the reader currently points, or null if the reader isn't pointing to a token.
     */
    public abstract JsonToken currentToken();

    /**
     * Iterates to and returns the next {@link JsonToken} in the JSON encoded value.
     * <p>
     * Returns null if iterating to the next token completes reading of the JSON encoded value.
     *
     * @return The next {@link JsonToken} in the JSON encoded value, or null if reading completes.
     */
    public abstract JsonToken nextToken();

    /**
     * Whether the {@link #currentToken()} is {@link JsonToken#START_ARRAY} or {@link JsonToken#START_OBJECT}.
     *
     * @return Whether the {@link #currentToken()} is {@link JsonToken#START_ARRAY} or {@link JsonToken#START_OBJECT}.
     */
    public final boolean isStartArrayOrObject() {
        return isStartArrayOrObject(currentToken());
    }

    private static boolean isStartArrayOrObject(JsonToken token) {
        return token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT;
    }

    /**
     * Whether the {@link #currentToken()} is {@link JsonToken#END_ARRAY} or {@link JsonToken#END_OBJECT}.
     *
     * @return Whether the {@link #currentToken()} is {@link JsonToken#END_ARRAY} or {@link JsonToken#END_OBJECT}.
     */
    public final boolean isEndArrayOrObject() {
        return isEndArrayOrObject(currentToken());
    }

    private static boolean isEndArrayOrObject(JsonToken token) {
        return token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT;
    }

    /**
     * Gets the binary value if the reader is currently pointing to a {@link JsonToken#STRING} token.
     * <p>
     * This returns the equivalent of {@link Base64#getDecoder()} {@link Base64.Decoder#decode(String)}.
     * <p>
     * If the reader is pointing to a {@link JsonToken#NULL} null will be returned. If the reader is pointing to any
     * other token type an {@link IllegalStateException} will be thrown.
     *
     * @return The binary value based on whether the current token is {@link JsonToken#STRING} or
     * {@link JsonToken#NULL}.
     * @throws IllegalStateException If the reader isn't pointing to either {@link JsonToken#STRING} or
     * {@link JsonToken#NULL}.
     */
    public abstract byte[] getBinaryValue();

    /**
     * Gets the boolean value if the reader is currently pointing to a {@link JsonToken#BOOLEAN} token.
     * <p>
     * If the reader is pointing to any other token type an {@link IllegalStateException} will be thrown.
     *
     * @return The boolean value based on whether the current token is {@link JsonToken#BOOLEAN}.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#BOOLEAN}.
     */
    public abstract boolean getBooleanValue();

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
    public abstract double getDoubleValue();

    /**
     * Gets the float value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a double.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The float value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * float.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    public abstract float getFloatValue();

    /**
     * Gets the int value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
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
    public abstract int getIntValue();

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
    public abstract long getLongValue();

    /**
     * Gets the String value if the reader is currently pointing to a {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL},
     * {@link JsonToken#NUMBER}, or {@link JsonToken#STRING}.
     * <p>
     * If the current token is a {@link JsonToken#BOOLEAN}, or {@link JsonToken#NUMBER} the String representation of the
     * value will be returned. If the current token is {@link JsonToken#NULL} null will be returned.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The String value based on the current token.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#BOOLEAN}, {@link JsonToken#NULL},
     * {@link JsonToken#NUMBER}, or {@link JsonToken#STRING}.
     */
    public abstract String getStringValue();

    /**
     * Gets the field name if the reader is currently pointing to a {@link JsonToken#FIELD_NAME}.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The field name based on the current token.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#FIELD_NAME}.
     */
    public abstract String getFieldName();

    /**
     * Recursively skips the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     */
    public abstract void skipChildren();

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     *
     * @return The raw textual value of the JSON token sub-stream.
     */
    public final String readChildren() {
        return readChildrenInternal(new StringBuilder()).toString();
    }

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT} into the passed {@link StringBuilder}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     *
     * @param buffer The {@link StringBuilder} where the read sub-stream will be written.
     */
    public final void readChildren(StringBuilder buffer) {
        readChildrenInternal(buffer);
    }

    private StringBuilder readChildrenInternal(StringBuilder buffer) {
        JsonToken token = currentToken();

        // Not pointing to an array or object start, no-op.
        if (!isStartArrayOrObject(token)) {
            return buffer;
        }

        buffer.append(getTextValue());

        // Initial array or object depth is 1.
        int depth = 1;

        while (depth > 0) {
            JsonToken previousToken = token;
            token = nextToken();

            if (isStartArrayOrObject(token)) {
                // Entering another array or object, increase depth.
                depth++;
            } else if (isEndArrayOrObject(token)) {
                // Existing the array or object, decrease depth.
                depth--;
            } else if (token == null) {
                // Should never get into this state if the JSON token stream is properly formatted JSON.
                // But if this happens, just return until a better strategy can be determined.
                return buffer;
            }

            // 1. If the previous token was a struct start token it should never be followed by ','.
            // 2. If the current token is a struct end a ',' should never occur between it and the previous token.
            // 3. If the previous token was a field name a ',' should never occur after it.
            if (!(isStartArrayOrObject(previousToken)
                || isEndArrayOrObject(token)
                || previousToken == JsonToken.FIELD_NAME)) {
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

        return buffer;
    }

    /**
     * Gets the text value for the {@link #currentToken()}.
     * <p>
     * The following is how each {@link JsonToken} type is handled:
     *
     * <ul>
     *     <li>{@link JsonToken#START_OBJECT} -&gt; &#123;</li>
     *     <li>{@link JsonToken#END_OBJECT} -&gt; &#125;</li>
     *     <li>{@link JsonToken#START_ARRAY} -&gt; [</li>
     *     <li>{@link JsonToken#END_ARRAY} -&gt; ]</li>
     *     <li>{@link JsonToken#FIELD_NAME} -&gt; {@link #getFieldName()}</li>
     *     <li>{@link JsonToken#BOOLEAN} -&gt; String.valueOf {@link #getBooleanValue()}</li>
     *     <li>{@link JsonToken#NULL} -&gt; "null"</li>
     *     <li>{@link JsonToken#STRING} -&gt; {@link #getStringValue()}</li>
     *     <li>{@link JsonToken#NUMBER} -&gt; String.valueOf {@link #getStringValue()}</li>
     * </ul>
     *
     * If the current token is null an {@link IllegalStateException} will be thrown.
     *
     * @return The text value for the {@link #currentToken()}.
     * @throws IllegalStateException If the current token is null.
     */
    public final String getTextValue() {
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

            case BOOLEAN:
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
