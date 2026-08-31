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
 * Tests for VoiceAgentDefinition serialization, focusing on the union typed properties.
 * maxOutputTokens is a union type: int32 | "inf".
 * toolChoice is a union type: String ("none"/"auto"/"required") | openai-java ToolChoiceFunction | openai-java
 * ToolChoiceMcp.
 */
public class VoiceAgentDefinitionSerializationTests {

    private static final String TEST_MODEL = "gpt-realtime";

    private static VoiceAgentDefinition newDefinition() {
        return new VoiceAgentDefinition(VoiceModelType.MANAGED, TEST_MODEL);
    }

    // ===== maxOutputTokens tests =====

    @Test
    public void testSerializationWithoutMaxOutputTokens() throws IOException {
        String json = serializeToJson(newDefinition());

        assertNotNull(json);
        assertTrue(json.contains("\"model\":\"gpt-realtime\""));
        assertFalse(json.contains("\"max_output_tokens\""));
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsInt() throws IOException {
        String json = serializeToJson(newDefinition().setMaxOutputTokens(4096));

        assertTrue(json.contains("\"max_output_tokens\":4096"), "Expected a raw JSON number, got: " + json);
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsString() throws IOException {
        String json = serializeToJson(newDefinition().setMaxOutputTokens("inf"));

        assertTrue(json.contains("\"max_output_tokens\":\"inf\""), "Expected a quoted JSON string, got: " + json);
    }

    @Test
    public void testDeserializationWithMaxOutputTokensAsInt() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\",\"max_output_tokens\":2048}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        assertEquals(Integer.valueOf(2048), definition.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testDeserializationWithMaxOutputTokensAsString() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\",\"max_output_tokens\":\"inf\"}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        assertEquals("inf", definition.getMaxOutputTokensAsString());
    }

    @Test
    public void testDeserializationWithoutMaxOutputTokens() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\"}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        assertNull(definition.getMaxOutputTokensAsInteger());
        assertNull(definition.getMaxOutputTokensAsString());
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsInt() throws IOException {
        VoiceAgentDefinition original = newDefinition().setMaxOutputTokens(1024);

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Integer.valueOf(1024), deserialized.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsString() throws IOException {
        VoiceAgentDefinition original = newDefinition().setMaxOutputTokens("inf");

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("inf", deserialized.getMaxOutputTokensAsString());
    }

    // ===== toolChoice tests =====

    @Test
    public void testSerializationWithoutToolChoice() throws IOException {
        String json = serializeToJson(newDefinition());

        assertFalse(json.contains("\"tool_choice\""));
    }

    @Test
    public void testSerializationWithToolChoiceAsString() throws IOException {
        String json = serializeToJson(newDefinition().setToolChoice("required"));

        assertTrue(json.contains("\"tool_choice\":\"required\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithFunctionToolChoice() throws IOException {
        String json = serializeToJson(newDefinition().setFunctionToolChoice(functionChoice("get_weather")));

        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("\"name\":\"get_weather\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"type\":\"function\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithMcpToolChoice() throws IOException {
        String json = serializeToJson(newDefinition().setMcpToolChoice(mcpChoice("my_server", "search")));

        assertTrue(json.contains("\"server_label\":\"my_server\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"type\":\"mcp\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testDeserializationWithToolChoiceAsString() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\",\"tool_choice\":\"auto\"}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        assertEquals("auto", definition.getToolChoiceAsString());
        assertNull(definition.getFunctionToolChoice());
        assertNull(definition.getMcpToolChoice());
    }

    @Test
    public void testDeserializationWithFunctionToolChoice() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\","
            + "\"tool_choice\":{\"type\":\"function\",\"name\":\"get_weather\"}}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        ToolChoiceFunction toolChoice = definition.getFunctionToolChoice();
        assertNotNull(toolChoice);
        assertEquals("get_weather", toolChoice.name());
        assertNull(definition.getMcpToolChoice());
    }

    @Test
    public void testDeserializationWithMcpToolChoice() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\","
            + "\"tool_choice\":{\"type\":\"mcp\",\"server_label\":\"my_server\",\"name\":\"search\"}}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        ToolChoiceMcp toolChoice = definition.getMcpToolChoice();
        assertNotNull(toolChoice);
        assertEquals("my_server", toolChoice.serverLabel());
        assertEquals("search", toolChoice.name().orElse(null));
        assertNull(definition.getFunctionToolChoice());
    }

    @Test
    public void testDeserializationWithoutToolChoice() throws IOException {
        String json = "{\"model_type\":\"managed\",\"model\":\"gpt-realtime\"}";

        VoiceAgentDefinition definition = deserializeFromJson(json);

        assertNull(definition.getToolChoiceAsString());
        assertNull(definition.getFunctionToolChoice());
        assertNull(definition.getMcpToolChoice());
    }

    @Test
    public void testRoundTripWithToolChoiceAsString() throws IOException {
        VoiceAgentDefinition original = newDefinition().setToolChoice("none");

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("none", deserialized.getToolChoiceAsString());
    }

    @Test
    public void testRoundTripWithFunctionToolChoice() throws IOException {
        VoiceAgentDefinition original = newDefinition().setFunctionToolChoice(functionChoice("get_weather"));

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        ToolChoiceFunction toolChoice = deserialized.getFunctionToolChoice();
        assertNotNull(toolChoice);
        assertEquals("get_weather", toolChoice.name());
    }

    @Test
    public void testRoundTripWithMcpToolChoice() throws IOException {
        VoiceAgentDefinition original = newDefinition().setMcpToolChoice(mcpChoice("my_server", "search"));

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        ToolChoiceMcp toolChoice = deserialized.getMcpToolChoice();
        assertNotNull(toolChoice);
        assertEquals("my_server", toolChoice.serverLabel());
        assertEquals("search", toolChoice.name().orElse(null));
    }

    @Test
    public void testSettingNullOpenAIToolChoiceClearsValue() throws IOException {
        VoiceAgentDefinition definition
            = newDefinition().setMcpToolChoice(mcpChoice("my_server", "search")).setMcpToolChoice(null);

        assertNull(definition.getMcpToolChoice());
        assertFalse(serializeToJson(definition).contains("\"tool_choice\""));
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAndToolChoice() throws IOException {
        VoiceAgentDefinition original = newDefinition().setMaxOutputTokens(512).setToolChoice("auto");

        VoiceAgentDefinition deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Integer.valueOf(512), deserialized.getMaxOutputTokensAsInteger());
        assertEquals("auto", deserialized.getToolChoiceAsString());
    }

    // ===== direct set -> get (same instance, no serialization round trip) =====

    @Test
    public void testDirectSetGetMaxOutputTokensAsInteger() {
        assertEquals(Integer.valueOf(4096), newDefinition().setMaxOutputTokens(4096).getMaxOutputTokensAsInteger());
    }

    @Test
    public void testDirectSetGetMaxOutputTokensAsString() {
        assertEquals("inf", newDefinition().setMaxOutputTokens("inf").getMaxOutputTokensAsString());
    }

    @Test
    public void testDirectSetGetToolChoiceAsString() {
        assertEquals("required", newDefinition().setToolChoice("required").getToolChoiceAsString());
    }

    @Test
    public void testDirectSetGetFunctionToolChoice() {
        ToolChoiceFunction choice
            = newDefinition().setFunctionToolChoice(functionChoice("get_weather")).getFunctionToolChoice();

        assertNotNull(choice);
        assertEquals("get_weather", choice.name());
    }

    @Test
    public void testDirectSetGetMcpToolChoice() {
        ToolChoiceMcp choice = newDefinition().setMcpToolChoice(mcpChoice("my_server", "search")).getMcpToolChoice();

        assertNotNull(choice);
        assertEquals("my_server", choice.serverLabel());
        assertEquals("search", choice.name().orElse(null));
    }

    // ===== cross-variant reads return null instead of throwing =====

    @Test
    public void testMaxOutputTokensCrossVariantReturnsNull() {
        assertNull(newDefinition().setMaxOutputTokens(4096).getMaxOutputTokensAsString());
        assertNull(newDefinition().setMaxOutputTokens("inf").getMaxOutputTokensAsInteger());
    }

    @Test
    public void testMaxOutputTokensCrossVariantReturnsNullAfterDeserialization() throws IOException {
        VoiceAgentDefinition numeric
            = deserializeFromJson("{\"kind\":\"voice\",\"model\":\"gpt-realtime\",\"max_output_tokens\":4096}");
        assertEquals(Integer.valueOf(4096), numeric.getMaxOutputTokensAsInteger());
        assertNull(numeric.getMaxOutputTokensAsString());

        VoiceAgentDefinition text
            = deserializeFromJson("{\"kind\":\"voice\",\"model\":\"gpt-realtime\",\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", text.getMaxOutputTokensAsString());
        assertNull(text.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testToolChoiceCrossVariantReturnsNull() {
        VoiceAgentDefinition stringChoice = newDefinition().setToolChoice("auto");
        assertNull(stringChoice.getFunctionToolChoice());
        assertNull(stringChoice.getMcpToolChoice());

        VoiceAgentDefinition function = newDefinition().setFunctionToolChoice(functionChoice("get_weather"));
        assertNull(function.getToolChoiceAsString());
        assertNull(function.getMcpToolChoice());

        VoiceAgentDefinition mcp = newDefinition().setMcpToolChoice(mcpChoice("my_server", "search"));
        assertNull(mcp.getToolChoiceAsString());
        assertNull(mcp.getFunctionToolChoice());
    }

    @Test
    public void testNullArgumentClearsMaxOutputTokensAndToolChoice() throws IOException {
        VoiceAgentDefinition definition = newDefinition().setMaxOutputTokens("inf").setMaxOutputTokens((String) null);
        assertNull(definition.getMaxOutputTokensAsString());
        assertNull(definition.getMaxOutputTokensAsInteger());
        assertFalse(serializeToJson(definition).contains("\"max_output_tokens\""));

        VoiceAgentDefinition cleared = newDefinition().setToolChoice("auto").setToolChoice((String) null);
        assertNull(cleared.getToolChoiceAsString());
        assertNull(cleared.getFunctionToolChoice());
        assertNull(cleared.getMcpToolChoice());
        assertFalse(serializeToJson(cleared).contains("\"tool_choice\""));
    }

    @Test
    public void testNullArgumentClearsOpenAIToolChoice() throws IOException {
        VoiceAgentDefinition function
            = newDefinition().setFunctionToolChoice(functionChoice("get_weather")).setFunctionToolChoice(null);
        assertNull(function.getFunctionToolChoice());
        assertNull(function.getToolChoiceAsString());
        assertFalse(serializeToJson(function).contains("\"tool_choice\""));
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

    private String serializeToJson(VoiceAgentDefinition definition) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            definition.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private VoiceAgentDefinition deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceAgentDefinition.fromJson(jsonReader);
        }
    }
}
