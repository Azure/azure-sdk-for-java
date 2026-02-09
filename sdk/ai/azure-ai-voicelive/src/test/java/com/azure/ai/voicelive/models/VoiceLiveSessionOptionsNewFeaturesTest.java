// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for new session options features:
 * - ReasoningEffort configuration
 * - FillerResponse configuration
 */
class VoiceLiveSessionOptionsNewFeaturesTest {

    @Test
    void testSetAndGetReasoningEffort() {
        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();

        // Act
        VoiceLiveSessionOptions result = options.setReasoningEffort(ReasoningEffort.MEDIUM);

        // Assert
        assertSame(options, result);
        assertEquals(ReasoningEffort.MEDIUM, options.getReasoningEffort());
    }

    @Test
    void testSetReasoningEffortAllValues() {
        // Test all reasoning effort values
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();

        for (ReasoningEffort effort : ReasoningEffort.values()) {
            options.setReasoningEffort(effort);
            assertEquals(effort, options.getReasoningEffort());
        }
    }

    @Test
    void testSetAndGetFillerResponse() {
        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();
        BasicFillerResponseConfig fillerConfig
            = new BasicFillerResponseConfig().setTexts(Arrays.asList("Please wait...", "One moment..."))
                .setTriggers(Arrays.asList(FillerTrigger.LATENCY))
                .setLatencyThresholdMs(2000);
        BinaryData fillerData = BinaryData.fromObject(fillerConfig);

        // Act
        VoiceLiveSessionOptions result = options.setFillerResponse(fillerData);

        // Assert
        assertSame(options, result);
        assertNotNull(options.getFillerResponse());
    }

    @Test
    void testReasoningEffortJsonSerialization() {
        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setReasoningEffort(ReasoningEffort.HIGH);

        // Act
        BinaryData serialized = BinaryData.fromObject(options);
        VoiceLiveSessionOptions deserialized = serialized.toObject(VoiceLiveSessionOptions.class);

        // Assert
        assertEquals(options.getModel(), deserialized.getModel());
        assertEquals(options.getReasoningEffort(), deserialized.getReasoningEffort());
    }

    @Test
    void testFillerResponseJsonSerialization() {
        // Arrange
        LlmFillerResponseConfig fillerConfig = new LlmFillerResponseConfig().setModel("gpt-4.1-mini")
            .setInstructions("Generate brief waiting messages")
            .setMaxCompletionTokens(50)
            .setTriggers(Arrays.asList(FillerTrigger.TOOL))
            .setLatencyThresholdMs(1500);

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setFillerResponse(BinaryData.fromObject(fillerConfig));

        // Act
        BinaryData serialized = BinaryData.fromObject(options);
        VoiceLiveSessionOptions deserialized = serialized.toObject(VoiceLiveSessionOptions.class);

        // Assert
        assertEquals(options.getModel(), deserialized.getModel());
        assertNotNull(deserialized.getFillerResponse());
    }

    @Test
    void testMethodChainingWithNewFeatures() {
        // Arrange
        BasicFillerResponseConfig fillerConfig = new BasicFillerResponseConfig().setTexts(Arrays.asList("Hold on..."))
            .setTriggers(Arrays.asList(FillerTrigger.LATENCY, FillerTrigger.TOOL));

        // Act
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("Test instructions")
            .setReasoningEffort(ReasoningEffort.LOW)
            .setFillerResponse(BinaryData.fromObject(fillerConfig))
            .setTemperature(0.8);

        // Assert
        assertEquals("gpt-4o-realtime-preview", options.getModel());
        assertEquals("Test instructions", options.getInstructions());
        assertEquals(ReasoningEffort.LOW, options.getReasoningEffort());
        assertNotNull(options.getFillerResponse());
        assertEquals(0.8, options.getTemperature());
    }

    @Test
    void testVoiceLiveSessionResponseReasoningEffort() {
        // Arrange
        String json = "{\"id\":\"session123\",\"reasoning_effort\":\"high\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        VoiceLiveSessionResponse response = data.toObject(VoiceLiveSessionResponse.class);

        // Assert
        assertNotNull(response);
        assertEquals(ReasoningEffort.HIGH, response.getReasoningEffort());
    }

    @Test
    void testVoiceLiveSessionResponseFillerResponse() {
        // Arrange
        String json
            = "{\"id\":\"session456\",\"filler_response\":{\"type\":\"static_filler\",\"texts\":[\"Wait...\"]}}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        VoiceLiveSessionResponse response = data.toObject(VoiceLiveSessionResponse.class);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getFillerResponse());
    }
}
