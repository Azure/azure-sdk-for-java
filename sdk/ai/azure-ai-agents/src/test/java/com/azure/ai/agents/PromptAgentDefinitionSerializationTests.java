// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for PromptAgentDefinition serialization, particularly focusing on toolChoice handling.
 */
public class PromptAgentDefinitionSerializationTests {

    private static final String TEST_MODEL = "gpt-4o";

    /**
     * Tests serialization when toolChoice is not set (null).
     */
    @Test
    public void testSerializationWithoutToolChoice() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setInstructions("Test instructions");

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"model\":\"gpt-4o\""));
        assertTrue(json.contains("\"instructions\":\"Test instructions\""));
        // tool_choice should not be present when not set
        assertFalse(json.contains("\"tool_choice\""));
    }

    /**
     * Tests serialization with toolChoice set to a simple string value "auto".
     * The string should be passed directly without additional quoting.
     */
    @Test
    public void testSerializationWithToolChoiceAutoString() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice("auto");

        String json = serializeToJson(definition);

        assertNotNull(json);
        // When using setToolChoice(String), the string is wrapped directly
        // The serialization should produce "tool_choice":"auto" or "tool_choice":auto
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("auto"));
    }

    /**
     * Tests serialization with toolChoice set to a simple string value "none".
     */
    @Test
    public void testSerializationWithToolChoiceNoneString() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice("none");

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("none"));
    }

    /**
     * Tests serialization with toolChoice set to a simple string value "required".
     */
    @Test
    public void testSerializationWithToolChoiceRequiredString() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice("required");

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("required"));
    }

    /**
     * Tests serialization with ToolChoiceOptions.AUTO using the OpenAI SDK type.
     */
    @Test
    public void testSerializationWithToolChoiceOptionsAuto() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL)
            .setToolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.AUTO));

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("auto"));
    }

    /**
     * Tests serialization with ToolChoiceOptions.NONE using the OpenAI SDK type.
     */
    @Test
    public void testSerializationWithToolChoiceOptionsNone() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL)
            .setToolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.NONE));

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("none"));
    }

    /**
     * Tests serialization with ToolChoiceOptions.REQUIRED using the OpenAI SDK type.
     */
    @Test
    public void testSerializationWithToolChoiceOptionsRequired() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL)
            .setToolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.REQUIRED));

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("required"));
    }

    /**
     * Tests serialization with toolChoice set to a function object structure as a JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceFunctionJsonString() throws IOException {
        String functionChoiceJson = "{\"type\":\"function\",\"name\":\"get_weather\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(functionChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("function"));
        assertTrue(json.contains("get_weather"));
    }

    /**
     * Tests serialization with toolChoice set to a web_search type as a JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceTypesWebSearchJsonString() throws IOException {
        String typesChoiceJson = "{\"type\":\"web_search\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(typesChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("web_search"));
    }

    /**
     * Tests serialization with toolChoice set to a file_search type as a JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceTypesFileSearchJsonString() throws IOException {
        String typesChoiceJson = "{\"type\":\"file_search\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(typesChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("file_search"));
    }

    /**
     * Tests serialization with toolChoice set to a code_interpreter type as a JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceTypesCodeInterpreterJsonString() throws IOException {
        String typesChoiceJson = "{\"type\":\"code_interpreter\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(typesChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("code_interpreter"));
    }

    /**
     * Tests deserialization with toolChoice set to "auto" string.
     */
    @Test
    public void testDeserializationWithToolChoiceAutoString() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":\"auto\"}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to "none" string.
     */
    @Test
    public void testDeserializationWithToolChoiceNoneString() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":\"none\"}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to "required" string.
     */
    @Test
    public void testDeserializationWithToolChoiceRequiredString() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":\"required\"}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to a function object.
     */
    @Test
    public void testDeserializationWithToolChoiceFunction() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"function\",\"name\":\"get_weather\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to a built-in type (web_search).
     */
    @Test
    public void testDeserializationWithToolChoiceTypesWebSearch() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"web_search\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to a built-in type (file_search).
     */
    @Test
    public void testDeserializationWithToolChoiceTypesFileSearch() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"file_search\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice set to a built-in type (code_interpreter).
     */
    @Test
    public void testDeserializationWithToolChoiceTypesCodeInterpreter() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"code_interpreter\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests deserialization with toolChoice absent.
     */
    @Test
    public void testDeserializationWithoutToolChoice() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"instructions\":\"Test instructions\"}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
        assertEquals("Test instructions", definition.getInstructions());
    }

    /**
     * Tests round-trip serialization/deserialization with ToolChoiceOptions.AUTO.
     */
    @Test
    public void testRoundTripSerializationWithToolChoiceAuto() throws IOException {
        PromptAgentDefinition original = new PromptAgentDefinition(TEST_MODEL).setInstructions("Test instructions")
            .setTemperature(0.7)
            .setToolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.AUTO));

        String json = serializeToJson(original);
        PromptAgentDefinition deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getModel(), deserialized.getModel());
        assertEquals(original.getInstructions(), deserialized.getInstructions());
        assertEquals(original.getTemperature(), deserialized.getTemperature());
    }

    /**
     * Tests round-trip serialization/deserialization with a function toolChoice as JSON string.
     */
    @Test
    public void testRoundTripSerializationWithToolChoiceFunctionJsonString() throws IOException {
        String functionChoiceJson = "{\"type\":\"function\",\"name\":\"calculate_sum\"}";

        PromptAgentDefinition original
            = new PromptAgentDefinition(TEST_MODEL).setInstructions("You are a calculator assistant")
                .setToolChoice(functionChoiceJson);

        String json = serializeToJson(original);
        PromptAgentDefinition deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getModel(), deserialized.getModel());
        assertEquals(original.getInstructions(), deserialized.getInstructions());
    }

    /**
     * Tests serialization with all fields populated including toolChoice.
     */
    @Test
    public void testFullSerializationWithAllFields() throws IOException {
        PromptAgentDefinition definition
            = new PromptAgentDefinition(TEST_MODEL).setInstructions("You are a helpful assistant")
                .setTemperature(0.8)
                .setTopP(0.9)
                .setToolChoice(ResponseCreateParams.ToolChoice.ofOptions(ToolChoiceOptions.AUTO));

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"model\":\"gpt-4o\""));
        assertTrue(json.contains("\"instructions\":\"You are a helpful assistant\""));
        assertTrue(json.contains("\"temperature\":0.8"));
        assertTrue(json.contains("\"top_p\":0.9"));
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("auto"));
    }

    /**
     * Tests deserialization of a complex ToolChoiceAllowed structure.
     */
    @Test
    public void testDeserializationWithComplexToolChoiceAllowed() throws IOException {
        String json
            = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"allowed\",\"allowed_tools\":[{\"type\":\"function\",\"name\":\"func1\"},{\"type\":\"function\",\"name\":\"func2\"}]}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests serialization with ToolChoiceAllowed structure as JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceAllowedJsonString() throws IOException {
        String allowedChoiceJson
            = "{\"type\":\"allowed\",\"allowed_tools\":[{\"type\":\"function\",\"name\":\"func1\"}]}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(allowedChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("allowed"));
        assertTrue(json.contains("allowed_tools"));
        assertTrue(json.contains("func1"));
    }

    /**
     * Tests deserialization with toolChoice set to an MCP server configuration.
     */
    @Test
    public void testDeserializationWithToolChoiceMcp() throws IOException {
        String json
            = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"mcp\",\"server_label\":\"my_mcp_server\",\"name\":\"my_tool\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests serialization with MCP tool choice as JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceMcpJsonString() throws IOException {
        String mcpChoiceJson = "{\"type\":\"mcp\",\"server_label\":\"my_mcp_server\",\"name\":\"my_tool\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(mcpChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("mcp"));
        assertTrue(json.contains("my_mcp_server"));
    }

    /**
     * Tests deserialization with toolChoice set to a custom tool configuration.
     */
    @Test
    public void testDeserializationWithToolChoiceCustom() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"tool_choice\":{\"type\":\"custom\",\"name\":\"my_custom_tool\"}}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    /**
     * Tests serialization with custom tool choice as JSON string.
     */
    @Test
    public void testSerializationWithToolChoiceCustomJsonString() throws IOException {
        String customChoiceJson = "{\"type\":\"custom\",\"name\":\"my_custom_tool\"}";
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setToolChoice(customChoiceJson);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"tool_choice\""));
        assertTrue(json.contains("custom"));
        assertTrue(json.contains("my_custom_tool"));
    }

    /**
     * Tests that the model field is required and serialized correctly.
     */
    @Test
    public void testModelFieldIsSerialized() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"model\":\"gpt-4o\""));
    }

    /**
     * Tests serialization of PromptAgentDefinition with only model (minimal valid state).
     */
    @Test
    public void testMinimalSerializationOnlyModel() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL);

        String json = serializeToJson(definition);

        assertNotNull(json);
        assertTrue(json.contains("\"model\":\"gpt-4o\""));
        assertFalse(json.contains("\"tool_choice\""));
        assertFalse(json.contains("\"instructions\""));
    }

    /**
     * Tests deserialization of minimal JSON with only model field.
     */
    @Test
    public void testMinimalDeserializationOnlyModel() throws IOException {
        String json = "{\"model\":\"gpt-4o\"}";

        PromptAgentDefinition definition = deserializeFromJson(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());
    }

    // Helper method to serialize PromptAgentDefinition to JSON string
    private String serializeToJson(PromptAgentDefinition definition) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            definition.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize JSON string to PromptAgentDefinition
    private PromptAgentDefinition deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return PromptAgentDefinition.fromJson(jsonReader);
        }
    }
}
