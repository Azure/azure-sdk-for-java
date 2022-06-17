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
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Contains utility methods that aid in the serialization to JSON and deserialization from JSON.
 */
public final class JsonUtils {
    /**
     * Serializes an array.
     * <p>
     * Handles two scenarios for the array:
     *
     * <ul>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * If a null array should be written as JSON null use
     * {@link #writeArray(JsonWriter, String, Object[], boolean, BiConsumer)} and pass true for {@code writeNull}.
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code array} is null
     */
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, T[] array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        return writeArray(jsonWriter, fieldName, array, false, elementWriterFunc);
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null, iff {@code writeNull} is true</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param writeNull Whether JSON null should be written if {@code array} is null.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code array} is null and {@code writeNull} is
     * false.
     */
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, T[] array, boolean writeNull,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNull ? jsonWriter.writeNullField(fieldName).flush() : jsonWriter;
        }

        jsonWriter.writeStartArray(fieldName);

        for (T element : array) {
            elementWriterFunc.accept(jsonWriter, element);
        }

        return jsonWriter.writeEndArray().flush();
    }

    /**
     * Serializes an array.
     * <p>
     * Handles two scenarios for the array:
     *
     * <ul>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * If a null array should be written as JSON null use
     * {@link #writeArray(JsonWriter, String, Iterable, boolean, BiConsumer)} and pass true for {@code writeNull}.
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code array} is null
     */
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, Iterable<T> array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        return writeArray(jsonWriter, fieldName, array, false, elementWriterFunc);
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null, iff {@code writeNull} is true</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param writeNull Whether JSON null should be written if {@code array} is null.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code array} is null and {@code writeNull} is
     * false.
     */
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, Iterable<T> array,
        boolean writeNull, BiConsumer<JsonWriter, T> elementWriterFunc) {
        if (array == null) {
            return writeNull ? jsonWriter.writeNullField(fieldName).flush() : jsonWriter;
        }

        jsonWriter.writeStartArray(fieldName);

        for (T element : array) {
            elementWriterFunc.accept(jsonWriter, element);
        }

        return jsonWriter.writeEndArray().flush();
    }

    /**
     * Serializes a map.
     * <p>
     * If the map is null this method is a no-op. Use {@link #writeMap(JsonWriter, String, Map, boolean, BiConsumer)}
     * and passed true for {@code writeNull} if JSON null should be written.
     *
     * @param jsonWriter The {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the map.
     * @param map The map.
     * @param entryWriterFunc Function that writes the map entry value.
     * @param <T> Type of map value.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code map} is null
     */
    public static <T> JsonWriter writeMap(JsonWriter jsonWriter, String fieldName, Map<String, T> map,
        BiConsumer<JsonWriter, T> entryWriterFunc) {
        return writeMap(jsonWriter, fieldName, map, false, entryWriterFunc);
    }

    /**
     * Serializes a map.
     * <p>
     * If {@code map} is null and {@code writeNull} is false this method is effectively a no-op.
     *
     * @param jsonWriter The {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the map.
     * @param map The map.
     * @param writeNull Whether JSON null should be written if {@code map} is null.
     * @param entryWriterFunc Function that writes the map entry value.
     * @param <T> Type of map value.
     * @return The updated {@link JsonWriter} object, or a no-op if {@code map} is null and {@code writeNull} is false.
     */
    public static <T> JsonWriter writeMap(JsonWriter jsonWriter, String fieldName, Map<String, T> map,
        boolean writeNull, BiConsumer<JsonWriter, T> entryWriterFunc) {
        if (map == null) {
            return writeNull ? jsonWriter.writeNullField(fieldName).flush() : jsonWriter;
        }

        jsonWriter.writeStartObject(fieldName);

        for (Map.Entry<String, T> entry : map.entrySet()) {
            jsonWriter.writeFieldName(entry.getKey());
            entryWriterFunc.accept(jsonWriter, entry.getValue());
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Handles basic logic for deserializing an object before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for object reading and then check if the current token is
     * {@link JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#FIELD_NAME} and throw an {@link IllegalStateException}. {@link JsonToken#FIELD_NAME} is a valid
     * starting location to support partial object reads.
     * <p>
     * Use {@link #readArray(JsonReader, Function)} if a JSON array is being deserialized.
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

        if (currentToken == JsonToken.NULL) {
            return null;
        } else if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + jsonReader.currentToken());
        }

        return deserializationFunc.apply(jsonReader);
    }

    /**
     * Handles basic logic for deserializing an array before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for array reading and then check if the current token is
     * {@link JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_ARRAY} and throw an
     * {@link IllegalStateException}.
     * <p>
     * Use {@link #readObject(JsonReader, Function)} if a JSON object is being deserialized.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic.
     * @param <T> The type of array element that is being deserialized.
     * @return The deserialized array, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_ARRAY}.
     */
    public static <T> List<T> readArray(JsonReader jsonReader, Function<JsonReader, T> deserializationFunc) {
        if (jsonReader.currentToken() == null) {
            jsonReader.nextToken();
        }

        if (jsonReader.currentToken() == JsonToken.NULL) {
            return null;
        } else if (jsonReader.currentToken() != JsonToken.START_ARRAY) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + jsonReader.currentToken());
        }

        List<T> array = new ArrayList<>();

        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
            array.add(deserializationFunc.apply(jsonReader));
        }

        return array;
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

        // Untyped fields cannot begin with END_OBJECT, END_ARRAY, or FIELD_NAME as these would constitute invalid JSON.
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
            throw new IllegalStateException("Unexpected token to begin an untyped field: " + token);
        }

        if (token == JsonToken.NULL) {
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

            return json.toString();
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-array, non-object, non-field name "
                + "starting location. Starting location: " + jsonReader.currentToken());
        }
    }

    private JsonUtils() {
    }
}
