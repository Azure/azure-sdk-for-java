package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonHelper {
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    public static String toJsonString(Object input) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        } catch (JsonProcessingException jsonError) {
            return "EXCEPTION: " + jsonError.toString();
        }
    }
}
