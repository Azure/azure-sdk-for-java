// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.core.ObjectMappers;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OpenAIJsonHelper {

    private static final ObjectMapper MAPPER = ObjectMappers.jsonMapper()
        .rebuild()
        .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
        .configure(MapperFeature.AUTO_DETECT_GETTERS, true)
        .configure(MapperFeature.AUTO_DETECT_CREATORS, true)
        .configure(MapperFeature.AUTO_DETECT_SETTERS, true)
        .build();

    /**
     * Functional interface for deserializing JSON to an Azure SDK type.
     * This is needed because {@link JsonSerializable#fromJson(JsonReader)} is static
     * and cannot be called via generics.
     *
     * @param <T> The target Azure SDK type.
     */
    @FunctionalInterface
    public interface JsonDeserializer<T> {
        T deserialize(JsonReader reader) throws IOException;
    }

    public static <T> JsonValue toJsonValue(T obj) {
        return JsonValue.from(MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {
        }));
    }

    /**
     * Converts an OpenAI SDK type to an Azure SDK type that implements {@link JsonSerializable}.
     * This method serializes the OpenAI type to JSON and then deserializes it as the Azure SDK type,
     * leveraging the fact that both types share the same JSON schema.
     *
     * @param <S> The source OpenAI SDK type.
     * @param <T> The target Azure SDK type that implements {@link JsonSerializable}.
     * @param openAIObject The OpenAI SDK object to convert.
     * @param fromJson The deserializer function, typically a method reference to the static fromJson method
     *                 (e.g., {@code InputItem::fromJson}).
     * @return The equivalent Azure SDK object, or null if the input is null.
     * @throws RuntimeException if the conversion fails due to serialization/deserialization errors.
     */
    public static <S, T extends JsonSerializable<T>> T toAzureType(S openAIObject, JsonDeserializer<T> fromJson) {
        if (openAIObject == null) {
            return null;
        }
        try {
            String json = MAPPER.writeValueAsString(openAIObject);
            try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(json))) {
                return fromJson.deserialize(jsonReader);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OpenAI type to JSON", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize JSON to Azure SDK type", e);
        }
    }

    /**
     * Converts a list of OpenAI SDK types to a list of Azure SDK types.
     *
     * @param <S> The source OpenAI SDK type.
     * @param <T> The target Azure SDK type that implements {@link JsonSerializable}.
     * @param openAIObjects The list of OpenAI SDK objects to convert.
     * @param fromJson The deserializer function, typically a method reference to the static fromJson method
     *                 (e.g., {@code InputItem::fromJson}).
     * @return The equivalent list of Azure SDK objects, or null if the input is null.
     */
    public static <S, T extends JsonSerializable<T>> List<T> toAzureTypeList(List<S> openAIObjects,
        JsonDeserializer<T> fromJson) {
        if (openAIObjects == null) {
            return null;
        }
        return openAIObjects.stream().map(obj -> toAzureType(obj, fromJson)).collect(Collectors.toList());
    }
}
