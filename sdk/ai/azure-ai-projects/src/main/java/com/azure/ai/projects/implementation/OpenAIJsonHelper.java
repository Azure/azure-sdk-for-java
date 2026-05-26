// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.ObjectMappers;

import java.io.IOException;

/**
 * Helper methods for adapting Azure SDK models to openai-java models.
 */
public final class OpenAIJsonHelper {
    private static final ObjectMapper MAPPER = ObjectMappers.jsonMapper();

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
}
