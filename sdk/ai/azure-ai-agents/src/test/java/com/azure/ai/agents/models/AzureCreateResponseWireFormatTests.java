// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.openai.core.JsonValue;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that validate the wire format of {@link AzureCreateResponseOptions} when flattened via
 * {@link OpenAIJsonHelper#toJsonValueMap}. The goal is to confirm that properties like
 * {@code agent_reference} and {@code structured_inputs} appear as separate top-level
 * entries in the additionalBodyProperties map (matching the @@copyProperties wire format),
 * rather than nested under a single key.
 */
public class AzureCreateResponseWireFormatTests {

    /**
     * Verifies that AzureCreateResponseOptions serializes to JSON with correct wire-format property names
     * (snake_case) via its toJson method.
     */
    @Test
    public void testAzureCreateResponseOptionsSerialization() throws IOException {
        AzureCreateResponseOptions createResponse
            = new AzureCreateResponseOptions().setAgentReference(new AgentReference("test-agent").setVersion("1"))
                .setStructuredInputs(buildStructuredInputs());

        String json = serializeToJson(createResponse);

        assertNotNull(json);
        assertTrue(json.contains("\"agent_reference\""), "Should use wire name 'agent_reference'");
        assertTrue(json.contains("\"structured_inputs\""), "Should use wire name 'structured_inputs'");
        assertTrue(json.contains("\"type\":\"agent_reference\""));
        assertTrue(json.contains("\"name\":\"test-agent\""));
        assertTrue(json.contains("\"version\":\"1\""));
        assertTrue(json.contains("\"userName\":\"Alice Smith\""));
        assertTrue(json.contains("\"userRole\":\"Senior Developer\""));
        // Should NOT contain camelCase Java field names
        assertFalse(json.contains("\"agentReference\""), "Should not use Java field name 'agentReference'");
        assertFalse(json.contains("\"structuredInputs\""), "Should not use Java field name 'structuredInputs'");
    }

    /**
     * Verifies that toJsonValueMap produces a flat map with exactly the expected top-level keys,
     * matching the wire format required by @@copyProperties.
     */
    @Test
    public void testToJsonValueMapProducesFlatKeys() {
        AzureCreateResponseOptions createResponse
            = new AzureCreateResponseOptions().setAgentReference(new AgentReference("my-agent").setVersion("2"))
                .setStructuredInputs(buildStructuredInputs());

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);

        assertNotNull(flatMap);
        assertTrue(flatMap.containsKey("agent_reference"),
            "Flat map should contain 'agent_reference' as a top-level key");
        assertTrue(flatMap.containsKey("structured_inputs"),
            "Flat map should contain 'structured_inputs' as a top-level key");
        assertEquals(2, flatMap.size(), "Flat map should have exactly 2 entries");

        // Should NOT have nested key like "azure_create_response"
        assertFalse(flatMap.containsKey("azure_create_response"),
            "Properties should be flattened, not nested under 'azure_create_response'");
    }

    /**
     * Verifies that the agent_reference JsonValue has the correct internal structure.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAgentReferenceJsonValueStructure() {
        AzureCreateResponseOptions createResponse = new AzureCreateResponseOptions()
            .setAgentReference(new AgentReference("structured-input-agent").setVersion("3"));

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);
        JsonValue agentRefValue = flatMap.get("agent_reference");

        assertNotNull(agentRefValue, "agent_reference should be present");
        // The JsonValue should be an object (map) type
        assertTrue(agentRefValue.asObject().isPresent(), "agent_reference should be a JSON object");

        Map<String, JsonValue> agentRefMap = (Map<String, JsonValue>) agentRefValue.asObject().get();
        assertTrue(agentRefMap.containsKey("type"));
        assertTrue(agentRefMap.containsKey("name"));
        assertTrue(agentRefMap.containsKey("version"));

        assertEquals("agent_reference", agentRefMap.get("type").asString().orElse(null));
        assertEquals("structured-input-agent", agentRefMap.get("name").asString().orElse(null));
        assertEquals("3", agentRefMap.get("version").asString().orElse(null));
    }

    /**
     * Verifies that structured_inputs JsonValue has the correct key-value structure.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStructuredInputsJsonValueStructure() {
        AzureCreateResponseOptions createResponse
            = new AzureCreateResponseOptions().setStructuredInputs(buildStructuredInputs());

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);
        JsonValue structuredInputsValue = flatMap.get("structured_inputs");

        assertNotNull(structuredInputsValue, "structured_inputs should be present");
        assertTrue(structuredInputsValue.asObject().isPresent(), "structured_inputs should be a JSON object");

        Map<String, JsonValue> inputsMap = (Map<String, JsonValue>) structuredInputsValue.asObject().get();
        assertEquals("Alice Smith", inputsMap.get("userName").asString().orElse(null));
        assertEquals("Senior Developer", inputsMap.get("userRole").asString().orElse(null));
    }

    /**
     * Verifies that toJsonValueMap handles null fields gracefully (omitting them from the map).
     */
    @Test
    public void testToJsonValueMapWithOnlyAgentReference() {
        AzureCreateResponseOptions createResponse
            = new AzureCreateResponseOptions().setAgentReference(new AgentReference("agent-only"));

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);

        assertTrue(flatMap.containsKey("agent_reference"));
        assertFalse(flatMap.containsKey("structured_inputs"), "structured_inputs should be absent when not set");
    }

    /**
     * Verifies that toJsonValueMap handles null fields gracefully (omitting them from the map).
     */
    @Test
    public void testToJsonValueMapWithOnlyStructuredInputs() {
        AzureCreateResponseOptions createResponse
            = new AzureCreateResponseOptions().setStructuredInputs(buildStructuredInputs());

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);

        assertTrue(flatMap.containsKey("structured_inputs"));
        assertFalse(flatMap.containsKey("agent_reference"), "agent_reference should be absent when not set");
    }

    /**
     * Verifies that toJsonValueMap returns an empty map for a default (empty) AzureCreateResponseOptions.
     */
    @Test
    public void testToJsonValueMapWithEmptyObject() {
        AzureCreateResponseOptions createResponse = new AzureCreateResponseOptions();

        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(createResponse);

        assertNotNull(flatMap);
        assertTrue(flatMap.isEmpty(), "Empty AzureCreateResponseOptions should produce an empty map");
    }

    /**
     * Verifies that toJsonValueMap returns an empty map when passed null.
     */
    @Test
    public void testToJsonValueMapWithNull() {
        Map<String, JsonValue> flatMap = OpenAIJsonHelper.toJsonValueMap(null);

        assertNotNull(flatMap);
        assertTrue(flatMap.isEmpty(), "null input should produce an empty map");
    }

    // --- Helpers ---

    private Map<String, BinaryData> buildStructuredInputs() {
        Map<String, BinaryData> inputs = new LinkedHashMap<>();
        inputs.put("userName", BinaryData.fromObject("Alice Smith"));
        inputs.put("userRole", BinaryData.fromObject("Senior Developer"));
        return inputs;
    }

    private String serializeToJson(AzureCreateResponseOptions createResponse) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            createResponse.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }
}
