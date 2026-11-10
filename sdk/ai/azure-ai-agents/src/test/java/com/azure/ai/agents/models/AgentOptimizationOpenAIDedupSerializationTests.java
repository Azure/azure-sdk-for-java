// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentOptimizationOpenAIDedupSerializationTests {
    private static final String TOOL_JSON = "{\"type\":\"function\",\"function\":{\"name\":\"lookup\","
        + "\"description\":\"Looks up a value\",\"parameters\":{\"type\":\"object\"},\"strict\":true}}";

    @Test
    public void baselineConfigurationRoundTripsOpenAITools() throws IOException {
        FunctionParameters parameters
            = FunctionParameters.builder().putAdditionalProperty("type", JsonValue.from("object")).build();
        FunctionDefinition function = FunctionDefinition.builder()
            .name("lookup")
            .description("Looks up a value")
            .parameters(parameters)
            .strict(true)
            .build();
        ChatCompletionFunctionTool tool = ChatCompletionFunctionTool.builder().function(function).build();
        AgentOptimizationBaselineAgentConfiguration original
            = new AgentOptimizationBaselineAgentConfiguration().setTools(Collections.singletonList(tool));

        String json = serialize(original);
        assertTrue(json.contains("\"type\":\"function\""));
        assertTrue(json.contains("\"name\":\"lookup\""));
        assertTrue(json.contains("\"description\":\"Looks up a value\""));
        assertTrue(json.contains("\"parameters\":{\"type\":\"object\"}"));
        assertTrue(json.contains("\"strict\":true"));

        AgentOptimizationBaselineAgentConfiguration roundTripped;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            roundTripped = AgentOptimizationBaselineAgentConfiguration.fromJson(reader);
        }

        assertEquals("lookup", roundTripped.getTools().get(0).function().name());
        assertEquals(json, serialize(roundTripped));
    }

    @Test
    public void toolsMutationRoundTripsOpenAITools() throws IOException {
        String json = "{\"type\":\"tools\",\"value\":[" + TOOL_JSON + "]}";

        AgentOptimizationToolsMutation mutation;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            mutation = AgentOptimizationToolsMutation.fromJson(reader);
        }

        assertEquals("lookup", mutation.getValue().get(0).function().name());
        String serialized = serialize(mutation);
        assertTrue(serialized.contains("\"type\":\"tools\""));
        assertTrue(serialized.contains("\"type\":\"function\""));
        assertTrue(serialized.contains("\"name\":\"lookup\""));
    }

    private static String serialize(com.azure.json.JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            value.toJson(writer).flush();
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
