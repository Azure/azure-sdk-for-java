// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.json.ReadValueCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The JSON Converter.
 */
public final class JsonConverterUtil {
    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonConverterUtil.class.getName());

    /**
     * Deserializes the {@code json} as an instance of {@link JsonSerializable}.
     *
     * @param deserializationFunction The deserialization function.
     * @param json The JSON being deserialized.
     *
     * @return An instance of {@code jsonSerializable} based on the {@code json}.
     *
     * @throws IOException If an error occurs during deserialization.
     * @throws IllegalStateException If the {@code jsonSerializable} does not have a static {@code fromJson} method.
     * @throws Error If an error occurs during deserialization.
     */
    public static <T extends JsonSerializable<T>> T fromJson(ReadValueCallback<JsonReader, T> deserializationFunction,
        String json) throws IOException {

        LOGGER.entering("JsonConverterUtil", "fromJson", new Object[] { deserializationFunction, json });

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            T deserialized = deserializationFunction.read(jsonReader);

            LOGGER.exiting("JsonConverterUtil", "fromJson", deserialized);

            return deserialized;
        }
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param jsonSerializable The object to serialize.
     */
    @SuppressWarnings("CharsetObjectCanBeUsed")
    public static String toJson(JsonSerializable<?> jsonSerializable) {
        LOGGER.entering("JsonConverterUtil", "toJson", jsonSerializable);

        if (jsonSerializable == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(byteArrayOutputStream)) {

            jsonWriter.writeUntyped(jsonSerializable);
            jsonWriter.flush();

            return byteArrayOutputStream.toString("UTF-8");
        } catch (IOException e) {
            LOGGER.log(WARNING, "Unable to convert to JSON", e);
        }

        LOGGER.exiting("JsonConverterUtil", "toJson");

        return null;
    }
}
