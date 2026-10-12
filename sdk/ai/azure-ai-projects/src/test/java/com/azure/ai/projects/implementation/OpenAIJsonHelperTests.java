// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openai.core.ObjectMappers;
import com.openai.models.Reasoning;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIJsonHelperTests {
    @ParameterizedTest
    @MethodSource("openAIModels")
    public <T> void serializationPreservesWireFormat(Class<T> modelType, String json) throws IOException {
        T model = ObjectMappers.jsonMapper().readValue(json, modelType);

        BinaryData serialized = OpenAIJsonHelper.toBinaryData(model);

        assertFalse(serialized.toString().contains("\"isValid\""));
        assertEquals(ObjectMappers.jsonMapper().readTree(json),
            ObjectMappers.jsonMapper().readTree(serialized.toString()));
        assertEquals(model, OpenAIJsonHelper.fromBinaryData(serialized, modelType));
    }

    @ParameterizedTest
    @MethodSource("openAIModels")
    public <T> void roundTripPreservesAdditionalProperties(Class<T> modelType, String json) throws IOException {
        ObjectNode expected = (ObjectNode) ObjectMappers.jsonMapper().readTree(json);
        expected.putObject("future_setting").put("enabled", true);

        T model = OpenAIJsonHelper.fromBinaryData(BinaryData.fromString(expected.toString()), modelType);

        assertEquals(expected, ObjectMappers.jsonMapper().readTree(OpenAIJsonHelper.toBinaryData(model).toString()));
    }

    @ParameterizedTest
    @MethodSource("openAIModels")
    public <T> void listConversionPreservesNullElements(Class<T> modelType, String json) throws IOException {
        T model = ObjectMappers.jsonMapper().readValue(json, modelType);
        List<T> values = Arrays.asList(model, null);

        List<BinaryData> serialized = OpenAIJsonHelper.toBinaryDataList(values);

        assertEquals(2, serialized.size());
        assertEquals(ObjectMappers.jsonMapper().readTree(json),
            ObjectMappers.jsonMapper().readTree(serialized.get(0).toString()));
        assertNull(serialized.get(1));
        assertEquals(values, OpenAIJsonHelper.fromBinaryDataList(serialized, modelType));
    }

    @Test
    public void nullAndEmptyConversions() {
        assertNull(OpenAIJsonHelper.toBinaryData(null));
        assertNull(OpenAIJsonHelper.fromBinaryData(null, Object.class));
        assertNull(OpenAIJsonHelper.toBinaryDataList(null));
        assertNull(OpenAIJsonHelper.fromBinaryDataList(null, Object.class));
        assertTrue(OpenAIJsonHelper.toBinaryDataList(Collections.emptyList()).isEmpty());
        assertTrue(OpenAIJsonHelper.fromBinaryDataList(Collections.emptyList(), Object.class).isEmpty());
    }

    private static Stream<Arguments> openAIModels() {
        return Stream.of(
            Arguments.of(Reasoning.class, "{\"effort\":\"low\",\"summary\":\"concise\",\"generate_summary\":\"auto\"}"),
            Arguments.of(FunctionTool.class,
                "{\"type\":\"function\",\"name\":\"lookup\",\"strict\":false,"
                    + "\"parameters\":{\"type\":\"object\",\"properties\":{}}}"),
            Arguments.of(Tool.class, "{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_1\"]}"));
    }
}
