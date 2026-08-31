// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for VoiceAgentLlmGeneratedGreetingConfig serialization, focusing on the toolChoice union type handling.
 * toolChoice is a union type: String ("none"/"auto"/"required") | openai-java ToolChoiceFunction | openai-java
 * ToolChoiceMcp.
 */
public class VoiceAgentLlmGeneratedGreetingConfigSerializationTests {

    private static final String TEST_PROMPT = "Greet the caller warmly.";

    private static VoiceAgentLlmGeneratedGreetingConfig newConfig() {
        return new VoiceAgentLlmGeneratedGreetingConfig(TEST_PROMPT);
    }

    @Test
    public void testSerializationWithoutToolChoice() throws IOException {
        String json = serializeToJson(newConfig());

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"llm_generated\""));
        assertFalse(json.contains("\"tool_choice\""));
    }

    @Test
    public void testSerializationWithToolChoiceAsString() throws IOException {
        String json = serializeToJson(newConfig().setToolChoice("none"));

        assertTrue(json.contains("\"tool_choice\":\"none\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithFunctionToolChoice() throws IOException {
        String json = serializeToJson(newConfig().setFunctionToolChoice(functionChoice("greet")));

        assertTrue(json.contains("\"name\":\"greet\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"type\":\"function\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithMcpToolChoice() throws IOException {
        String json = serializeToJson(newConfig().setMcpToolChoice(mcpChoice("my_server", null)));

        assertTrue(json.contains("\"server_label\":\"my_server\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"type\":\"mcp\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testDeserializationWithToolChoiceAsString() throws IOException {
        String json = "{\"type\":\"llm_generated\",\"prompt\":\"" + TEST_PROMPT + "\",\"tool_choice\":\"required\"}";

        VoiceAgentLlmGeneratedGreetingConfig config = deserializeFromJson(json);

        assertEquals("required", config.getToolChoiceAsString());
        assertNull(config.getFunctionToolChoice());
        assertNull(config.getMcpToolChoice());
    }

    @Test
    public void testDeserializationWithFunctionToolChoice() throws IOException {
        String json = "{\"type\":\"llm_generated\",\"prompt\":\"" + TEST_PROMPT
            + "\",\"tool_choice\":{\"type\":\"function\",\"name\":\"greet\"}}";

        VoiceAgentLlmGeneratedGreetingConfig config = deserializeFromJson(json);

        ToolChoiceFunction toolChoice = config.getFunctionToolChoice();
        assertNotNull(toolChoice);
        assertEquals("greet", toolChoice.name());
        assertNull(config.getMcpToolChoice());
    }

    @Test
    public void testDeserializationWithMcpToolChoice() throws IOException {
        String json = "{\"type\":\"llm_generated\",\"prompt\":\"" + TEST_PROMPT
            + "\",\"tool_choice\":{\"type\":\"mcp\",\"server_label\":\"my_server\",\"name\":\"search\"}}";

        VoiceAgentLlmGeneratedGreetingConfig config = deserializeFromJson(json);

        ToolChoiceMcp toolChoice = config.getMcpToolChoice();
        assertNotNull(toolChoice);
        assertEquals("my_server", toolChoice.serverLabel());
        assertEquals("search", toolChoice.name().orElse(null));
        assertNull(config.getFunctionToolChoice());
    }

    @Test
    public void testDeserializationWithoutToolChoice() throws IOException {
        String json = "{\"type\":\"llm_generated\",\"prompt\":\"" + TEST_PROMPT + "\"}";

        VoiceAgentLlmGeneratedGreetingConfig config = deserializeFromJson(json);

        assertNull(config.getToolChoiceAsString());
        assertNull(config.getFunctionToolChoice());
        assertNull(config.getMcpToolChoice());
    }

    @Test
    public void testRoundTripWithToolChoiceAsString() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfig original = newConfig().setToolChoice("auto");

        VoiceAgentLlmGeneratedGreetingConfig deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(TEST_PROMPT, deserialized.getPrompt());
        assertEquals("auto", deserialized.getToolChoiceAsString());
    }

    @Test
    public void testRoundTripWithFunctionToolChoice() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfig original = newConfig().setFunctionToolChoice(functionChoice("greet"));

        VoiceAgentLlmGeneratedGreetingConfig deserialized = deserializeFromJson(serializeToJson(original));

        ToolChoiceFunction toolChoice = deserialized.getFunctionToolChoice();
        assertNotNull(toolChoice);
        assertEquals("greet", toolChoice.name());
    }

    @Test
    public void testRoundTripWithMcpToolChoice() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfig original = newConfig().setMcpToolChoice(mcpChoice("my_server", "search"));

        VoiceAgentLlmGeneratedGreetingConfig deserialized = deserializeFromJson(serializeToJson(original));

        ToolChoiceMcp toolChoice = deserialized.getMcpToolChoice();
        assertNotNull(toolChoice);
        assertEquals("my_server", toolChoice.serverLabel());
        assertEquals("search", toolChoice.name().orElse(null));
    }

    @Test
    public void testSettingNullOpenAIToolChoiceClearsValue() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfig config
            = newConfig().setFunctionToolChoice(functionChoice("greet")).setFunctionToolChoice(null);

        assertNull(config.getFunctionToolChoice());
        assertFalse(serializeToJson(config).contains("\"tool_choice\""));
    }

    // ===== direct set -> get and cross-variant null =====

    @Test
    public void testDirectSetGetToolChoiceAsString() {
        assertEquals("none", newConfig().setToolChoice("none").getToolChoiceAsString());
    }

    @Test
    public void testDirectSetGetFunctionToolChoice() {
        ToolChoiceFunction choice = newConfig().setFunctionToolChoice(functionChoice("greet")).getFunctionToolChoice();

        assertNotNull(choice);
        assertEquals("greet", choice.name());
    }

    @Test
    public void testDirectSetGetMcpToolChoice() {
        ToolChoiceMcp choice = newConfig().setMcpToolChoice(mcpChoice("my_server", null)).getMcpToolChoice();

        assertNotNull(choice);
        assertEquals("my_server", choice.serverLabel());
    }

    @Test
    public void testToolChoiceCrossVariantReturnsNull() {
        VoiceAgentLlmGeneratedGreetingConfig stringChoice = newConfig().setToolChoice("auto");
        assertNull(stringChoice.getFunctionToolChoice());
        assertNull(stringChoice.getMcpToolChoice());

        VoiceAgentLlmGeneratedGreetingConfig function = newConfig().setFunctionToolChoice(functionChoice("greet"));
        assertNull(function.getToolChoiceAsString());
        assertNull(function.getMcpToolChoice());

        VoiceAgentLlmGeneratedGreetingConfig mcp = newConfig().setMcpToolChoice(mcpChoice("my_server", null));
        assertNull(mcp.getToolChoiceAsString());
        assertNull(mcp.getFunctionToolChoice());
    }

    @Test
    public void testNullArgumentClearsToolChoice() throws IOException {
        VoiceAgentLlmGeneratedGreetingConfig cleared = newConfig().setToolChoice("auto").setToolChoice((String) null);
        assertNull(cleared.getToolChoiceAsString());
        assertNull(cleared.getFunctionToolChoice());
        assertNull(cleared.getMcpToolChoice());
        assertFalse(serializeToJson(cleared).contains("\"tool_choice\""));
    }

    private static ToolChoiceFunction functionChoice(String name) {
        return ToolChoiceFunction.builder().name(name).build();
    }

    private static ToolChoiceMcp mcpChoice(String serverLabel, String name) {
        ToolChoiceMcp.Builder builder = ToolChoiceMcp.builder().serverLabel(serverLabel);
        if (name != null) {
            builder.name(name);
        }
        return builder.build();
    }

    private String serializeToJson(VoiceAgentLlmGeneratedGreetingConfig config) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            config.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private VoiceAgentLlmGeneratedGreetingConfig deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceAgentLlmGeneratedGreetingConfig.fromJson(jsonReader);
        }
    }
}
