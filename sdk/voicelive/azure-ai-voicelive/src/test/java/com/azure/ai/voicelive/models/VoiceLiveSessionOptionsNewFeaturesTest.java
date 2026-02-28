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
 * - InterimResponse configuration (formerly FillerResponse)
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
    void testSetAndGetInterimResponse() {
        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions();
        StaticInterimResponseConfig interimConfig
            = new StaticInterimResponseConfig().setTexts(Arrays.asList("Please wait...", "One moment..."))
                .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY))
                .setLatencyThresholdMs(2000);
        BinaryData interimData = BinaryData.fromObject(interimConfig);

        // Act
        VoiceLiveSessionOptions result = options.setInterimResponse(interimData);

        // Assert
        assertSame(options, result);
        assertNotNull(options.getInterimResponse());
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
    void testInterimResponseJsonSerialization() {
        // Arrange
        LlmInterimResponseConfig interimConfig = new LlmInterimResponseConfig().setModel("gpt-4.1-mini")
            .setInstructions("Generate brief waiting messages")
            .setMaxCompletionTokens(50)
            .setTriggers(Arrays.asList(InterimResponseTrigger.TOOL))
            .setLatencyThresholdMs(1500);

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInterimResponse(BinaryData.fromObject(interimConfig));

        // Act
        BinaryData serialized = BinaryData.fromObject(options);
        VoiceLiveSessionOptions deserialized = serialized.toObject(VoiceLiveSessionOptions.class);

        // Assert
        assertEquals(options.getModel(), deserialized.getModel());
        assertNotNull(deserialized.getInterimResponse());
    }

    @Test
    void testMethodChainingWithNewFeatures() {
        // Arrange
        StaticInterimResponseConfig interimConfig
            = new StaticInterimResponseConfig().setTexts(Arrays.asList("Hold on..."))
                .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY, InterimResponseTrigger.TOOL));

        // Act
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("Test instructions")
            .setReasoningEffort(ReasoningEffort.LOW)
            .setInterimResponse(BinaryData.fromObject(interimConfig))
            .setTemperature(0.8);

        // Assert
        assertEquals("gpt-4o-realtime-preview", options.getModel());
        assertEquals("Test instructions", options.getInstructions());
        assertEquals(ReasoningEffort.LOW, options.getReasoningEffort());
        assertNotNull(options.getInterimResponse());
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
    void testVoiceLiveSessionResponseInterimResponse() {
        // Arrange
        String json
            = "{\"id\":\"session456\",\"interim_response\":{\"type\":\"static_interim_response\",\"texts\":[\"Wait...\"]}}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        VoiceLiveSessionResponse response = data.toObject(VoiceLiveSessionResponse.class);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getInterimResponse());
    }
}
