// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for CodeInterpreterTool serialization, focusing on the container union type handling.
 * The container property is a union type: string (container ID) | AutoCodeInterpreterToolParameter.
 */
public class CodeInterpreterToolSerializationTests {

    /**
     * Tests serialization when container is not set (null).
     */
    @Test
    public void testSerializationWithoutContainer() throws IOException {
        CodeInterpreterTool tool = new CodeInterpreterTool();

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"code_interpreter\""));
        assertFalse(json.contains("\"container\""));
    }

    /**
     * Tests serialization with container set to a string container ID.
     */
    @Test
    public void testSerializationWithContainerIdString() throws IOException {
        CodeInterpreterTool tool = new CodeInterpreterTool();
        tool.setContainer("my-container-id");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"container\""));
        assertTrue(json.contains("my-container-id"));
    }

    /**
     * Tests serialization with container set to an AutoCodeInterpreterToolParameter.
     */
    @Test
    public void testSerializationWithAutoCodeInterpreterToolParameter() throws IOException {
        AutoCodeInterpreterToolParameter autoParam
            = new AutoCodeInterpreterToolParameter().setFileIds(Arrays.asList("file-1", "file-2"))
                .setMemoryLimit(ContainerMemoryLimit.MEMORY_4GB);

        CodeInterpreterTool tool = new CodeInterpreterTool();
        tool.setContainer(autoParam);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"container\""));
        assertTrue(json.contains("\"type\":\"auto\""));
        assertTrue(json.contains("file-1"));
        assertTrue(json.contains("file-2"));
    }

    /**
     * Tests serialization with a minimal AutoCodeInterpreterToolParameter (no file IDs).
     */
    @Test
    public void testSerializationWithMinimalAutoCodeInterpreterToolParameter() throws IOException {
        AutoCodeInterpreterToolParameter autoParam = new AutoCodeInterpreterToolParameter();

        CodeInterpreterTool tool = new CodeInterpreterTool();
        tool.setContainer(autoParam);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"container\""));
        assertTrue(json.contains("\"type\":\"auto\""));
    }

    /**
     * Tests deserialization with container set to a string container ID.
     */
    @Test
    public void testDeserializationWithContainerIdString() throws IOException {
        String json = "{\"type\":\"code_interpreter\",\"container\":\"my-container-id\"}";

        CodeInterpreterTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertEquals(ToolType.CODE_INTERPRETER, tool.getType());
        assertNotNull(tool.getContainerAsString());
        assertTrue(tool.getContainerAsString().contains("my-container-id"));
    }

    /**
     * Tests deserialization with container set to an AutoCodeInterpreterToolParameter object.
     */
    @Test
    public void testDeserializationWithAutoCodeInterpreterToolParameter() throws IOException {
        String json
            = "{\"type\":\"code_interpreter\",\"container\":{\"type\":\"auto\",\"file_ids\":[\"file-1\",\"file-2\"]}}";

        CodeInterpreterTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertEquals(ToolType.CODE_INTERPRETER, tool.getType());
        assertNotNull(tool.getContainerAsAutoCodeInterpreterToolParameter());
        AutoCodeInterpreterToolParameter autoParam = tool.getContainerAsAutoCodeInterpreterToolParameter();
        assertEquals("auto", autoParam.getType());
        assertNotNull(autoParam.getFileIds());
        assertEquals(2, autoParam.getFileIds().size());
    }

    /**
     * Tests deserialization with container absent.
     */
    @Test
    public void testDeserializationWithoutContainer() throws IOException {
        String json = "{\"type\":\"code_interpreter\"}";

        CodeInterpreterTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNull(tool.getContainerAsString());
        assertNull(tool.getContainerAsAutoCodeInterpreterToolParameter());
    }

    /**
     * Tests round-trip serialization/deserialization with a container ID string.
     */
    @Test
    public void testRoundTripWithContainerIdString() throws IOException {
        CodeInterpreterTool original = new CodeInterpreterTool();
        original.setContainer("my-container-id");

        String json = serializeToJson(original);
        CodeInterpreterTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertNotNull(deserialized.getContainerAsString());
        assertTrue(deserialized.getContainerAsString().contains("my-container-id"));
    }

    /**
     * Tests round-trip serialization/deserialization with an AutoCodeInterpreterToolParameter.
     */
    @Test
    public void testRoundTripWithAutoCodeInterpreterToolParameter() throws IOException {
        AutoCodeInterpreterToolParameter autoParam
            = new AutoCodeInterpreterToolParameter().setFileIds(Arrays.asList("file-a", "file-b"));

        CodeInterpreterTool original = new CodeInterpreterTool();
        original.setContainer(autoParam);

        String json = serializeToJson(original);
        CodeInterpreterTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        AutoCodeInterpreterToolParameter deserializedParam = deserialized.getContainerAsAutoCodeInterpreterToolParameter();
        assertNotNull(deserializedParam);
        assertEquals("auto", deserializedParam.getType());
        assertEquals(2, deserializedParam.getFileIds().size());
    }

    /**
     * Regression: container string must not be double-quoted in serialized JSON.
     * Setting via setContainer(String) must produce "container":"value", never "container":"\"value\"".
     */
    @Test
    public void testContainerStringNoDoubleQuoting() throws IOException {
        CodeInterpreterTool tool = new CodeInterpreterTool();
        tool.setContainer("cntr-abc-123");

        String json = serializeToJson(tool);

        assertTrue(json.contains("\"container\":\"cntr-abc-123\""),
            "Container string must not be double-quoted, got: " + json);
        assertFalse(json.contains("\\\"cntr-abc-123\\\""),
            "Container string must not have escaped quotes, got: " + json);
    }

    /**
     * Regression: container string getter must return the plain string after round-trip,
     * not a JSON-encoded value with surrounding quotes.
     */
    @Test
    public void testContainerStringRoundTripNoExtraQuotes() throws IOException {
        CodeInterpreterTool original = new CodeInterpreterTool();
        original.setContainer("cntr-abc-123");

        String json = serializeToJson(original);
        CodeInterpreterTool deserialized = deserializeFromJson(json);

        assertEquals("cntr-abc-123", deserialized.getContainerAsString(),
            "Round-tripped container string must not have extra quotes");
    }

    /**
     * Regression: container string deserialized from JSON must return the plain string,
     * not a JSON-encoded value with surrounding quotes.
     */
    @Test
    public void testContainerStringDeserializationNoExtraQuotes() throws IOException {
        String json = "{\"type\":\"code_interpreter\",\"container\":\"my-id-456\"}";

        CodeInterpreterTool tool = deserializeFromJson(json);

        assertEquals("my-id-456", tool.getContainerAsString(),
            "Deserialized container string must not have extra quotes");
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(CodeInterpreterTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private CodeInterpreterTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return CodeInterpreterTool.fromJson(jsonReader);
        }
    }
}
