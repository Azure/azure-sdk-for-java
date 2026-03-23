// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.BinaryData;
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
import java.util.HashMap;
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

    // AI Tooling: openai-java de-dup

    /**
     * Serializes an openai-java object to {@link BinaryData} whose content is a JSON object.
     * The resulting BinaryData can be written to a {@link com.azure.json.JsonWriter} via
     * {@code binaryData.writeTo(jsonWriter)} and will produce a JSON object, not a quoted string.
     *
     * @param openAIObject the openai-java object to serialize.
     * @return BinaryData containing the JSON representation, or null if the input is null.
     */
    public static BinaryData toBinaryData(Object openAIObject) {
        if (openAIObject == null) {
            return null;
        }
        try {
            String json = MAPPER.writeValueAsString(openAIObject);
            try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
                reader.nextToken();
                return BinaryData.fromObject(reader.readUntyped());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert OpenAI type to BinaryData", e);
        }
    }

    /**
     * Deserializes {@link BinaryData} to an openai-java type using the openai-java ObjectMapper.
     *
     * @param data the BinaryData containing JSON.
     * @param type the target openai-java class.
     * @param <T> the target type.
     * @return the deserialized openai-java object, or null if the input is null.
     */
    public static <T> T fromBinaryData(BinaryData data, Class<T> type) {
        if (data == null) {
            return null;
        }
        try {
            return MAPPER.readValue(data.toString(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize BinaryData to OpenAI type", e);
        }
    }

    /**
     * Flattens a {@link JsonSerializable} object into a map of top-level JSON property names to
     * {@link JsonValue} entries. This is useful for adding Azure-specific properties as additional
     * body properties in an OpenAI request, where each field must appear at the top level of the
     * request body rather than nested under a single key.
     *
     * <p>The object is serialized using Azure SDK's {@link JsonSerializable#toJson} (via
     * {@link BinaryData#fromObject}), which preserves the correct wire-format property names
     * (e.g., {@code agent_reference} instead of {@code agentReference}).</p>
     *
     * @param <T> the Azure SDK type that implements {@link JsonSerializable}.
     * @param obj the object to flatten.
     * @return a map of property names to {@link JsonValue} entries, or an empty map if the input is null.
     * @throws RuntimeException if serialization or deserialization fails.
     */
    public static <T extends JsonSerializable<T>> Map<String, JsonValue> toJsonValueMap(T obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        try {
            String json = BinaryData.fromObject(obj).toString();
            Map<String, Object> map = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            Map<String, JsonValue> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), JsonValue.from(entry.getValue()));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to flatten JsonSerializable to JsonValue map", e);
        }
    }

    /**
     * Deserializes a map of {@link JsonValue} entries (typically from
     * {@code response._additionalProperties()}) into an Azure SDK type that implements
     * {@link JsonSerializable}. This is the inverse of {@link #toJsonValueMap}: it takes
     * the additional properties that the OpenAI SDK didn't recognize (i.e., Azure-specific
     * fields like {@code agent_reference}) and reconstructs the corresponding Azure model.
     *
     * @param <T> the Azure SDK type that implements {@link JsonSerializable}.
     * @param additionalProperties the map of property names to {@link JsonValue} entries.
     * @param fromJson the deserializer function, typically a method reference to the static
     *     {@code fromJson} method (e.g., {@code AzureCreateResponseDetails::fromJson}).
     * @return the deserialized Azure SDK object, or null if the input is null or empty.
     * @throws RuntimeException if serialization or deserialization fails.
     */
    public static <T extends JsonSerializable<T>> T
        fromAdditionalProperties(Map<String, JsonValue> additionalProperties, JsonDeserializer<T> fromJson) {
        if (additionalProperties == null || additionalProperties.isEmpty()) {
            return null;
        }
        try {
            // Convert Map<String, JsonValue> → Map<String, Object> → JSON string → Azure type
            Map<String, Object> rawMap = new HashMap<>();
            for (Map.Entry<String, JsonValue> entry : additionalProperties.entrySet()) {
                rawMap.put(entry.getKey(), MAPPER.convertValue(entry.getValue(), Object.class));
            }
            String json = MAPPER.writeValueAsString(rawMap);
            try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(json))) {
                return fromJson.deserialize(jsonReader);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize additional properties to Azure SDK type", e);
        }
    }
}
