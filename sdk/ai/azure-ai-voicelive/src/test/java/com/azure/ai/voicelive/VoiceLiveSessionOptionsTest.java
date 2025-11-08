// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link VoiceLiveSessionOptions}.
 */
@ExtendWith(MockitoExtension.class)
class VoiceLiveSessionOptionsTest {

    private VoiceLiveSessionOptions sessionOptions;
    private static final String TEST_MODEL = "gpt-4o-realtime-preview";

    @BeforeEach
    void setUp() {
        sessionOptions = new VoiceLiveSessionOptions().setModel(TEST_MODEL);
    }

    @Test
    void testConstructorWithValidModel() {
        // Act & Assert
        assertNotNull(sessionOptions);
        assertEquals(TEST_MODEL, sessionOptions.getModel());
    }

    @Test
    void testSetAndGetInstructions() {
        // Arrange
        String instructions = "You are a helpful AI assistant.";

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setInstructions(instructions);

        // Assert
        assertSame(sessionOptions, result); // Should return same instance for chaining
        assertEquals(instructions, sessionOptions.getInstructions());
    }

    @Test
    void testSetInstructionsWithNull() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            sessionOptions.setInstructions(null);
        });
        assertNull(sessionOptions.getInstructions());
    }

    @Test
    void testSetAndGetVoice() {
        // Arrange
        OpenAIVoice voice = new OpenAIVoice(OpenAIVoiceName.ALLOY);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setVoice(voice);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(voice, sessionOptions.getVoice());
    }

    @Test
    void testSetAndGetInputAudioFormat() {
        // Arrange
        InputAudioFormat format = InputAudioFormat.PCM16;

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setInputAudioFormat(format);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(format, sessionOptions.getInputAudioFormat());
    }

    @Test
    void testSetAndGetOutputAudioFormat() {
        // Arrange
        OutputAudioFormat format = OutputAudioFormat.PCM16;

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setOutputAudioFormat(format);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(format, sessionOptions.getOutputAudioFormat());
    }

    @Test
    void testSetAndGetModalities() {
        // Arrange
        List<InteractionModality> modalities = Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setModalities(modalities);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(modalities, sessionOptions.getModalities());
    }

    @Test
    void testSetModalitiesWithNull() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            sessionOptions.setModalities(null);
        });
        assertNull(sessionOptions.getModalities());
    }

    @Test
    void testSetAndGetTurnDetection() {
        // Arrange
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection();
        turnDetection.setThreshold(0.5);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setTurnDetection(turnDetection);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(turnDetection, sessionOptions.getTurnDetection());
    }

    @Test
    void testSetAndGetTools() {
        // Arrange
        VoiceLiveFunctionDefinition tool = new VoiceLiveFunctionDefinition("test_function");
        List<VoiceLiveToolDefinition> tools = Arrays.asList(tool);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setTools(tools);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(tools, sessionOptions.getTools());
    }

    @Test
    void testSetAndGetToolChoice() {
        // Arrange
        ToolChoice toolChoice = ToolChoice.fromLiteral(ToolChoiceLiteral.AUTO);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setToolChoice(toolChoice);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(toolChoice, sessionOptions.getToolChoice());
    }

    @Test
    void testSetAndGetTemperature() {
        // Arrange
        Double temperature = 0.7;

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setTemperature(temperature);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(temperature, sessionOptions.getTemperature());
    }

    @Test
    void testSetTemperatureWithValidRange() {
        // Test boundary values
        assertDoesNotThrow(() -> {
            sessionOptions.setTemperature(0.0);
            sessionOptions.setTemperature(1.0);
            sessionOptions.setTemperature(0.5);
        });
    }

    @Test
    void testSetAndGetMaxResponseOutputTokens() {
        // Arrange
        Integer maxTokens = 1000;
        MaxOutputTokens maxOutputTokens = MaxOutputTokens.of(maxTokens);

        // Act
        VoiceLiveSessionOptions result = sessionOptions.setMaxResponseOutputTokens(maxOutputTokens);

        // Assert
        assertSame(sessionOptions, result);
        assertEquals(maxTokens, sessionOptions.getMaxResponseOutputTokens().getValue());
    }

    @Test
    void testMethodChaining() {
        // Arrange
        String instructions = "Test instructions";
        OpenAIVoice voice = new OpenAIVoice(OpenAIVoiceName.ECHO);
        InputAudioFormat inputFormat = InputAudioFormat.PCM16;
        OutputAudioFormat outputFormat = OutputAudioFormat.PCM16;
        List<InteractionModality> modalities = Arrays.asList(InteractionModality.TEXT);
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection();
        Double temperature = 0.8;
        Integer maxTokens = 500;
        MaxOutputTokens maxOutputTokens = MaxOutputTokens.of(maxTokens);

        // Act & Assert
        VoiceLiveSessionOptions result = sessionOptions.setInstructions(instructions)
            .setVoice(voice)
            .setInputAudioFormat(inputFormat)
            .setOutputAudioFormat(outputFormat)
            .setModalities(modalities)
            .setTurnDetection(turnDetection)
            .setTemperature(temperature)
            .setMaxResponseOutputTokens(maxOutputTokens);

        assertSame(sessionOptions, result);
        assertEquals(instructions, sessionOptions.getInstructions());
        assertEquals(voice, sessionOptions.getVoice());
        assertEquals(inputFormat, sessionOptions.getInputAudioFormat());
        assertEquals(outputFormat, sessionOptions.getOutputAudioFormat());
        assertEquals(modalities, sessionOptions.getModalities());
        assertEquals(turnDetection, sessionOptions.getTurnDetection());
        assertEquals(temperature, sessionOptions.getTemperature());
        assertEquals(maxTokens, sessionOptions.getMaxResponseOutputTokens().getValue());
    }

    @Test
    void testDefaultValues() {
        // Assert default values are null/unset
        assertEquals(TEST_MODEL, sessionOptions.getModel());
        assertNull(sessionOptions.getInstructions());
        assertNull(sessionOptions.getVoice());
        assertNull(sessionOptions.getInputAudioFormat());
        assertNull(sessionOptions.getOutputAudioFormat());
        assertNull(sessionOptions.getModalities());
        assertNull(sessionOptions.getTurnDetection());
        assertNull(sessionOptions.getTools());
        assertNull(sessionOptions.getToolChoice());
        assertNull(sessionOptions.getTemperature());
        assertNull(sessionOptions.getMaxResponseOutputTokens());
    }
}
