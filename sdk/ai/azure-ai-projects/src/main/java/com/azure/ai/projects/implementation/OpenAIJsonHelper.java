// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.ObjectMappers;

import java.io.IOException;

/**
 * Helper methods for adapting Azure SDK models to openai-java models.
 */
public final class OpenAIJsonHelper {
    private static final ObjectMapper MAPPER = ObjectMappers.jsonMapper()
        .rebuild()
        .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
        .configure(MapperFeature.AUTO_DETECT_GETTERS, true)
        .configure(MapperFeature.AUTO_DETECT_CREATORS, true)
        .configure(MapperFeature.AUTO_DETECT_SETTERS, true)
        .build();

    private OpenAIJsonHelper() {
    }

    /**
     * Converts an Azure SDK type that implements {@link JsonSerializable} to an openai-java type.
     *
     * @param azureObject The Azure SDK model to convert.
     * @param openAIType The target openai-java type.
     * @param <S> The Azure SDK source type.
     * @param <T> The openai-java target type.
     * @return The openai-java model.
     * @throws NullPointerException if {@code azureObject} or {@code openAIType} is null.
     * @throws RuntimeException if conversion fails.
     */
    public static <S extends JsonSerializable<S>, T> T toOpenAIType(S azureObject, Class<T> openAIType) {
        if (azureObject == null) {
            throw new NullPointerException("'azureObject' cannot be null.");
        }
        if (openAIType == null) {
            throw new NullPointerException("'openAIType' cannot be null.");
        }

        try {
            return MAPPER.readValue(BinaryData.fromObject(azureObject).toString(), openAIType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Azure SDK type to OpenAI type.", e);
        }
    }

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
     * Converts a list of OpenAI SDK types to a list of BinaryData objects.
     *
     * @param <S> The source OpenAI SDK type.
     * @param openAIObjects The list of OpenAI SDK objects to convert.
     * @return The equivalent list of BinaryData objects, or null if the input is null.
     */
    public static <S> List<BinaryData> toBinaryDataList(List<S> openAIObjects) {
        if (openAIObjects == null) {
            return null;
        }
        return openAIObjects.stream().map(OpenAIJsonHelper::toBinaryData).collect(Collectors.toList());
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
     * Deserializes a list of {@link BinaryData} objects to a list of openai-java types.
     *
     * @param <T> The target openai-java type.
     * @param dataList The list of BinaryData objects to convert.
     * @param type the target openai-java class.
     * @return The equivalent list of openai-java objects, or null if the input is null.
     */
    public static <T> List<T> fromBinaryDataList(List<BinaryData> dataList, Class<T> type) {
        if (dataList == null) {
            return null;
        }
        return dataList.stream().map(data -> fromBinaryData(data, type)).collect(Collectors.toList());
    }
}
