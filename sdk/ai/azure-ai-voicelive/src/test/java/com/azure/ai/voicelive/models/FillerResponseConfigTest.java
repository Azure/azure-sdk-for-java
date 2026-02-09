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
 * Unit tests for filler response configuration classes:
 * {@link FillerResponseConfigBase}, {@link BasicFillerResponseConfig}, {@link LlmFillerResponseConfig}.
 */
class FillerResponseConfigTest {

    @Test
    void testFillerResponseConfigTypeValues() {
        // Assert all known values exist
        assertNotNull(FillerResponseConfigType.STATIC_FILLER);
        assertNotNull(FillerResponseConfigType.LLM_FILLER);

        assertEquals("static_filler", FillerResponseConfigType.STATIC_FILLER.toString());
        assertEquals("llm_filler", FillerResponseConfigType.LLM_FILLER.toString());
    }

    @Test
    void testFillerResponseConfigTypeFromString() {
        // Act & Assert
        assertEquals(FillerResponseConfigType.STATIC_FILLER, FillerResponseConfigType.fromString("static_filler"));
        assertEquals(FillerResponseConfigType.LLM_FILLER, FillerResponseConfigType.fromString("llm_filler"));
    }

    @Test
    void testFillerTriggerValues() {
        // Assert all known values exist
        assertNotNull(FillerTrigger.LATENCY);
        assertNotNull(FillerTrigger.TOOL);

        assertEquals("latency", FillerTrigger.LATENCY.toString());
        assertEquals("tool", FillerTrigger.TOOL.toString());
    }

    @Test
    void testFillerTriggerFromString() {
        // Act & Assert
        assertEquals(FillerTrigger.LATENCY, FillerTrigger.fromString("latency"));
        assertEquals(FillerTrigger.TOOL, FillerTrigger.fromString("tool"));
    }

    @Test
    void testBasicFillerResponseConfigCreation() {
        // Arrange & Act
        BasicFillerResponseConfig config = new BasicFillerResponseConfig();

        // Assert
        assertNotNull(config);
        assertEquals(FillerResponseConfigType.STATIC_FILLER, config.getType());
    }

    @Test
    void testBasicFillerResponseConfigWithAllProperties() {
        // Arrange
        List<String> texts = Arrays.asList("Please wait...", "One moment please...", "Let me check...");
        List<FillerTrigger> triggers = Arrays.asList(FillerTrigger.LATENCY, FillerTrigger.TOOL);

        // Act
        BasicFillerResponseConfig config
            = new BasicFillerResponseConfig().setTexts(texts).setTriggers(triggers).setLatencyThresholdMs(3000);

        // Assert
        assertEquals(texts, config.getTexts());
        assertEquals(triggers, config.getTriggers());
        assertEquals(3000, config.getLatencyThresholdMs());
        assertEquals(FillerResponseConfigType.STATIC_FILLER, config.getType());
    }

    @Test
    void testBasicFillerResponseConfigJsonSerialization() {
        // Arrange
        BasicFillerResponseConfig config
            = new BasicFillerResponseConfig().setTexts(Arrays.asList("Hold on...", "Processing..."))
                .setTriggers(Arrays.asList(FillerTrigger.LATENCY))
                .setLatencyThresholdMs(2500);

        // Act
        BinaryData serialized = BinaryData.fromObject(config);
        BasicFillerResponseConfig deserialized = serialized.toObject(BasicFillerResponseConfig.class);

        // Assert
        assertEquals(config.getTexts(), deserialized.getTexts());
        assertEquals(config.getTriggers(), deserialized.getTriggers());
        assertEquals(config.getLatencyThresholdMs(), deserialized.getLatencyThresholdMs());
        assertEquals(FillerResponseConfigType.STATIC_FILLER, deserialized.getType());
    }

    @Test
    void testLlmFillerResponseConfigCreation() {
        // Arrange & Act
        LlmFillerResponseConfig config = new LlmFillerResponseConfig();

        // Assert
        assertNotNull(config);
        assertEquals(FillerResponseConfigType.LLM_FILLER, config.getType());
    }

    @Test
    void testLlmFillerResponseConfigWithAllProperties() {
        // Arrange
        List<FillerTrigger> triggers = Arrays.asList(FillerTrigger.TOOL);

        // Act
        LlmFillerResponseConfig config = new LlmFillerResponseConfig().setModel("gpt-4.1-mini")
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
        assertEquals(FillerResponseConfigType.LLM_FILLER, config.getType());
    }

    @Test
    void testLlmFillerResponseConfigJsonSerialization() {
        // Arrange
        LlmFillerResponseConfig config = new LlmFillerResponseConfig().setModel("test-model")
            .setInstructions("Test instructions")
            .setMaxCompletionTokens(100)
            .setTriggers(Arrays.asList(FillerTrigger.LATENCY, FillerTrigger.TOOL))
            .setLatencyThresholdMs(2000);

        // Act
        BinaryData serialized = BinaryData.fromObject(config);
        LlmFillerResponseConfig deserialized = serialized.toObject(LlmFillerResponseConfig.class);

        // Assert
        assertEquals(config.getModel(), deserialized.getModel());
        assertEquals(config.getInstructions(), deserialized.getInstructions());
        assertEquals(config.getMaxCompletionTokens(), deserialized.getMaxCompletionTokens());
        assertEquals(config.getTriggers(), deserialized.getTriggers());
        assertEquals(config.getLatencyThresholdMs(), deserialized.getLatencyThresholdMs());
        assertEquals(FillerResponseConfigType.LLM_FILLER, deserialized.getType());
    }

    @Test
    void testBasicFillerResponseConfigJsonDeserialization() {
        // Arrange
        String json = "{\"type\":\"static_filler\",\"texts\":[\"Wait...\",\"Hold on...\"],"
            + "\"triggers\":[\"latency\"],\"latency_threshold_ms\":2000}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        BasicFillerResponseConfig config = data.toObject(BasicFillerResponseConfig.class);

        // Assert
        assertNotNull(config);
        assertEquals(2, config.getTexts().size());
        assertEquals("Wait...", config.getTexts().get(0));
        assertEquals("Hold on...", config.getTexts().get(1));
        assertEquals(1, config.getTriggers().size());
        assertEquals(FillerTrigger.LATENCY, config.getTriggers().get(0));
        assertEquals(2000, config.getLatencyThresholdMs());
    }

    @Test
    void testLlmFillerResponseConfigJsonDeserialization() {
        // Arrange
        String json = "{\"type\":\"llm_filler\",\"model\":\"gpt-4\",\"instructions\":\"Be brief\","
            + "\"max_completion_tokens\":25,\"triggers\":[\"tool\"],\"latency_threshold_ms\":3000}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        LlmFillerResponseConfig config = data.toObject(LlmFillerResponseConfig.class);

        // Assert
        assertNotNull(config);
        assertEquals("gpt-4", config.getModel());
        assertEquals("Be brief", config.getInstructions());
        assertEquals(25, config.getMaxCompletionTokens());
        assertEquals(1, config.getTriggers().size());
        assertEquals(FillerTrigger.TOOL, config.getTriggers().get(0));
        assertEquals(3000, config.getLatencyThresholdMs());
    }
}
