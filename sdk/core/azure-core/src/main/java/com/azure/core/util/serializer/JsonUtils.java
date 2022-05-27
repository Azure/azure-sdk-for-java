// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
        if (array == null) {
            return jsonWriter.writeNullField(fieldName).flush();
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
        if (array == null) {
            return jsonWriter.writeNullField(fieldName).flush();
        }

        jsonWriter.writeStartArray(fieldName);

        for (T element : array) {
            elementWriterFunc.accept(jsonWriter, element);
        }

        return jsonWriter.writeEndArray().flush();
    }

    /**
     * Handles basic logic for deserializing an object before passing it into the deserialization function.
     * <p>
     * This will initialize the {@link JsonReader} for object reading and then check if the current token is
     * {@link JsonToken#NULL} and return null or check if the current isn't a {@link JsonToken#START_OBJECT} and throw
     * an {@link IllegalStateException}. The {@link JsonToken} passed into the {@code deserializationFunc} will be
     * {@link JsonToken#START_OBJECT} if the function is called.
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
        if (jsonReader.currentToken() == null) {
            jsonReader.nextToken();
        }

        if (jsonReader.currentToken() == JsonToken.NULL) {
            return null;
        } else if (jsonReader.currentToken() != JsonToken.START_OBJECT) {
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
     * Reads the fields of a JSON object until the end of the object is reached.
     * <p>
     * The passed {@link JsonReader} will point to the field value each time {@code fieldNameConsumer} is called.
     * <p>
     * An {@link IllegalStateException} will be thrown if the {@link JsonReader#currentToken()} isn't
     * {@link JsonToken#START_OBJECT} or {@link JsonToken#NULL} when the method is called.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param fieldNameConsumer The field name consumer function.
     * @throws IllegalStateException If {@link JsonReader#currentToken()} isn't {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#NULL} when this method is called.
     */
    public static void readFields(JsonReader jsonReader, Consumer<String> fieldNameConsumer) {
        readFields(jsonReader, false, fieldName -> {
            fieldNameConsumer.accept(fieldName);
            return false;
        });
    }

    /**
     * Reads the fields of a JSON object until the end of the object is reached.
     * <p>
     * The passed {@link JsonReader} will point to the field value each time {@code fieldNameConsumer} is called.
     * <p>
     * An {@link IllegalStateException} will be thrown if the {@link JsonReader#currentToken()} isn't
     * {@link JsonToken#START_OBJECT} or {@link JsonToken#NULL} when the method is called.
     * <p>
     * If {@code readAdditionalProperties} is true and {@code fieldNameConsumer} returns false the JSON field value
     * will be read as if it were an additional property. After the object completes reading the untyped additional
     * properties mapping will be returned, this may be null if there were no additional properties in the JSON object.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @param readAdditionalProperties Whether additional properties should be read.
     * @param fieldNameConsumer The field name consumer function.
     * @return The additional property map if {@code readAdditionalProperties} is true and there were additional
     * properties in the JSON object, otherwise null.
     * @throws IllegalStateException If {@link JsonReader#currentToken()} isn't {@link JsonToken#START_OBJECT} or
     * {@link JsonToken#NULL} when this method is called.
     */
    public static Map<String, Object> readFields(JsonReader jsonReader, boolean readAdditionalProperties,
        Function<String, Boolean> fieldNameConsumer) {
        if (jsonReader.currentToken() != JsonToken.START_OBJECT && jsonReader.currentToken() != JsonToken.NULL) {
            throw new IllegalStateException("Expected the current token of the JsonReader to either be "
                + "START_OBJECT or NULL. It was: " + jsonReader.currentToken());
        }

        if (jsonReader.currentToken() == JsonToken.NULL) {
            return null;
        }

        Map<String, Object> additionalProperties = null;

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            jsonReader.nextToken();

            boolean consumed = fieldNameConsumer.apply(fieldName);

            if (!consumed && readAdditionalProperties) {
                if (additionalProperties == null) {
                    additionalProperties = new LinkedHashMap<>();
                }

                additionalProperties.put(fieldName, readUntypedField(jsonReader));
            } else if (!consumed) {
                jsonReader.skipChildren();
            }
        }

        return additionalProperties;
    }

    private JsonUtils() {
    }
}
