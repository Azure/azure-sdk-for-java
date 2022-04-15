// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Contains utility methods that aid in the serialization to JSON and deserialization from JSON.
 */
public final class JsonUtils {

    /**
     * Appends a nullable field to the {@link StringBuilder} constructing the JSON.
     * <p>
     * If the {@code value} is null nothing is appended to the {@link StringBuilder}, otherwise {@code
     * "fieldName":value} is appended.
     *
     * @param stringBuilder The {@link StringBuilder} being appended.
     * @param fieldName The name of the field.
     * @param value The value of the field.
     * @return The updated {@link StringBuilder} object.
     */
    public static StringBuilder appendNullableField(StringBuilder stringBuilder, String fieldName, Object value) {
        if (value == null) {
            return stringBuilder;
        }

        stringBuilder.append("\"").append(fieldName).append("\":");

        // Characters and CharSequences need to be quoted.
        return (value instanceof Character || value instanceof CharSequence)
            ? stringBuilder.append("\"").append(value).append("\"")
            : stringBuilder.append(value);
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param stringBuilder {@link StringBuilder} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link StringBuilder} object.
     */
    public static <T> StringBuilder appendArray(StringBuilder stringBuilder, String fieldName, Iterable<T> array,
        BiConsumer<StringBuilder, T> elementWriterFunc) {
        stringBuilder.append("\"").append(fieldName).append("\":");

        if (array == null) {
            return stringBuilder.append("null");
        }

        stringBuilder.append("[");

        int i = 0;
        for (T element : array) {
            if (i > 0) {
                stringBuilder.append(",");
            }

            elementWriterFunc.accept(stringBuilder, element);
            i++;
        }

        return stringBuilder.append("]");
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param stringBuilder {@link StringBuilder} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link StringBuilder} object.
     */
    public static <T> StringBuilder appendArray(StringBuilder stringBuilder, String fieldName, T[] array,
        BiConsumer<StringBuilder, T> elementWriterFunc) {
        stringBuilder.append("\"").append(fieldName).append("\":");

        if (array == null) {
            return stringBuilder.append("null");
        }

        stringBuilder.append("[");

        int i = 0;
        for (T element : array) {
            if (i > 0) {
                stringBuilder.append(",");
            }

            elementWriterFunc.accept(stringBuilder, element);
            i++;
        }

        return stringBuilder.append("]");
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object.
     */
    public static <T> JsonWriter serializeArray(JsonWriter jsonWriter, String fieldName, T[] array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        jsonWriter.writeFieldName(fieldName);

        if (array == null) {
            return jsonWriter.writeNull().flush();
        }

        jsonWriter.writeStartArray();

        for (T element : array) {
            elementWriterFunc.accept(jsonWriter, element);
        }

        return jsonWriter.writeEndArray().flush();
    }

    /**
     * Serializes an array.
     * <p>
     * Handles three scenarios for the array:
     *
     * <ul>
     *     <li>null {@code array} writes JSON null</li>
     *     <li>empty {@code array} writes {@code []}</li>
     *     <li>non-empty {@code array} writes a populated JSON array</li>
     * </ul>
     *
     * @param jsonWriter {@link JsonWriter} where JSON will be written.
     * @param fieldName Field name for the array.
     * @param array The array.
     * @param elementWriterFunc Function that writes the array element.
     * @param <T> Type of array element.
     * @return The updated {@link JsonWriter} object.
     */
    public static <T> JsonWriter serializeArray(JsonWriter jsonWriter, String fieldName, Iterable<T> array,
        BiConsumer<JsonWriter, T> elementWriterFunc) {
        jsonWriter.writeFieldName(fieldName);

        if (array == null) {
            return jsonWriter.writeNull().flush();
        }

        jsonWriter.writeStartArray();

        for (T element : array) {
            elementWriterFunc.accept(jsonWriter, element);
        }

        return jsonWriter.writeEndArray().flush();
    }

    /**
     * Handles basic logic for deserializing an object before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for object reading and then check if the current token is {@link
     * JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_OBJECT} and throw an
     * {@link IllegalStateException}.
     * <p>
     * Use {@link #deserializeArray(JsonReader, BiFunction)} if a JSON array is being deserialized.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic, passing the reader and current
     * token.
     * @param <T> The type of object that is being deserialized.
     * @return The deserialized object, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_OBJECT}.
     */
    public static <T> T deserializeObject(JsonReader jsonReader,
        BiFunction<JsonReader, JsonToken, T> deserializationFunc) {
        JsonToken token = jsonReader.currentToken();

        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token == JsonToken.NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + token);
        }

        return deserializationFunc.apply(jsonReader, token);
    }

    /**
     * Handles basic logic for deserializing an array before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for array reading and then check if the current token is {@link
     * JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_ARRAY} and throw an {@link
     * IllegalStateException}.
     * <p>
     * Use {@link #deserializeObject(JsonReader, BiFunction)} if a JSON object is being deserialized.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic.
     * @param <T> The type of array element that is being deserialized.
     * @return The deserialized array, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_ARRAY}.
     */
    public static <T> List<T> deserializeArray(JsonReader jsonReader,
        BiFunction<JsonReader, JsonToken, T> deserializationFunc) {
        JsonToken token = jsonReader.currentToken();

        if (token == null) {
            token = jsonReader.nextToken();
        }

        if (token == JsonToken.NULL) {
            return null;
        } else if (token != JsonToken.START_ARRAY) {
            // Otherwise, this is an invalid state, throw an exception.
            throw new IllegalStateException("Unexpected token to begin deserialization: " + token);
        }

        List<T> array = new ArrayList<>();

        while ((token = jsonReader.nextToken()) != JsonToken.END_ARRAY) {
            array.add(deserializationFunc.apply(jsonReader, token));
        }

        return array;
    }

    private JsonUtils() {
    }
}
