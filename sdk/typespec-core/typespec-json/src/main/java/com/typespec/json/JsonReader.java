// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import com.typespec.json.implementation.jackson.core.io.JsonStringEncoder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads a JSON value as a stream of tokens.
 * <p>
 * Instances of {@link JsonReader} are created using an instance of {@link JsonProvider} or using the utility methods
 * in {@link JsonProviders}.
 *
 * @see com.typespec.json
 * @see JsonProvider
 * @see JsonProviders
 */
public abstract class JsonReader implements Closeable {
    private static final JsonStringEncoder ENCODER = JsonStringEncoder.getInstance();

    /**
     * Creates an instance of {@link JsonReader}.
     */
    public JsonReader() {
    }

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
     * @throws IOException If the next token cannot be determined.
     */
    public abstract JsonToken nextToken() throws IOException;

    /**
     * Closes the JSON stream.
     *
     * @throws IOException If the underlying content store fails to close.
     */
    @Override
    public abstract void close() throws IOException;

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
     * @throws IOException If the next value cannot be read as binary.
     */
    public abstract byte[] getBinary() throws IOException;

    /**
     * Gets the boolean value if the reader is currently pointing to a {@link JsonToken#BOOLEAN} token.
     * <p>
     * If the reader is pointing to any other token type an {@link IllegalStateException} will be thrown.
     * <p>
     * If {@link Boolean} should be read use {@link #getNullable(ReadValueCallback)}.
     *
     * @return The boolean value based on the {@link JsonToken#BOOLEAN}.
     * @throws IllegalStateException If the reader isn't pointing to {@link JsonToken#BOOLEAN}.
     * @throws IOException If the next value cannot be read as a boolean.
     */
    public abstract boolean getBoolean() throws IOException;

    /**
     * Gets the float value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a float.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Float} should be read use {@link #getNullable(ReadValueCallback)}.
     *
     * @return The float value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * float.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     * @throws IOException If the next value cannot be read as a float.
     */
    public abstract float getFloat() throws IOException;

    /**
     * Gets the double value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a double.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Double} should be read use {@link #getNullable(ReadValueCallback)}.
     *
     * @return The double value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * double.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     * @throws IOException If the next value cannot be read as a double.
     */
    public abstract double getDouble() throws IOException;

    /**
     * Gets the int value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to an int.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Integer} should be read use {@link #getNullable(ReadValueCallback)}.
     *
     * @return The int value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to an
     * int.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     * @throws IOException If the next value cannot be read as an int.
     */
    public abstract int getInt() throws IOException;

    /**
     * Gets the long value if the reader is currently pointing to a {@link JsonToken#NUMBER} or
     * {@link JsonToken#STRING}.
     * <p>
     * {@link JsonToken#STRING} will throw a {@link NumberFormatException} if the underlying string value cannot be
     * converted to a long.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     * <p>
     * If {@link Long} should be read use {@link #getNullable(ReadValueCallback)}.
     *
     * @return The long value based on the current token.
     * @throws NumberFormatException If the current token is a {@link JsonToken#STRING} and cannot be converted to a
     * long.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#NUMBER} or {@link JsonToken#STRING}.
     * @throws IOException If the next value cannot be read as a long.
     */
    public abstract long getLong() throws IOException;

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
     * @throws IOException If the next value cannot be read as a String.
     */
    public abstract String getString() throws IOException;

    /**
     * Gets the field name if the reader is currently pointing to a {@link JsonToken#FIELD_NAME}.
     * <p>
     * All other {@link JsonToken} types will throw an {@link IllegalStateException}.
     *
     * @return The field name based on the current token.
     * @throws IllegalStateException If the current token isn't a {@link JsonToken#FIELD_NAME}.
     * @throws IOException If the next value cannot be read as a field name.
     */
    public abstract String getFieldName() throws IOException;

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
     * @throws IOException If the next value cannot be read as a nullable.
     */
    public final <T> T getNullable(ReadValueCallback<JsonReader, T> nonNullGetter) throws IOException {
        return currentToken() == JsonToken.NULL ? null : nonNullGetter.read(this);
    }

    /**
     * Recursively skips the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the current token isn't the beginning of an array or object this method is a no-op.
     *
     * @throws IOException If the children cannot be skipped.
     */
    public abstract void skipChildren() throws IOException;

    /**
     * Reads and returns the current JSON object the {@link JsonReader} is pointing to. This will mutate the current
     * location of this {@link JsonReader}.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#FIELD_NAME} an
     * {@link IllegalStateException} will be thrown.
     * <p>
     * If the {@link #currentToken()} is {@link JsonToken#FIELD_NAME} this will create a JSON object where the first
     * JSON field is the {@link #currentToken()} field, meaning this can be called from the middle of a JSON object to
     * create a new JSON object with only a subset of fields (those remaining from when the method is called).
     * <p>
     * The returned {@link JsonReader} is able to be {@link #reset()} to replay the underlying JSON stream.
     *
     * @return The buffered JSON object the {@link JsonReader} was pointing to.
     * @throws IllegalStateException If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#FIELD_NAME} followed by {@link JsonToken#START_OBJECT}
     * @throws IOException If the child object cannot be buffered.
     */
    public abstract JsonReader bufferObject() throws IOException;

    /**
     * Indicates whether the {@link JsonReader} supports {@link #reset() resetting}.
     *
     * @return Whether {@link #reset()} is supported.
     */
    public abstract boolean isResetSupported();

    /**
     * Creates a new {@link JsonReader} reset to the beginning of the JSON stream.
     * <p>
     * Use {@link #isResetSupported()} to determine whether the {@link JsonReader} can be reset. If resetting is called
     * and it isn't supported an {@link IllegalStateException} will be thrown.
     *
     * @return A new {@link JsonReader} reset to the beginning of the JSON stream.
     * @throws IllegalStateException If resetting isn't supported by the current JsonReader.
     * @throws IOException If the {@link JsonReader} cannot be reset.
     */
    public abstract JsonReader reset() throws IOException;

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT}.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#START_ARRAY} nothing will
     * be read.
     *
     * @return The raw textual value of the JSON token sub-stream.
     * @throws IOException If the children cannot be read.
     */
    public final String readChildren() throws IOException {
        return readInternal(new StringBuilder(), true, false).toString();
    }

    /**
     * Recursively reads the JSON token sub-stream if the current token is either {@link JsonToken#START_ARRAY} or
     * {@link JsonToken#START_OBJECT} into the passed {@link StringBuilder}.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#START_ARRAY} nothing will
     * be read.
     *
     * @param buffer The {@link StringBuilder} where the read sub-stream will be written.
     * @throws NullPointerException If {@code buffer} is null.
     * @throws IOException If the children cannot be read.
     */
    public final void readChildren(StringBuilder buffer) throws IOException {
        readInternal(buffer, true, false);
    }

    /**
     * Reads the remaining fields in the current JSON object as a JSON object.
     * <p>
     * If the {@link #currentToken()} is {@link JsonToken#START_OBJECT} this functions the same as
     * {@link #readChildren()}. If the {@link #currentToken()} is {@link JsonToken#FIELD_NAME} this creates a JSON
     * object where the first field is the current field and reads the remaining fields in the JSON object.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#FIELD_NAME} nothing will
     * be read.
     *
     * @return The raw textual value of the remaining JSON fields.
     * @throws IOException If the remaining JSON fields cannot be read.
     */
    public final String readRemainingFieldsAsJsonObject() throws IOException {
        return readInternal(new StringBuilder(), false, true).toString();
    }

    /**
     * Reads the remaining fields in the current JSON object as a JSON object.
     * <p>
     * If the {@link #currentToken()} is {@link JsonToken#START_OBJECT} this functions the same as
     * {@link #readChildren(StringBuilder)}. If the {@link #currentToken()} is {@link JsonToken#FIELD_NAME} this creates
     * a JSON object where the first field is the current field and reads the remaining fields in the JSON object.
     * <p>
     * If the {@link #currentToken()} isn't {@link JsonToken#START_OBJECT} or {@link JsonToken#FIELD_NAME} nothing will
     * be read.
     *
     * @param buffer The {@link StringBuilder} where the remaining JSON fields will be written.
     * @throws NullPointerException If {@code buffer} is null.
     * @throws IOException If the remaining JSON fields cannot be read.
     */
    public final void readRemainingFieldsAsJsonObject(StringBuilder buffer) throws IOException {
        readInternal(buffer, false, true);
    }

    private StringBuilder readInternal(StringBuilder buffer, boolean canStartAtArray, boolean canStartAtFieldName)
        throws IOException {
        Objects.requireNonNull(buffer, "The 'buffer' used to read the JSON object cannot be null.");

        JsonToken token = currentToken();

        boolean canRead = (token == JsonToken.START_OBJECT)
            || (canStartAtArray && token == JsonToken.START_ARRAY)
            || (canStartAtFieldName && token == JsonToken.FIELD_NAME);

        // Not a valid starting point.
        if (!canRead) {
            return buffer;
        }

        if (token == JsonToken.FIELD_NAME) {
            buffer.append("{\"");
            ENCODER.quoteAsString(getFieldName(), buffer);
            buffer.append("\":");
            token = nextToken();
        }

        appendJson(buffer, token);

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

            appendJson(buffer, token);
        }

        return buffer;
    }

    /**
     * Convenience method to read a JSON element into a buffer.
     *
     * @param buffer The buffer where the JSON element value will be written.
     * @param token The type of the JSON element.
     * @throws IOException If an error occurs while reading the JSON element.
     */
    private void appendJson(StringBuilder buffer, JsonToken token) throws IOException {
        // TODO (alzimmer): Think of making this a protected method. This will allow for optimizations such as where
        //  Jackson can read text directly into a StringBuilder which removes a String copy.
        if (token == JsonToken.FIELD_NAME) {
            buffer.append("\"");
            ENCODER.quoteAsString(getFieldName(), buffer);
            buffer.append("\":");
        } else if (token == JsonToken.STRING) {
            buffer.append("\"");
            ENCODER.quoteAsString(getString(), buffer);
            buffer.append("\"");
        } else {
            buffer.append(getText());
        }
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
     * If a JSON array should be read use {@link #readArray(ReadValueCallback)} or if a JSON map should be read use
     * {@link #readMap(ReadValueCallback)}.
     *
     * @param objectReaderFunc Function that reads each value of the key-value pair.
     * @param <T> The value element type.
     * @return The read JSON object, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_OBJECT}, {@link JsonToken#NULL}, or null.
     * @throws IOException If the object cannot be read.
     */
    public final <T> T readObject(ReadValueCallback<JsonReader, T> objectReaderFunc) throws IOException {
        return readMapOrObject(objectReaderFunc, false);
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
     * If a JSON object should be read use {@link #readObject(ReadValueCallback)} or if a JSON map should be read use
     * {@link #readMap(ReadValueCallback)}.
     *
     * @param elementReaderFunc Function that reads each element of the array.
     * @param <T> The array element type.
     * @return The read JSON array, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_ARRAY}, {@link JsonToken#NULL}, or null.
     * @throws IOException If the array cannot be read.
     */
    public final <T> List<T> readArray(ReadValueCallback<JsonReader, T> elementReaderFunc) throws IOException {
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
            array.add(elementReaderFunc.read(this));
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
     * If a JSON object should be read use {@link #readObject(ReadValueCallback)} or if a JSON array should be read use
     * {@link #readArray(ReadValueCallback)}.
     *
     * @param valueReaderFunc Function that reads each value of the key-value pair.
     * @param <T> The value element type.
     * @return The read JSON map, or null if the {@link JsonToken} is null or {@link JsonToken#NULL}.
     * @throws IllegalStateException If the token isn't {@link JsonToken#START_OBJECT}, {@link JsonToken#NULL}, or null.
     * @throws IOException If the map cannot be read.
     */
    public final <T> Map<String, T> readMap(ReadValueCallback<JsonReader, T> valueReaderFunc) throws IOException {
        return readMapOrObject(reader -> {
            Map<String, T> map = new LinkedHashMap<>();

            while (nextToken() != JsonToken.END_OBJECT) {
                String fieldName = getFieldName();
                nextToken();

                map.put(fieldName, valueReaderFunc.read(this));
            }

            return map;
        }, true);
    }

    private <T> T readMapOrObject(ReadValueCallback<JsonReader, T> valueReaderFunc, boolean isMap) throws IOException {
        JsonToken currentToken = currentToken();
        if (currentToken == null) {
            currentToken = nextToken();
        }

        // Reading maps and objects can begin in one of four states with the following outcomes:
        // - JSON null token or no token: returns null
        // - JSON start object or field name: reads the map or object
        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        } else if (currentToken != JsonToken.START_OBJECT) {
            String type = isMap ? "map" : "object";
            throw new IllegalStateException("Unexpected token to begin " + type + " deserialization: " + currentToken);
        }

        return valueReaderFunc.read(this);
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
     * @throws IOException If the untyped cannot be read.
     */
    public final Object readUntyped() throws IOException {
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

    private Object readUntypedHelper(int depth) throws IOException {
        // Keep track of array and object nested depth to prevent a StackOverflowError from occurring.
        if (depth >= 999) {
            throw new IllegalStateException("Untyped object exceeded allowed object nested depth of 1000.");
        }

        JsonToken token = currentToken();
        if (token == JsonToken.NULL || token == null) {
            return null;
        } else if (token == JsonToken.BOOLEAN) {
            return getBoolean();
        } else if (token == JsonToken.NUMBER) {
            String numberText = getText();

            if ("INF".equals(numberText) || "Infinity".equals(numberText)
                || "-INF".equals(numberText) || "-Infinity".equals(numberText)
                || "NaN".equals(numberText)) {
                // Return special Double values as text as not all implementations of JsonReader may be able to handle
                // them as Doubles when parsing generically.
                return numberText;
            } else if (numberText.contains(".")) {
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
            return getString();
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
     *     <li>{@link JsonToken#BOOLEAN} -&gt; String.valueOf {@link #getBoolean()}</li>
     *     <li>{@link JsonToken#NULL} -&gt; "null"</li>
     *     <li>{@link JsonToken#STRING} -&gt; {@link #getString()}</li>
     *     <li>{@link JsonToken#NUMBER} -&gt; String.valueOf {@link #getString()}</li>
     * </ul>
     *
     * If the current token is null an {@link IllegalStateException} will be thrown.
     *
     * @return The text value for the {@link #currentToken()}.
     * @throws IllegalStateException If the current token is null.
     * @throws IOException If the text cannot be read.
     */
    public final String getText() throws IOException {
        return getTextInternal(false);
    }

    /**
     * Gets the raw text value for the {@link #currentToken()}.
     * <p>
     * The following is how each {@link JsonToken} type is handled:
     *
     * <ul>
     *     <li>{@link JsonToken#START_OBJECT} -&gt; &#123;</li>
     *     <li>{@link JsonToken#END_OBJECT} -&gt; &#125;</li>
     *     <li>{@link JsonToken#START_ARRAY} -&gt; [</li>
     *     <li>{@link JsonToken#END_ARRAY} -&gt; ]</li>
     *     <li>{@link JsonToken#FIELD_NAME} -&gt; {@link #getFieldName()} retaining JSON encoded and quoted
     *     characters</li>
     *     <li>{@link JsonToken#BOOLEAN} -&gt; String.valueOf {@link #getBoolean()}</li>
     *     <li>{@link JsonToken#NULL} -&gt; "null"</li>
     *     <li>{@link JsonToken#STRING} -&gt; {@link #getString()} retaining JSON encoded and quoted characters</li>
     *     <li>{@link JsonToken#NUMBER} -&gt; String.valueOf {@link #getString()}</li>
     * </ul>
     *
     * If the current token is null an {@link IllegalStateException} will be thrown.
     *
     * @return The raw text value for the {@link #currentToken()}.
     * @throws IllegalStateException If the current token is null.
     * @throws IOException If the text cannot be read.
     */
    public String getRawText() throws IOException {
        return getTextInternal(true);
    }

    private String getTextInternal(boolean raw) throws IOException {
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
                return raw ? new String(ENCODER.quoteAsUTF8(getFieldName()), StandardCharsets.UTF_8) : getFieldName();

            case BOOLEAN:
                return String.valueOf(getBoolean());

            case NUMBER:
                return getString();

            case STRING:
                return raw ? new String(ENCODER.quoteAsUTF8(getString()), StandardCharsets.UTF_8) : getString();

            case NULL:
                return "null";

            default:
                return ""; // Should never reach this point.
        }
    }
}
