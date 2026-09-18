// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReasoningDedupSerializationTests {

    private static final String TEST_MODEL = "gpt-4o";

    // -----------------------------------------------------------------------
    // Reasoning on PromptAgentDefinition — getter / setter
    // -----------------------------------------------------------------------

    @Test
    public void testReasoningSetAndGet() {
        Reasoning reasoning = Reasoning.builder()
            .effort(ReasoningEffort.HIGH)
            .summary(Reasoning.Summary.CONCISE)
            .generateSummary(Reasoning.GenerateSummary.AUTO)
            .build();

        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setReasoning(reasoning);

        Reasoning result = definition.getReasoning();
        assertNotNull(result);
        assertEquals(ReasoningEffort.HIGH, result.effort().get());
        assertEquals(Reasoning.Summary.CONCISE, result.summary().get());
        assertEquals(Reasoning.GenerateSummary.AUTO, result.generateSummary().get());
    }

    @Test
    public void testReasoningNullSafe() {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL);
        assertNull(definition.getReasoning());

        definition.setReasoning(null);
        assertNull(definition.getReasoning());
    }

    // -----------------------------------------------------------------------
    // Reasoning on PromptAgentDefinition — serialization
    // -----------------------------------------------------------------------

    @Test
    public void testSerializationWithAllReasoningFields() throws IOException {
        Reasoning reasoning = Reasoning.builder()
            .effort(ReasoningEffort.MEDIUM)
            .summary(Reasoning.Summary.DETAILED)
            .generateSummary(Reasoning.GenerateSummary.CONCISE)
            .build();

        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setReasoning(reasoning);

        String json = serializeDefinition(definition);

        assertTrue(json.contains("\"reasoning\""));
        assertTrue(json.contains("\"effort\":\"medium\""));
        assertTrue(json.contains("\"summary\":\"detailed\""));
        assertTrue(json.contains("\"generate_summary\":\"concise\""));
    }

    @Test
    public void testSerializationWithReasoningEffortOnly() throws IOException {
        Reasoning reasoning = Reasoning.builder().effort(ReasoningEffort.HIGH).build();

        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setReasoning(reasoning);

        String json = serializeDefinition(definition);

        assertTrue(json.contains("\"effort\":\"high\""));
        // summary and generate_summary should not appear when not set
        assertFalse(json.contains("\"summary\""));
        assertFalse(json.contains("\"generate_summary\""));
    }

    @Test
    public void testSerializationWithoutReasoning() throws IOException {
        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL);

        String json = serializeDefinition(definition);

        assertFalse(json.contains("\"reasoning\""));
    }

    // -----------------------------------------------------------------------
    // Reasoning on PromptAgentDefinition — deserialization
    // -----------------------------------------------------------------------

    @Test
    public void testDeserializationWithAllReasoningFields() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"reasoning\":"
            + "{\"effort\":\"high\",\"summary\":\"concise\",\"generate_summary\":\"auto\"}}";

        PromptAgentDefinition definition = deserializeDefinition(json);

        assertNotNull(definition);
        assertEquals(TEST_MODEL, definition.getModel());

        Reasoning reasoning = definition.getReasoning();
        assertNotNull(reasoning);
        assertEquals(ReasoningEffort.HIGH, reasoning.effort().get());
        assertEquals(Reasoning.Summary.CONCISE, reasoning.summary().get());
        assertEquals(Reasoning.GenerateSummary.AUTO, reasoning.generateSummary().get());
    }

    @Test
    public void testDeserializationWithReasoningEffortOnly() throws IOException {
        String json = "{\"model\":\"gpt-4o\",\"reasoning\":{\"effort\":\"low\"}}";

        PromptAgentDefinition definition = deserializeDefinition(json);

        Reasoning reasoning = definition.getReasoning();
        assertNotNull(reasoning);
        assertEquals(ReasoningEffort.LOW, reasoning.effort().get());
        assertFalse(reasoning.summary().isPresent());
        assertFalse(reasoning.generateSummary().isPresent());
    }

    @Test
    public void testDeserializationWithoutReasoning() throws IOException {
        String json = "{\"model\":\"gpt-4o\"}";

        PromptAgentDefinition definition = deserializeDefinition(json);

        assertNotNull(definition);
        assertNull(definition.getReasoning());
    }

    // -----------------------------------------------------------------------
    // Reasoning on PromptAgentDefinition — round-trip
    // -----------------------------------------------------------------------

    @Test
    public void testRoundTripWithReasoning() throws IOException {
        Reasoning reasoning = Reasoning.builder()
            .effort(ReasoningEffort.LOW)
            .generateSummary(Reasoning.GenerateSummary.DETAILED)
            .build();

        PromptAgentDefinition original
            = new PromptAgentDefinition(TEST_MODEL).setInstructions("Test").setTemperature(0.7).setReasoning(reasoning);

        String json = serializeDefinition(original);
        PromptAgentDefinition deserialized = deserializeDefinition(json);

        assertEquals(original.getModel(), deserialized.getModel());
        assertEquals(original.getInstructions(), deserialized.getInstructions());
        assertEquals(original.getTemperature(), deserialized.getTemperature());

        Reasoning result = deserialized.getReasoning();
        assertNotNull(result);
        assertEquals(ReasoningEffort.LOW, result.effort().get());
        assertEquals(Reasoning.GenerateSummary.DETAILED, result.generateSummary().get());

        // JSON stability: re-serialize and compare
        String reserializedJson = serializeDefinition(deserialized);
        assertEquals(json, reserializedJson, "JSON should be identical after round-trip");
    }

    @Test
    public void testRoundTripWithAllReasoningEffortValues() throws IOException {
        ReasoningEffort[] efforts = {
            ReasoningEffort.NONE,
            ReasoningEffort.MINIMAL,
            ReasoningEffort.LOW,
            ReasoningEffort.MEDIUM,
            ReasoningEffort.HIGH,
            ReasoningEffort.XHIGH };

        for (ReasoningEffort effort : efforts) {
            Reasoning reasoning = Reasoning.builder().effort(effort).build();
            PromptAgentDefinition original = new PromptAgentDefinition(TEST_MODEL).setReasoning(reasoning);

            String json = serializeDefinition(original);
            PromptAgentDefinition deserialized = deserializeDefinition(json);

            assertEquals(effort, deserialized.getReasoning().effort().get(), "Round-trip failed for effort: " + effort);

            // JSON stability
            assertEquals(json, serializeDefinition(deserialized), "JSON stability failed for effort: " + effort);
        }
    }

    // -----------------------------------------------------------------------
    // PromptAgentDefinition with reasoning + tools combined
    // -----------------------------------------------------------------------

    @Test
    public void testSerializationWithReasoningAndTools() throws IOException {
        Reasoning reasoning = Reasoning.builder().effort(ReasoningEffort.HIGH).build();
        FileSearchTool fileSearch = new FileSearchTool(Collections.singletonList("vs_001"));

        PromptAgentDefinition definition = new PromptAgentDefinition(TEST_MODEL).setReasoning(reasoning)
            .setTools(Collections.singletonList(fileSearch));

        String json = serializeDefinition(definition);

        assertTrue(json.contains("\"effort\":\"high\""));
        assertTrue(json.contains("\"vector_store_ids\":[\"vs_001\"]"));
        assertTrue(json.contains("\"type\":\"file_search\""));
    }

    // -----------------------------------------------------------------------
    // ComparisonFilter on FileSearchTool — typed setter
    // -----------------------------------------------------------------------

    @Test
    public void testComparisonFilterSetterSerializes() throws IOException {
        com.openai.models.ComparisonFilter filter = com.openai.models.ComparisonFilter.builder()
            .key("author")
            .type(com.openai.models.ComparisonFilter.Type.EQ)
            .value(com.openai.models.ComparisonFilter.Value.ofString("john"))
            .build();

        FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs_123")).setComparisonFilter(filter);

        String json = serializeTool(tool);

        assertTrue(json.contains("\"key\":\"author\""), "Missing key, got: " + json);
        assertTrue(json.contains("\"value\":\"john\""), "Missing value, got: " + json);
    }

    @Test
    public void testComparisonFilterRoundTrip() throws IOException {
        com.openai.models.ComparisonFilter filter = com.openai.models.ComparisonFilter.builder()
            .key("category")
            .type(com.openai.models.ComparisonFilter.Type.NE)
            .value(com.openai.models.ComparisonFilter.Value.ofNumber(42.0))
            .build();

        FileSearchTool original = new FileSearchTool(Collections.singletonList("vs_abc")).setComparisonFilter(filter);

        String json = serializeTool(original);

        // Deserialize and verify filters survived
        FileSearchTool deserialized = deserializeTool(json);
        assertNotNull(deserialized.getFilters(), "Filters should survive round-trip");
        assertTrue(deserialized.getFilters().toString().contains("category"), "Filter key should survive round-trip");

        // JSON stability
        String reserializedJson = serializeTool(deserialized);
        assertEquals(json, reserializedJson, "JSON should be identical after round-trip");
    }

    // -----------------------------------------------------------------------
    // CompoundFilter on FileSearchTool — typed setter
    // -----------------------------------------------------------------------

    @Test
    public void testCompoundFilterSetterSerializes() throws IOException {
        com.openai.models.CompoundFilter filter = com.openai.models.CompoundFilter.builder()
            .type(com.openai.models.CompoundFilter.Type.OR)
            .addFilter(com.openai.models.CompoundFilter.Filter.ofComparison(com.openai.models.ComparisonFilter.builder()
                .key("tag")
                .type(com.openai.models.ComparisonFilter.Type.EQ)
                .value(com.openai.models.ComparisonFilter.Value.ofString("ai"))
                .build()))
            .addFilter(com.openai.models.CompoundFilter.Filter.ofComparison(com.openai.models.ComparisonFilter.builder()
                .key("tag")
                .type(com.openai.models.ComparisonFilter.Type.EQ)
                .value(com.openai.models.ComparisonFilter.Value.ofString("ml"))
                .build()))
            .build();

        FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs_456")).setCompoundFilter(filter);

        String json = serializeTool(tool);

        assertTrue(json.contains("\"key\":\"tag\""), "Missing key, got: " + json);
        assertTrue(json.contains("\"ai\""), "Missing first filter value, got: " + json);
        assertTrue(json.contains("\"ml\""), "Missing second filter value, got: " + json);
    }

    @Test
    public void testCompoundFilterRoundTrip() throws IOException {
        com.openai.models.CompoundFilter filter = com.openai.models.CompoundFilter.builder()
            .type(com.openai.models.CompoundFilter.Type.AND)
            .addFilter(com.openai.models.CompoundFilter.Filter.ofComparison(com.openai.models.ComparisonFilter.builder()
                .key("status")
                .type(com.openai.models.ComparisonFilter.Type.EQ)
                .value(com.openai.models.ComparisonFilter.Value.ofString("published"))
                .build()))
            .build();

        FileSearchTool original = new FileSearchTool(Collections.singletonList("vs_789")).setCompoundFilter(filter);

        String json = serializeTool(original);
        FileSearchTool deserialized = deserializeTool(json);

        assertNotNull(deserialized.getFilters());
        assertTrue(deserialized.getFilters().toString().contains("status"));

        // JSON stability
        String reserializedJson = serializeTool(deserialized);
        assertEquals(json, reserializedJson, "JSON should be identical after round-trip");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String serializeDefinition(PromptAgentDefinition definition) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(out)) {
            definition.toJson(writer);
        }
        return out.toString("UTF-8");
    }

    private PromptAgentDefinition deserializeDefinition(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return PromptAgentDefinition.fromJson(reader);
        }
    }

    private String serializeTool(FileSearchTool tool) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(out)) {
            tool.toJson(writer);
        }
        return out.toString("UTF-8");
    }

    private FileSearchTool deserializeTool(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return FileSearchTool.fromJson(reader);
        }
    }
}
