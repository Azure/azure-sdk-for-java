// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.util.DateTimeRfc1123;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Contains utility methods that aid in the serialization to JSON and deserialization from JSON.
 */
public final class JsonUtils {
    /**
     * Handles basic logic for deserializing an object before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for object reading and then check if the current token is
     * {@link JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#FIELD_NAME} and throw an {@link IllegalStateException}. {@link JsonToken#FIELD_NAME} is a valid
     * starting location to support partial object reads.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic, passing the reader and current
     * token.
     * @param <T> The type of object that is being deserialized.
     * @return The deserialized object, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_OBJECT}.
     */
    public static <T> T readObject(JsonReader jsonReader, Function<JsonReader, T> deserializationFunc) {
        JsonToken currentToken = jsonReader.currentToken();
        if (currentToken == null) {
            currentToken = jsonReader.nextToken();
        }

        // If the current token is JSON NULL or current token is still null return null.
        // The current token may be null if there was no JSON content to read.
        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        } else if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + jsonReader.currentToken());
        }

        return deserializationFunc.apply(jsonReader);
    }

    /**
     * Reads the {@link JsonReader} as an untyped object.
     * <p>
     * The returned object is one of the following:
     *
     * <ul>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     * </ul>
     *
     * If the {@link JsonReader#currentToken()} is one of {@link JsonToken#END_ARRAY}, {@link JsonToken#END_OBJECT}, or
     * {@link JsonToken#FIELD_NAME}, an {@link IllegalStateException} will be thrown as an untyped field cannot begin
     * with the ending of an array or object or with the name of a field.
     *
     * @param jsonReader The {@link JsonReader} that will be read into an untyped object.
     * @return The untyped object based on the description.
     * @throws IllegalStateException If the {@link JsonReader#currentToken()} is {@link JsonToken#END_ARRAY},
     * {@link JsonToken#END_OBJECT}, or {@link JsonToken#FIELD_NAME}.
     */
    public static Object readUntypedField(JsonReader jsonReader) {
        return readUntypedField(jsonReader, 0);
    }

    private static Object readUntypedField(JsonReader jsonReader, int depth) {
        // Keep track of array and object nested depth to prevent a StackOverflowError from occurring.
        if (depth >= 1000) {
            throw new IllegalStateException("Untyped object exceeded allowed object nested depth of 1000.");
        }

        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        // Untyped fields cannot begin with END_OBJECT, END_ARRAY, or FIELD_NAME as these would constitute invalid JSON.
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
            throw new IllegalStateException("Unexpected token to begin an untyped field: " + token);
        }

        if (token == JsonToken.NULL || token == null) {
            return null;
        } else if (token == JsonToken.BOOLEAN) {
            return jsonReader.getBooleanValue();
        } else if (token == JsonToken.NUMBER) {
            String numberText = jsonReader.getTextValue();
            if (numberText.contains(".")) {
                return Double.parseDouble(numberText);
            } else {
                return Long.parseLong(numberText);
            }
        } else if (token == JsonToken.STRING) {
            return jsonReader.getStringValue();
        } else if (token == JsonToken.START_ARRAY) {
            List<Object> array = new ArrayList<>();

            while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                array.add(readUntypedField(jsonReader, depth + 1));
            }

            return array;
        } else if (token == JsonToken.START_OBJECT) {
            Map<String, Object> object = new LinkedHashMap<>();

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.getFieldName();
                jsonReader.nextToken();
                Object value = readUntypedField(jsonReader, depth + 1);

                object.put(fieldName, value);
            }

            return object;
        }

        // This should never happen as all JsonToken cases are checked above.
        throw new IllegalStateException("Unknown token type while reading an untyped field: " + token);
    }

    /**
     * Writes the {@code value} as an untyped field to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} that will be written.
     * @param value The value to write.
     * @return The updated {@code jsonWriter} with the {@code value} written to it.
     */
    public static JsonWriter writeUntypedField(JsonWriter jsonWriter, Object value) {
        if (value == null) {
            return jsonWriter.writeNull().flush();
        } else if (value instanceof Short) {
            return jsonWriter.writeInt((short) value).flush();
        } else if (value instanceof Integer) {
            return jsonWriter.writeInt((int) value).flush();
        } else if (value instanceof Long) {
            return jsonWriter.writeLong((long) value).flush();
        } else if (value instanceof Float) {
            return jsonWriter.writeFloat((float) value).flush();
        } else if (value instanceof Double) {
            return jsonWriter.writeDouble((double) value).flush();
        } else if (value instanceof Boolean) {
            return jsonWriter.writeBoolean((boolean) value).flush();
        } else if (value instanceof byte[]) {
            return jsonWriter.writeBinary((byte[]) value).flush();
        } else if (value instanceof CharSequence) {
            return jsonWriter.writeString(String.valueOf(value)).flush();
        } else if (value instanceof Character) {
            return jsonWriter.writeString(String.valueOf(((Character) value).charValue())).flush();
        } else if (value instanceof DateTimeRfc1123) {
            return jsonWriter.writeString(value.toString()).flush();
        } else if (value instanceof OffsetDateTime) {
            return jsonWriter.writeString(value.toString()).flush();
        } else if (value instanceof LocalDate) {
            return jsonWriter.writeString(value.toString()).flush();
        } else if (value instanceof Duration) {
            return jsonWriter.writeString(value.toString()).flush();
        } else if (value instanceof JsonSerializable<?>) {
            return ((JsonSerializable<?>) value).toJson(jsonWriter).flush();
        } else if (value instanceof Iterable<?>) {
            jsonWriter.writeStartArray();
            for (Object obj : (Iterable<?>) value) {
                writeUntypedField(jsonWriter, obj);
            }
            return jsonWriter.writeEndArray().flush();
        } else if (value instanceof Map<?, ?>) {
            jsonWriter.writeStartObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                jsonWriter.writeFieldName(String.valueOf(entry.getKey()));
                writeUntypedField(jsonWriter, entry.getValue());
            }
            return jsonWriter.writeEndObject().flush();
        } else if (value.getClass() == Object.class) {
            return jsonWriter.writeStartObject().writeEndObject().flush();
        } else {
            return jsonWriter.writeString(String.valueOf(value)).flush();
        }
    }

    /**
     * Gets the nullable JSON property as null if the {@link JsonReader JsonReader's} {@link JsonReader#currentToken()}
     * is {@link JsonToken#NULL} or as the non-null value if the current token isn't null.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param nonNullGetter The non-null getter.
     * @param <T> The type of the property.
     * @return Either null if the current token is {@link JsonToken#NULL} or the value returned by the
     * {@code nonNullGetter}.
     */
    public static <T> T getNullableProperty(JsonReader jsonReader, Function<JsonReader, T> nonNullGetter) {
        return jsonReader.currentToken() == JsonToken.NULL ? null : nonNullGetter.apply(jsonReader);
    }

    /**
     * Reads and returns the current JSON object the {@link JsonReader} is pointing to. This will mutate the current
     * location of {@code jsonReader}.
     * <p>
     * If the {@code jsonReader} is pointing to {@link JsonToken#NULL} null will be returned. Otherwise, the current
     * JSON object will be read until completion and returned as a raw JSON string.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The buffered JSON object the {@link JsonReader} was pointing to, or null if it was pointing to
     * {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@code jsonReader}'s {@link JsonReader#currentToken() current token} isn't
     * one of {@link JsonToken#NULL}, {@link JsonToken#START_OBJECT}, {@link JsonToken#START_ARRAY}, or
     * {@link JsonToken#FIELD_NAME}.
     */
    public static String bufferJsonObject(JsonReader jsonReader) {
        if (jsonReader.currentToken() == JsonToken.NULL) {
            // If the current token is JsonToken.NULL return null.
            return null;
        } else if (jsonReader.isStartArrayOrObject()) {
            // If the current token is the beginning of an array or object use JsonReader's readChildren method.
            return jsonReader.readChildren();
        } else if (jsonReader.currentToken() == JsonToken.FIELD_NAME) {
            // Otherwise, we're in a complex case where the reading needs to be handled.

            // Add a starting object token.
            StringBuilder json = new StringBuilder("{");

            JsonToken token = jsonReader.currentToken();
            boolean needsComa = false;
            while (token != JsonToken.END_OBJECT) {
                // Appending comas happens in the subsequent loop run to prevent the case of appending comas before
                // the end of the object, ex {"fieldName":true,}
                if (needsComa) {
                    json.append(",");
                }

                if (token == JsonToken.FIELD_NAME) {
                    // Field names need to have quotes added and a trailing colon.
                    json.append("\"").append(jsonReader.getFieldName()).append("\":");

                    // Comas shouldn't happen after a field name.
                    needsComa = false;
                } else {
                    if (token == JsonToken.STRING) {
                        // String fields need to have quotes added.
                        json.append("\"").append(jsonReader.getStringValue()).append("\"");
                    } else if (jsonReader.isStartArrayOrObject()) {
                        // Structures use readChildren.
                        jsonReader.readChildren(json);
                    } else {
                        // All other value types use text value.
                        json.append(jsonReader.getTextValue());
                    }

                    // Comas should happen after a field value.
                    needsComa = true;
                }

                token = jsonReader.nextToken();
            }

            return json.append("}").toString();
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-array, non-object, non-field name "
                + "starting location. Starting location: " + jsonReader.currentToken());
        }
    }

    private JsonUtils() {
    }
}
