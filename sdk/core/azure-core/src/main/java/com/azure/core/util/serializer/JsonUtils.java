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
