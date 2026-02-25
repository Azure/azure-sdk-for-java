// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.core.ObjectMappers;

import java.util.Map;

public final class OpenAIJsonHelper {

    private static final ObjectMapper MAPPER = ObjectMappers.jsonMapper()
        .rebuild()
        .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
        .configure(MapperFeature.AUTO_DETECT_GETTERS, true)
        .configure(MapperFeature.AUTO_DETECT_CREATORS, true)
        .configure(MapperFeature.AUTO_DETECT_SETTERS, true)
        .build();

    public static <T> JsonValue toJsonValue(T obj) {
        return JsonValue.from(MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {
        }));
    }
}
