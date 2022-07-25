// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
     * <p>
     * If {@link Boolean} should be read use {@link #getBooleanNullableValue()}.
     *
     * @return The boolean value based on the {@link JsonToken#BOOLEAN}.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#BOOLEAN}.
     */
    public abstract boolean getBooleanValue();

    /**
     * Gets the {@link Boolean} value if the reader is currently pointing to a {@link JsonToken#BOOLEAN} token or null
     * if the reader is currently pointing to a {@link JsonToken#NULL}.
     * <p>
     * If the reader is pointing to any other token type an {@link IllegalStateException} will be thrown.
     * <p>
     * If boolean should be read use {@link #getBooleanValue()}.
     *
     * @return The {@link Boolean} value based on the current token.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#BOOLEAN} or
     * {@link JsonToken#NULL}.
     */
    public final Boolean getBooleanNullableValue() {
        return currentToken() == JsonToken.NULL ? null : getBooleanValue();
    }

    /**
     * Gets the float value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a float.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Float} should be read use {@link #getFloatNullableValue()}.
     *
     * @return The float value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * float.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    public abstract float getFloatValue();

    /**
     * Gets the {@link Float} value if the reader is currently pointing to a {@link JsonToken#NUMBER},
     * {@link JsonToken#STRING}, or {@link JsonToken#NULL}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a float.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If float should be read use {@link #getFloatValue()}.
     *
     * @return The {@link Float} value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * float.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER}, {@link JsonToken#STRING}, or
     * {@link JsonToken#NULL}.
     */
    public final Float getFloatNullableValue() {
        return currentToken() == JsonToken.NULL ? null : getFloatValue();
    }

    /**
     * Gets the double value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a double.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Double} should be read use {@link #getDoubleNullableValue()}.
     *
     * @return The double value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * double.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    public abstract double getDoubleValue();

    /**
     * Gets the {@link Double} value if the reader is currently pointing to a {@link JsonToken#NUMBER},
     * {@link JsonToken#STRING}, or {@link JsonToken#NULL}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a double.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If double should be read use {@link #getDoubleValue()}.
     *
     * @return The {@link Double} value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * double.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER}, {@link JsonToken#STRING}, or
     * {@link JsonToken#NULL}.
     */
    public final Double getDoubleNullableValue() {
        return currentToken() == JsonToken.NULL ? null : getDoubleValue();
    }

    /**
     * Gets the int value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to an int.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Integer} should be read use {@link #getIntegerNullableValue()}
     *
     * @return The int value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to an
     * int.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    public abstract int getIntValue();

    /**
     * Gets the {@link Integer} value if the reader is currently pointing to a {@link JsonToken#NUMBER},
     * {@link JsonToken#STRING}, or {@link JsonToken#NULL}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to an int.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If int should be read use {@link #getIntValue()}
     *
     * @return The {@link Integer} value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to an
     * int.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER}, {@link JsonToken#STRING}, or
     * {@link JsonToken#NULL}.
     */
    public final Integer getIntegerNullableValue() {
        return currentToken() == JsonToken.NULL ? null : getIntValue();
    }

    /**
     * Gets the long value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a long.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Long} should be read use {@link #getLongNullableValue()}.
     *
     * @return The long value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * long.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     */
    public abstract long getLongValue();

    /**
     * Gets the {@link Long} value if the reader is currently pointing to a {@link JsonToken#NUMBER},
     * {@link JsonToken#STRING}, or {@link JsonToken#NULL}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a long.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If long should be read use {@link #getLongValue()}.
     *
     * @return The {@link Long} value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * long.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER}, {@link JsonToken#STRING}, or
     * {@link JsonToken#NULL}.
     */
    public final Long getLongNullableValue() {
        return currentToken() == JsonToken.NULL ? null : getLongValue();
    }

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
     * Convenience method to read a nullable type.
     * <p>
     * If the {@link #currentToken()} is {@link JsonToken#NULL} null will be returned, otherwise this {@link JsonReader}
     * will be passed into the {@code nonNullGetter} function to get the value. Effectively, this is the generic form of
     * the get*NullableValue methods.
     *
     * @param nonNullGetter Function that reads the non-null JSON value.
     * @param <T> Type returned by the function.
     * @return null if the {@link #currentToken()} is {@link JsonToken#NULL} or the value returned by
     * {@code nonNullGetter}.
     */
    public final <T> T getNullableValue(Function<JsonReader, T> nonNullGetter) {
        return currentToken() == JsonToken.NULL ? null : nonNullGetter.apply(this);
    }

    /**
     * Recursively skips the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     */
    public abstract void skipChildren();

    /**
     * Reads and returns the current JSON structure the {@link JsonReader} is pointing to. This will mutate the current
     * location of this {@link JsonReader}.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#FIELD_NAME} an
     * {@link IllegalStateException} will be thrown.
     * <p>
     * The returned {@link JsonReader} is able to be {@link #reset()} to replay the underlying JSON stream.
     *
     * @return The buffered JSON object the {@link JsonReader} was pointing to.
     * @throws IllegalStateException If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#FIELD_NAME}.
     */
    public abstract JsonReader bufferObject();

    /**
     * Indicates whether the {@link JsonReader} supports {@link #reset() resetting}.
     *
     * @return Whether {@link #reset()} is supported.
     */
    public abstract boolean resetSupported();

    /**
     * Creates a new {@link JsonReader} reset to the beginning of the JSON stream.
     * <p>
     * Use {@link #resetSupported()} to determine whether the {@link JsonReader} can be reset. If resetting is called
     * and it isn't supported an {@link IllegalStateException} will be thrown.
     *
     * @return A new {@link JsonReader} reset to the beginning of the JSON stream.
     * @throws IllegalStateException If resetting isn't supported by the current JsonReader.
     */
    public abstract JsonReader reset();

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
     * Reads a JSON object.
     * <p>
     * If the {@link #currentToken()} is null this will {@link #nextToken() read the next token}. If the starting token
     * is still null or {@link JsonToken#NULL} null will be returned. If the token is anything other than
     * {@link JsonToken#START_OBJECT} an {@link IllegalStateException} will be thrown.
     * <p>
     * Once the JSON stream is prepared for object reading this will get the next token and pass this {@link JsonReader}
     * into the {@code objectReaderFunc} to handle reading the object.
     * <p>
     * If a JSON array should be read use {@link #readArray(Function)} or if a JSON map should be read use
     * {@link #readMap(Function)}.
     *
     * @param objectReaderFunc Function that reads each value of the key-value pair.
     * @param <T> The value element type.
     * @return The read JSON map, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_OBJECT}, {@link JsonToken#NULL}, or
     * null.
     */
    public final <T> T readObject(Function<JsonReader, T> objectReaderFunc) {
        JsonToken currentToken = currentToken();
        if (currentToken == null) {
            currentToken = nextToken();
        }

        // If the current token is JSON NULL or current token is still null return null.
        // The current token may be null if there was no JSON content to read.
        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        } else if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + currentToken);
        }

        return objectReaderFunc.apply(this);
    }

    /**
     * Reads a JSON array.
     * <p>
     * If the {@link #currentToken()} is null this will {@link #nextToken() read the next token}. If the starting token
     * is still null or {@link JsonToken#NULL} null will be returned. If the token is anything other than
     * {@link JsonToken#START_ARRAY} an {@link IllegalStateException} will be thrown.
     * <p>
     * Once the JSON stream is prepared for element reading this will get the element token and pass this
     * {@link JsonReader} into the {@code elementReaderFunc} to handle reading the element of the array. If the array
     * has no elements an empty list will be returned.
     * <p>
     * If a JSON object should be read use {@link #readObject(Function)} or if a JSON map should be read use
     * {@link #readMap(Function)}.
     *
     * @param elementReaderFunc Function that reads each element of the array.
     * @param <T> The array element type.
     * @return The read JSON array, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_ARRAY}, {@link JsonToken#NULL}, or null.
     */
    public final <T> List<T> readArray(Function<JsonReader, T> elementReaderFunc) {
        JsonToken currentToken = currentToken();
        if (currentToken == null) {
            currentToken = nextToken();
        }

        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        } else if (currentToken != JsonToken.START_ARRAY) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin array deserialization: " + currentToken);
        }

        List<T> array = new LinkedList<>();

        while (nextToken() != JsonToken.END_ARRAY) {
            array.add(elementReaderFunc.apply(this));
        }

        return array;
    }

    /**
     * Reads a JSON map.
     * <p>
     * If the {@link #currentToken()} is null this will {@link #nextToken() read the next token}. If the starting token
     * is still null or {@link JsonToken#NULL} null will be returned. If the token is anything other than
     * {@link JsonToken#START_OBJECT} an {@link IllegalStateException} will be thrown.
     * <p>
     * Once the JSON stream is prepared for key-value reading this will get the next token and read the field name as
     * the key then get the next token after that and pass this {@link JsonReader} into the {@code valueReaderFunc} to
     * handle reading the value of the key-value pair. If the object has no elements an empty map will be returned.
     * <p>
     * If a JSON object should be read use {@link #readObject(Function)} or if a JSON array should be read use
     * {@link #readArray(Function)}.
     *
     * @param valueReaderFunc Function that reads each value of the key-value pair.
     * @param <T> The value element type.
     * @return The read JSON map, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_OBJECT}, {@link JsonToken#NULL}, or
     * null.
     */
    public final <T> Map<String, T> readMap(Function<JsonReader, T> valueReaderFunc) {
        JsonToken currentToken = currentToken();
        if (currentToken == null) {
            currentToken = nextToken();
        }

        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        } else if (currentToken != JsonToken.START_OBJECT) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin map deserialization: " + currentToken);
        }

        Map<String, T> map = new LinkedHashMap<>();

        while (nextToken() != JsonToken.END_OBJECT) {
            String fieldName = getFieldName();
            nextToken();

            map.put(fieldName, valueReaderFunc.apply(this));
        }

        return map;
    }

    /**
     * Reads an untyped object.
     * <p>
     * If the {@link #currentToken()} is null this will {@link #nextToken() read the next token}.
     * <p>
     * If the starting token is {@link JsonToken#END_ARRAY}, {@link JsonToken#END_OBJECT}, or
     * {@link JsonToken#FIELD_NAME} an {@link IllegalStateException} will be thrown as these are invalid starting points
     * for reading an unknown type. If the untyped object is deeply nested an {@link IllegalStateException} will also be
     * thrown to prevent a stack overflow exception.
     * <p>
     * The returned object will be one of the following:
     *
     * <ul>
     *     <li>null if the starting token is null or {@link JsonToken#NULL}</li>
     *     <li>true or false if the starting token is {@link JsonToken#BOOLEAN}</li>
     *     <li>One of int, long, float, or double is the starting token is {@link JsonToken#NUMBER}, the smallest
     *     containing value will be used if the number is an integer</li>
     *     <li>An array of untyped elements if the starting point is {@link JsonToken#START_ARRAY}</li>
     *     <li>A map of String-untyped value if the starting point is {@link JsonToken#START_OBJECT}</li>
     * </ul>
     *
     * @return The untyped value based on the outlined return types above.
     * @throws IllegalStateException If the starting point of the object is {@link JsonToken#END_ARRAY},
     * {@link JsonToken#END_OBJECT}, or {@link JsonToken#FIELD_NAME} or if the untyped object is deeply nested.
     */
    public final Object readUntyped() {
        JsonToken token = currentToken();
        if (token == null) {
            token = nextToken();
        }

        // Untyped fields cannot begin with END_OBJECT, END_ARRAY, or FIELD_NAME as these would constitute invalid JSON.
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
            throw new IllegalStateException("Unexpected token to begin an untyped field: " + token);
        }

        return readUntypedHelper(0);
    }

    private Object readUntypedHelper(int depth) {
        // Keep track of array and object nested depth to prevent a StackOverflowError from occurring.
        if (depth >= 1000) {
            throw new IllegalStateException("Untyped object exceeded allowed object nested depth of 1000.");
        }

        JsonToken token = currentToken();
        if (token == JsonToken.NULL || token == null) {
            return null;
        } else if (token == JsonToken.BOOLEAN) {
            return getBooleanValue();
        } else if (token == JsonToken.NUMBER) {
            String numberText = getTextValue();
            if (numberText.contains(".")) {
                // Unlike integers always use Double to prevent floating point rounding issues.
                return Double.parseDouble(numberText);
            } else {
                try {
                    return Integer.parseInt(numberText);
                } catch (NumberFormatException ex) {
                    return Long.parseLong(numberText);
                }
            }
        } else if (token == JsonToken.STRING) {
            return getStringValue();
        } else if (token == JsonToken.START_ARRAY) {
            List<Object> array = new ArrayList<>();

            while (nextToken() != JsonToken.END_ARRAY) {
                array.add(readUntypedHelper(depth + 1));
            }

            return array;
        } else if (token == JsonToken.START_OBJECT) {
            Map<String, Object> object = new LinkedHashMap<>();

            while (nextToken() != JsonToken.END_OBJECT) {
                String fieldName = getFieldName();
                nextToken();
                Object value = readUntypedHelper(depth + 1);

                object.put(fieldName, value);
            }

            return object;
        }

        // This should never happen as all JsonToken cases are checked above.
        throw new IllegalStateException("Unknown token type while reading an untyped field: " + token);
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
