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
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, T[] array,
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
    public static <T> JsonWriter writeArray(JsonWriter jsonWriter, String fieldName, Iterable<T> array,
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
     * {@link IllegalStateException}. The {@link JsonToken} passed into the {@code deserializationFunc} will be {@link
     * JsonToken#START_OBJECT} if the function is called.
     * <p>
     * Use {@link #readArray(JsonReader, BiFunction)} if a JSON array is being deserialized.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic, passing the reader and current
     * token.
     * @param <T> The type of object that is being deserialized.
     * @return The deserialized object, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_OBJECT}.
     */
    public static <T> T readObject(JsonReader jsonReader, BiFunction<JsonReader, JsonToken, T> deserializationFunc) {
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
     * Use {@link #readObject(JsonReader, BiFunction)} if a JSON object is being deserialized.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param deserializationFunc The function that handles deserialization logic.
     * @param <T> The type of array element that is being deserialized.
     * @return The deserialized array, or null if the {@link JsonToken#NULL} represents the object.
     * @throws IllegalStateException If the initial token for reading isn't {@link JsonToken#START_ARRAY}.
     */
    public static <T> List<T> readArray(JsonReader jsonReader,
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

    /**
     * Writes the JSON string field if, and only if, {@code value} isn't null.
     *
     * @param writer The {@link JsonWriter} being written.
     * @param fieldName The field name.
     * @param value The value.
     * @return The updated {@link JsonWriter} if {@code value} wasn't null, otherwise the {@link JsonWriter} with no
     * modifications.
     */
    public static JsonWriter writeNonNullStringField(JsonWriter writer, String fieldName, String value) {
        return (value == null) ? writer : writer.writeStringField(fieldName, value);
    }

    /**
     * Writes the JSON int field if, and only if, {@code value} isn't null.
     *
     * @param writer The {@link JsonWriter} being written.
     * @param fieldName The field name.
     * @param value The value.
     * @return The updated {@link JsonWriter} if {@code value} wasn't null, otherwise the {@link JsonWriter} with no
     * modifications.
     */
    public static JsonWriter writeNonNullIntegerField(JsonWriter writer, String fieldName, Integer value) {
        return (value == null) ? writer : writer.writeIntField(fieldName, value);
    }

    private JsonUtils() {
    }
}
