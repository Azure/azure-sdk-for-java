// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for interim response configuration classes:
 * {@link InterimResponseConfigBase}, {@link StaticInterimResponseConfig}, {@link LlmInterimResponseConfig}.
 */
class InterimResponseConfigTest {

    @Test
    void testInterimResponseConfigTypeValues() {
        // Assert all known values exist
        assertNotNull(InterimResponseConfigType.STATIC_INTERIM_RESPONSE);
        assertNotNull(InterimResponseConfigType.LLM_INTERIM_RESPONSE);

        assertEquals("static_interim_response", InterimResponseConfigType.STATIC_INTERIM_RESPONSE.toString());
        assertEquals("llm_interim_response", InterimResponseConfigType.LLM_INTERIM_RESPONSE.toString());
    }

    @Test
    void testInterimResponseConfigTypeFromString() {
        // Act & Assert
        assertEquals(InterimResponseConfigType.STATIC_INTERIM_RESPONSE,
            InterimResponseConfigType.fromString("static_interim_response"));
        assertEquals(InterimResponseConfigType.LLM_INTERIM_RESPONSE,
            InterimResponseConfigType.fromString("llm_interim_response"));
    }

    @Test
    void testInterimResponseTriggerValues() {
        // Assert all known values exist
        assertNotNull(InterimResponseTrigger.LATENCY);
        assertNotNull(InterimResponseTrigger.TOOL);

        assertEquals("latency", InterimResponseTrigger.LATENCY.toString());
        assertEquals("tool", InterimResponseTrigger.TOOL.toString());
    }

    @Test
    void testInterimResponseTriggerFromString() {
        // Act & Assert
        assertEquals(InterimResponseTrigger.LATENCY, InterimResponseTrigger.fromString("latency"));
        assertEquals(InterimResponseTrigger.TOOL, InterimResponseTrigger.fromString("tool"));
    }

    @Test
    void testStaticInterimResponseConfigCreation() {
        // Arrange & Act
        StaticInterimResponseConfig config = new StaticInterimResponseConfig();

        // Assert
        assertNotNull(config);
        assertEquals(InterimResponseConfigType.STATIC_INTERIM_RESPONSE, config.getType());
    }

    @Test
    void testStaticInterimResponseConfigWithAllProperties() {
        // Arrange
        List<String> texts = Arrays.asList("Please wait...", "One moment please...", "Let me check...");
        List<InterimResponseTrigger> triggers
            = Arrays.asList(InterimResponseTrigger.LATENCY, InterimResponseTrigger.TOOL);

        // Act
        StaticInterimResponseConfig config
            = new StaticInterimResponseConfig().setTexts(texts).setTriggers(triggers).setLatencyThresholdMs(3000);

        // Assert
        assertEquals(texts, config.getTexts());
        assertEquals(triggers, config.getTriggers());
        assertEquals(3000, config.getLatencyThresholdMs());
        assertEquals(InterimResponseConfigType.STATIC_INTERIM_RESPONSE, config.getType());
    }

    @Test
    void testStaticInterimResponseConfigJsonSerialization() {
        // Arrange
        StaticInterimResponseConfig config
            = new StaticInterimResponseConfig().setTexts(Arrays.asList("Hold on...", "Processing..."))
                .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY))
                .setLatencyThresholdMs(2500);

        // Act
        BinaryData serialized = BinaryData.fromObject(config);
        StaticInterimResponseConfig deserialized = serialized.toObject(StaticInterimResponseConfig.class);

        // Assert
        assertEquals(config.getTexts(), deserialized.getTexts());
        assertEquals(config.getTriggers(), deserialized.getTriggers());
        assertEquals(config.getLatencyThresholdMs(), deserialized.getLatencyThresholdMs());
        assertEquals(InterimResponseConfigType.STATIC_INTERIM_RESPONSE, deserialized.getType());
    }

    @Test
    void testLlmInterimResponseConfigCreation() {
        // Arrange & Act
        LlmInterimResponseConfig config = new LlmInterimResponseConfig();

        // Assert
        assertNotNull(config);
        assertEquals(InterimResponseConfigType.LLM_INTERIM_RESPONSE, config.getType());
    }

    @Test
    void testLlmInterimResponseConfigWithAllProperties() {
        // Arrange
        List<InterimResponseTrigger> triggers = Arrays.asList(InterimResponseTrigger.TOOL);

        // Act
        LlmInterimResponseConfig config = new LlmInterimResponseConfig().setModel("gpt-4.1-mini")
            .setInstructions("Generate brief waiting messages")
            .setMaxCompletionTokens(50)
            .setTriggers(triggers)
            .setLatencyThresholdMs(1500);

        // Assert
        assertEquals("gpt-4.1-mini", config.getModel());
        assertEquals("Generate brief waiting messages", config.getInstructions());
        assertEquals(50, config.getMaxCompletionTokens());
        assertEquals(triggers, config.getTriggers());
        assertEquals(1500, config.getLatencyThresholdMs());
        assertEquals(InterimResponseConfigType.LLM_INTERIM_RESPONSE, config.getType());
    }

    @Test
    void testLlmInterimResponseConfigJsonSerialization() {
        // Arrange
        LlmInterimResponseConfig config = new LlmInterimResponseConfig().setModel("test-model")
            .setInstructions("Test instructions")
            .setMaxCompletionTokens(100)
            .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY, InterimResponseTrigger.TOOL))
            .setLatencyThresholdMs(2000);

        // Act
        BinaryData serialized = BinaryData.fromObject(config);
        LlmInterimResponseConfig deserialized = serialized.toObject(LlmInterimResponseConfig.class);

        // Assert
        assertEquals(config.getModel(), deserialized.getModel());
        assertEquals(config.getInstructions(), deserialized.getInstructions());
        assertEquals(config.getMaxCompletionTokens(), deserialized.getMaxCompletionTokens());
        assertEquals(config.getTriggers(), deserialized.getTriggers());
        assertEquals(config.getLatencyThresholdMs(), deserialized.getLatencyThresholdMs());
        assertEquals(InterimResponseConfigType.LLM_INTERIM_RESPONSE, deserialized.getType());
    }

    @Test
    void testStaticInterimResponseConfigJsonDeserialization() {
        // Arrange
        String json = "{\"type\":\"static_interim_response\",\"texts\":[\"Wait...\",\"Hold on...\"],"
            + "\"triggers\":[\"latency\"],\"latency_threshold_ms\":2000}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        StaticInterimResponseConfig config = data.toObject(StaticInterimResponseConfig.class);

        // Assert
        assertNotNull(config);
        assertEquals(2, config.getTexts().size());
        assertEquals("Wait...", config.getTexts().get(0));
        assertEquals("Hold on...", config.getTexts().get(1));
        assertEquals(1, config.getTriggers().size());
        assertEquals(InterimResponseTrigger.LATENCY, config.getTriggers().get(0));
        assertEquals(2000, config.getLatencyThresholdMs());
    }

    @Test
    void testLlmInterimResponseConfigJsonDeserialization() {
        // Arrange
        String json = "{\"type\":\"llm_interim_response\",\"model\":\"gpt-4\",\"instructions\":\"Be brief\","
            + "\"max_completion_tokens\":25,\"triggers\":[\"tool\"],\"latency_threshold_ms\":3000}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        LlmInterimResponseConfig config = data.toObject(LlmInterimResponseConfig.class);

        // Assert
        assertNotNull(config);
        assertEquals("gpt-4", config.getModel());
        assertEquals("Be brief", config.getInstructions());
        assertEquals(25, config.getMaxCompletionTokens());
        assertEquals(1, config.getTriggers().size());
        assertEquals(InterimResponseTrigger.TOOL, config.getTriggers().get(0));
        assertEquals(3000, config.getLatencyThresholdMs());
    }
}
